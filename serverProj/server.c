#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <fcntl.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <pthread.h>
#define VERSION 25
#define BUFSIZE 8096
#define ERROR 42
#define LOG 44
#define FORBIDDEN 403
#define NOTFOUND 404

struct
{
	char *ext;
	char *filetype;
} extensions [] = {
	{"gif", "image/gif" },  
	{"jpg", "image/jpg" }, 
	{"jpeg","image/jpeg"},
	{"png", "image/png" },  
	{"ico", "image/ico" },  
	{"zip", "image/zip" },  
	{"gz",  "image/gz"  },  
	{"tar", "image/tar" },  
	{"htm", "text/html" },  
	{"html","text/html" },  
	{0,0} };
  

static const char * HDRS_FORBIDDEN = "HTTP/1.1 403 Forbidden\nContent-Length: 185\nConnection: close\nContent-Type: text/html\n\n<html><head>\n<title>403 Forbidden</title>\n</head><body>\n<h1>Forbidden</h1>\nThe requested URL, file type or operation is not allowed on this simple static file webserver.\n</body></html>\n";
static const char * HDRS_NOTFOUND = "HTTP/1.1 404 Not Found\nContent-Length: 136\nConnection: close\nContent-Type: text/html\n\n<html><head>\n<title>404 Not Found</title>\n</head><body>\n<h1>Not Found</h1>\nThe requested URL was not found on this server.\n</body></html>\n";
static const char * HDRS_OK = "HTTP/1.1 200 OK\nServer: nweb/%d.0\nContent-Length: %ld\nConnection: close\nContent-Type: %s\n\n";
static int dummy; //keep compiler happy

/* 
Globals 
Declare structs, mutexes, condition vars
*/

typedef struct Job Job;
typedef struct Buffer Buffer;
typedef struct FIFOBuf FIFOBuf;
typedef struct HPBuf HPBuf;

struct Job{
	int socketfd;
	int job_id;
	char readBuf[BUFSIZE+1];
};

struct FIFOBuf{
	struct Job* jobs; //unitialized job buffer
	int front;
};

struct HPBuf{
	struct Job* pJobs; //unitialized priority job buffer
	int pFront;
	int pWaiting;
	
	struct Job* npJobs; //unitialized non-priority job buffer
	int npFront;
	int npWaiting;
};

struct Buffer{
	int capacity;
	int waiting;
	union{
		FIFOBuf fifoBuf;
		HPBuf hpBuf;
	};
};

static Buffer buf;
static char* schedAlg; //default schedAlg -> ANY
pthread_mutex_t bufMutex;
pthread_cond_t prodCond, consCond;

/*
Helper funcs
*/

void initBuf(int size)
{
	buf.capacity = size;
	buf.waiting = 0; 

	if (!strcmp(schedAlg, "ANY") || !strcmp(schedAlg, "FIFO"))
	{
		buf.fifoBuf.jobs = (Job*)calloc(size, sizeof(Job));
		buf.fifoBuf.front = 0;
	}
	else if (!strcmp(schedAlg, "HPIC") || !strcmp(schedAlg, "HPHC"))
	{
		buf.hpBuf.pJobs = (Job*)calloc(size, sizeof(Job));
		buf.hpBuf.npJobs = (Job*)calloc(size, sizeof(Job));
		buf.hpBuf.pFront = 0;
		buf.hpBuf.pWaiting = 0;
		buf.hpBuf.npFront = 0;
		buf.hpBuf.npWaiting = 0;
	}
}
int loadBuf(struct Job* newJob, char contentType) //need to add conditional locks for buf reads
{	
	if (buf.waiting == buf.capacity) {return -1;} //buffer is full

	if (!strcmp(schedAlg, "ANY") || !strcmp(schedAlg, "FIFO"))
	{
		int back = (buf.fifoBuf.front + (++buf.waiting)) % buf.capacity;
		buf.fifoBuf.jobs[back] = *newJob;

		return newJob->job_id;
	}
	else if (!strcmp(schedAlg, "HPIC") || !strcmp(schedAlg, "HPHC"))
	{
		char pContent = schedAlg[2];
		if (contentType == pContent)
		{
			int back = (buf.hpBuf.pFront + (++(buf.hpBuf.pWaiting))) % buf.capacity;
			buf.hpBuf.pJobs[back] = *newJob;
		}
		else
		{
			int back = (buf.hpBuf.npFront + (++buf.hpBuf.npWaiting)) % buf.capacity;
			buf.hpBuf.npJobs[back] = *newJob;
		}
		buf.waiting++;

		return newJob->job_id;
	}
	else return -1; //should never reach here
}

Job unloadBuf()
{	
	Job nextJob;

	if (!strcmp(schedAlg, "ANY") || !strcmp(schedAlg, "FIFO"))
	{
		int back = (buf.fifoBuf.front + (buf.waiting)) % buf.capacity;
		nextJob = buf.fifoBuf.jobs[back];
	}
	else if (!strcmp(schedAlg, "HPIC") || !strcmp(schedAlg, "HPHC"))
	{
		if (buf.hpBuf.pWaiting != 0) //if there are available high-priority requests
		{
			int back = (buf.hpBuf.pFront + (buf.hpBuf.pWaiting)) % buf.capacity;
			nextJob = buf.hpBuf.pJobs[back];
			(buf.hpBuf.pWaiting)--;
		}
		else
		{
			int back = (buf.hpBuf.npFront + (buf.hpBuf.npWaiting)) % buf.capacity;
			nextJob = buf.hpBuf.npJobs[back];
			(buf.hpBuf.npWaiting)--;
		}
	}
	(buf.waiting)--;
	return nextJob;
}

void logger(int type, char *s1, char *s2, int socket_fd)
{
	int fd;
	char logbuffer[BUFSIZE * 2];

	switch (type)
	{
	case ERROR:
		(void)sprintf(logbuffer, "ERROR: %s:%s Errno=%d exiting pid=%d", s1, s2, errno, getpid());
		break;
	case FORBIDDEN:
		dummy = write(socket_fd, HDRS_FORBIDDEN, 271);
		(void)sprintf(logbuffer, "FORBIDDEN: %s:%s", s1, s2);
		break;
	case NOTFOUND:
		dummy = write(socket_fd, HDRS_NOTFOUND, 224);
		(void)sprintf(logbuffer, "NOT FOUND: %s:%s", s1, s2);
		break;
	case LOG:
		(void)sprintf(logbuffer, " INFO: %s:%s:%d", s1, s2, socket_fd);
		break;
	}
	/* No checks here, nothing can be done with a failure anyway */
	if ((fd = open("nweb.log", O_CREAT | O_WRONLY | O_APPEND, 0644)) >= 0)
	{
		int len = strlen(logbuffer);
		logbuffer[len] = '\n';
		dummy = write(fd, logbuffer, len+1); /*Do it in a single thread-safe write*/
		(void)close(fd);
	}
}


void web(int fd, int hit, char* buffer)
{
	int j, file_fd, buflen;
	long i, ret, len;
	char *fstr;
	
	logger(LOG, "request", buffer, hit);
	if (strncmp(buffer, "GET ", 4) && strncmp(buffer, "get ", 4))
	{
		logger(FORBIDDEN, "Only simple GET operation supported", buffer, fd);
		goto endRequest;
	}
	for (i = 4; i < BUFSIZE; i++)
	{ /* null terminate after the second space to ignore extra stuff */
		if (buffer[i] == ' ')
		{ /* string is "GET URL " +lots of other stuff */
			buffer[i] = 0;
			break;
		}
	}
	for (j = 0; j < i - 1; j++)
	{ /* check for illegal parent directory use .. */
		if (buffer[j] == '.' && buffer[j + 1] == '.')
		{
			logger(FORBIDDEN, "Parent directory (..) path names not supported", buffer, fd);
			goto endRequest;
		}
	}
	if (!strncmp(&buffer[0], "GET /\0", 6) || !strncmp(&buffer[0], "get /\0", 6))
	{ /* convert no filename to index file */
		(void)strcpy(buffer, "GET /index.html");
	}

	/* work out the file type and check we support it */
	buflen = strlen(buffer);
	fstr = (char *)0;
	for (i = 0; extensions[i].ext != 0; i++)
	{
		len = strlen(extensions[i].ext);
		if (!strncmp(&buffer[buflen - len], extensions[i].ext, len))
		{
			fstr = extensions[i].filetype;
			break;
		}
	}
	if (fstr == 0)
	{
		logger(FORBIDDEN, "file extension type not supported", buffer, fd);
	}
	if ((file_fd = open(&buffer[5], O_RDONLY)) == -1)
	{ /* open the file for reading */
		logger(NOTFOUND, "failed to open file", &buffer[5], fd);
		goto endRequest;
	}
	logger(LOG, "SEND", &buffer[5], hit);
	len = (long)lseek(file_fd, (off_t)0, SEEK_END); /* lseek to the file end to find the length */
	(void)lseek(file_fd, (off_t)0, SEEK_SET);		/* lseek back to the file start ready for reading */
	/* print out the response line, stock headers, and a blank line at the end. */
	(void)sprintf(buffer, HDRS_OK, VERSION, len, fstr);
	logger(LOG, "Header", buffer, hit);
	dummy = write(fd, buffer, strlen(buffer));

	/* send file in 8KB block - last block may be smaller */
	while ((ret = read(file_fd, buffer, BUFSIZE)) > 0)
	{
		dummy = write(fd, buffer, ret);
	}
	close(file_fd); /*FIXED MEM LEAK*/
endRequest:
	sleep(1); /* allow socket to drain before signalling the socket is closed */
	close(fd);
}

/* Worker thread function*/
void *worker(void *arg)
{
	while (1)
	{
		pthread_mutex_lock(&bufMutex);
		while (buf.waiting == 0) //if buffer is empty, block
			pthread_cond_wait(&consCond, &bufMutex);
		Job nextJob = unloadBuf();
		pthread_cond_signal(&prodCond); //Awaken the master thread - there's room in buf
		pthread_mutex_unlock(&bufMutex);

		web(nextJob.socketfd, nextJob.job_id, nextJob.readBuf);
	}
}

/* Called by master thread to load an incoming request*/
void addJob(Job* newJob)
{
	//process fd to determine content type
	char contentType;
	memset(newJob->readBuf, 0, BUFSIZE+1);
	long ret, i;
	ret = read(newJob->socketfd, newJob->readBuf, BUFSIZE);  /* read Web request in one go */

	if (ret == 0 || ret == -1)
	{ /* read failure stop now */
		logger(FORBIDDEN, "failed to read browser request", "", newJob->socketfd);
		//end request
		sleep(1); /* allow socket to drain before signalling the socket is closed */
		close(newJob->socketfd);
		return;
	}
	if (ret > 0 && ret < BUFSIZE)
	{					 /* return code is valid chars */
		newJob->readBuf[ret] = 0; /* terminate the buffer */
	}
	else
	{
		newJob->readBuf[0] = 0;
	}
	for (i = 0; i < ret; i++)
	{ /* remove CF and LF characters */
		if (newJob->readBuf[i] == '\r' || newJob->readBuf[i] == '\n')
		{
			newJob->readBuf[i] = '*';
		}
	}

	if (strstr(newJob->readBuf, ".gif") != NULL ||
		strstr(newJob->readBuf, ".jpg") != NULL ||
		strstr(newJob->readBuf, ".jpeg") != NULL ||
		strstr(newJob->readBuf, ".png") != NULL ||
		strstr(newJob->readBuf, ".ico") != NULL ||
		strstr(newJob->readBuf, ".zip") != NULL ||
		strstr(newJob->readBuf, ".gz") != NULL ||
		strstr(newJob->readBuf, ".tar") != NULL
		)
	{
		contentType = 'I';
	}
	else if (strstr(newJob->readBuf, ".htm") != NULL || strstr(newJob->readBuf, ".html") != NULL)
	{
		contentType = 'H';
	}
	else { contentType = 'E';}

	pthread_mutex_lock(&bufMutex);
	while (buf.waiting == buf.capacity) //if buffer is full, block
		pthread_cond_wait(&prodCond, &bufMutex);

	if (loadBuf(newJob, contentType) == -1)
	{
		//mutex unlocked when buffer was full
		printf("ERROR: master thread attempted to access full buffer");
		exit(6);
	}

	pthread_cond_broadcast(&consCond); //Awaken all workers, can't hurt
	pthread_mutex_unlock(&bufMutex);
}

int main(int argc, char **argv)
{
	int i, port, listenfd, socketfd, hit;
	socklen_t length;
	static struct sockaddr_in cli_addr;  /* static = initialised to zeros */
	static struct sockaddr_in serv_addr; /* static = initialised to zeros */

	if (argc < 6 || argc > 6 || !strcmp(argv[1], "-?"))
	{
		(void)printf("USAGE: %s <port-number> <top-directory> <threads> <buffers> <schedalg>\t\tversion %d\n\n"
					 "\tnweb is a small and very safe mini web server\n"
					 "\tnweb only servers out file/web pages with extensions named below\n"
					 "\t and only from the named directory or its sub-directories.\n"
					 "\tProvides multi-threaded functionality, based on user-determined\n"
					 "\t thread count, job queue size, and scheduling algorithm.\n"
					 "\tExample: nweb 8181 /home/nwebdir 10 8 FIFO &\n\n"
					 "\tOnly Supports \"ANY\", \"FIFO\" (First In First Out), \"HPIC\"\n"
					 "\t (High Priority Image Content), and \"HPHC\" (High Priority HTML Content)\n"
					 "\t scheduling policies.\n"
					 "\tOnly Supports:",
					 argv[0], VERSION);
		for (i = 0; extensions[i].ext != 0; i++)
			(void)printf(" %s", extensions[i].ext);

		(void)printf("\n\tNot Supported: URLs including \"..\", Java, Javascript, CGI\n"
					 "\tNot Supported: directories / /etc /bin /lib /tmp /usr /dev /sbin \n"
					 "\tNo warranty given or implied\n\tNigel Griffiths nag@uk.ibm.com\n");
		exit(0);
	}
	if (!strncmp(argv[2], "/", 2) || !strncmp(argv[2], "/etc", 5) ||
		!strncmp(argv[2], "/bin", 5) || !strncmp(argv[2], "/lib", 5) ||
		!strncmp(argv[2], "/tmp", 5) || !strncmp(argv[2], "/usr", 5) ||
		!strncmp(argv[2], "/dev", 5) || !strncmp(argv[2], "/sbin", 6))
	{
		(void)printf("ERROR: Bad top directory %s, see nweb -?\n", argv[2]);
		exit(3);
	}
	if (chdir(argv[2]) == -1)
	{
		(void)printf("ERROR: Can't Change to directory %s\n", argv[2]);
		exit(4);
	}
	if (atoi(argv[3]) < 1)
	{	
		(void)printf("ERROR: Number of worker threads must be > 1 %s\n", argv[3]);
		exit(4);
	}
	if (atoi(argv[4]) < 1)
	{
		(void)printf("ERROR: buffer must be > 1 %s\n", argv[4]);
		exit(5);
	}

	logger(LOG, "nweb starting", argv[1], getpid());
	/* setup the network socket */
	if ((listenfd = socket(AF_INET, SOCK_STREAM, 0)) < 0)
	{
		logger(ERROR, "system call", "socket", 0);
	}
	port = atoi(argv[1]);
	if (port < 1025 || port > 65000)
	{
		logger(ERROR, "Invalid port number (try 1025->65000)", argv[1], 0);
	}
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	serv_addr.sin_port = htons(port);
	if (bind(listenfd, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0)
	{
		logger(ERROR, "system call", "bind", 0);
	}
	if (listen(listenfd, 64) < 0)
	{
		logger(ERROR, "system call", "listen", 0);
	}

	/* Set schedAlg */
	if (strcmp(argv[5], "ANY") && strcmp(argv[5], "FIFO") && strcmp(argv[5], "HPIC") && strcmp(argv[5], "HPHC")) 
	{
		(void)printf("ERROR: schedAlg must be one of \"ANY\", \"FIFO\", \"HPIC\", \"HPHC\", entered %s\n", argv[4]);
		exit(1);
	}
	else{ schedAlg = argv[5];}
	
	/* Initialize Buffer */
	int bufferSize = atoi(argv[4]);
	initBuf(bufferSize);

	/*Initialize pThread stuff */
	pthread_mutex_init(&bufMutex, NULL);
	pthread_cond_init(&prodCond, NULL);
	pthread_cond_init(&consCond, NULL);
	int numThreads = atoi(argv[3]);
	pthread_t threads[numThreads];
	for (int i = 0; i < numThreads; i++)
	{
		pthread_create(&threads[i], NULL, worker, NULL);
	}

	/* Master Thread Loop*/
	for (hit = 1;; hit++)
	{
		length = sizeof(cli_addr);
		if ((socketfd = accept(listenfd, (struct sockaddr *)&cli_addr, &length)) < 0)
		{
			logger(ERROR, "system call", "accept", 0);
		}
		Job newJob = {socketfd, hit};
		addJob(&newJob);
	}

	/* We never get here, Master thread is infinite loop
	pthread_mutex_destroy(&bufMutex);
	pthread_cond_destroy(&prodCond);
	pthread_cond_destroy(&consCond);
	*/
}
