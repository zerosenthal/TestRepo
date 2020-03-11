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
#include <sys/time.h>
#include <semaphore.h>
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
static const char * HDRS_OK = "HTTP/1.1 200 OK\nServer: nweb/%d.0\nContent-Length: %ld\nConnection: close\nContent-Type: %s\nX-stat-req-arrival-count: %d\nX-stat-req-arrival-time: %ld\nX-stat-req-dispatch-count: %d\nX-stat-req-dispatch-time: %ld\nX-stat-req-complete-count: %d\nX-stat-req-complete-time: %ld\nX-stat-req-age: %d\nX-stat-thread-id: %d\nX-stat-thread-count: %d\nX-stat-thread-html: %d\nX-stat-thread-image: %d\n\n";
static int dummy; //keep compiler happy

/* 
Globals 
Declare structs, mutexes, condition vars
*/

typedef struct Job Job;
typedef struct Buffer Buffer;
typedef struct FIFOBuf FIFOBuf;
typedef struct HPBuf HPBuf;
typedef struct ServerStats ServerStats;
typedef struct ThreadStats ThreadStats;

struct ServerStats {
	long startTime;
	int arrivalCount;
	int dispatchCount;
	int completedCount;
};

struct Job{
	int socketfd;
	int job_id;
	int arrivalCount;
	int dispatchCount;
	int completedCount;
	long arrivalTime;
	long dispatchTime;
	long completedTime;
	char readBuf[BUFSIZE+1];
	char contentType;
};

struct ThreadStats {
	int thread_id;
	int count;
	int hTMLCount;
	int imageCount;
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
static ServerStats stats; /* static = initialised to zeros */
static char* schedAlg; //default schedAlg -> ANY
pthread_mutex_t bufMutex;
pthread_cond_t prodCond, consCond;
sem_t statMutex;


/*
Helper funcs
*/

long getServerTime() {
	struct timeval tv;
	gettimeofday(&tv, NULL);//ERRORCHECK
	long time_in_mill = (tv.tv_sec) * 1000 + (tv.tv_usec) / 1000 ; 
	sem_wait(&statMutex);//ERRORCHECK
	time_in_mill -= stats.startTime;
	sem_post(&statMutex);//ERRORCHECK
	return time_in_mill;
}

void initBuf(int size)
{
	buf.capacity = size;
	buf.waiting = 0; 

	if (!strcmp(schedAlg, "ANY") || !strcmp(schedAlg, "FIFO"))//ERRORCHECK
	{
		buf.fifoBuf.jobs = (Job*)calloc(size, sizeof(Job));//ERRORCHECK
		buf.fifoBuf.front = 0;
	}
	else if (!strcmp(schedAlg, "HPIC") || !strcmp(schedAlg, "HPHC"))//ERRORCHECK
	{
		buf.hpBuf.pJobs = (Job*)calloc(size, sizeof(Job));//ERRORCHECK
		buf.hpBuf.npJobs = (Job*)calloc(size, sizeof(Job));//ERRORCHECK
		buf.hpBuf.pFront = 0;
		buf.hpBuf.pWaiting = 0;
		buf.hpBuf.npFront = 0;
		buf.hpBuf.npWaiting = 0;
	}
}

int loadBuf(struct Job* newJob, char contentType) //need to add conditional locks for buf reads
{	
	if (buf.waiting == buf.capacity) {return -1;} //buffer is full

	if (!strcmp(schedAlg, "ANY") || !strcmp(schedAlg, "FIFO"))//ERRORCHECK
	{
		int back = (buf.fifoBuf.front + (buf.waiting++)) % buf.capacity;
		buf.fifoBuf.jobs[back] = *newJob;

		return newJob->job_id;
	}
	else if (!strcmp(schedAlg, "HPIC") || !strcmp(schedAlg, "HPHC"))//ERRORCHECK
	{
		char pContent = schedAlg[2];
		if (contentType == pContent)
		{
			int back = (buf.hpBuf.pFront + ((buf.hpBuf.pWaiting)++)) % buf.capacity;
			buf.hpBuf.pJobs[back] = *newJob;
		}
		else
		{
			int back = (buf.hpBuf.npFront + (buf.hpBuf.npWaiting)++) % buf.capacity;
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

	if (!strcmp(schedAlg, "ANY") || !strcmp(schedAlg, "FIFO"))//ERRORCHECK
	{
		nextJob = buf.fifoBuf.jobs[buf.fifoBuf.front];
		buf.fifoBuf.front = (buf.fifoBuf.front + 1) % buf.capacity;
	}
	else if (!strcmp(schedAlg, "HPIC") || !strcmp(schedAlg, "HPHC"))//ERRORCHECK
	{
		if (buf.hpBuf.pWaiting != 0) //if there are available high-priority requests
		{
			nextJob = buf.hpBuf.pJobs[buf.hpBuf.pFront];
			buf.hpBuf.pFront = (buf.hpBuf.pFront + 1) % buf.capacity;
			(buf.hpBuf.pWaiting)--;
		}
		else
		{
			nextJob = buf.hpBuf.npJobs[buf.hpBuf.npFront];
			buf.hpBuf.npFront = (buf.hpBuf.npFront + 1) % buf.capacity;
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
		(void)sprintf(logbuffer, "ERROR: %s:%s Errno=%d exiting pid=%d", s1, s2, errno, getpid()); //ERRORCHECK
		break;
	case FORBIDDEN:
		dummy = write(socket_fd, HDRS_FORBIDDEN, 271);//ERRORCHECK
		(void)sprintf(logbuffer, "FORBIDDEN: %s:%s", s1, s2);//ERRORCHECK
		break;
	case NOTFOUND:
		dummy = write(socket_fd, HDRS_NOTFOUND, 224);//ERRORCHECK
		(void)sprintf(logbuffer, "NOT FOUND: %s:%s", s1, s2);//ERRORCHECK
		break;
	case LOG:
		(void)sprintf(logbuffer, " INFO: %s:%s:%d", s1, s2, socket_fd);//ERRORCHECK
		break;
	}
	/* No checks here, nothing can be done with a failure anyway */
	if ((fd = open("nweb.log", O_CREAT | O_WRONLY | O_APPEND, 0644)) >= 0)//ERRORCHECK
	{
		int len = strlen(logbuffer);
		logbuffer[len] = '\n';
		dummy = write(fd, logbuffer, len+1); /*Do it in a single thread-safe write*/ //ERRORCHECK
		(void)close(fd);//ERRORCHECK
	}
}


void web(Job *job, ThreadStats *tStats)
{
	int fd = job->socketfd;
	int hit = job->job_id;
	char *buffer = job->readBuf;
	int j, file_fd, buflen;
	long i, ret, len;
	char *fstr;
	
	logger(LOG, "request", buffer, hit);
	if (strncmp(buffer, "GET ", 4) && strncmp(buffer, "get ", 4))//ERRORCHECK
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
		(void)strcpy(buffer, "GET /index.html");//ERRORCHECK
	}

	/* work out the file type and check we support it */
	buflen = strlen(buffer);
	fstr = (char *)0;
	for (i = 0; extensions[i].ext != 0; i++)
	{
		len = strlen(extensions[i].ext);
		if (!strncmp(&buffer[buflen - len], extensions[i].ext, len))//ERRORCHECK
		{
			fstr = extensions[i].filetype;
			break;
		}
	}
	if (fstr == 0)
	{
		logger(FORBIDDEN, "file extension type not supported", buffer, fd);
	}
	if ((file_fd = open(&buffer[5], O_RDONLY)) == -1)//ERRORCHECK
	{ /* open the file for reading */
		logger(NOTFOUND, "failed to open file", &buffer[5], fd);
		goto endRequest;
	}

	job->completedTime = getServerTime();
	sem_wait(&statMutex);//ERRORCHECK
	job->completedCount = stats.completedCount;
	stats.completedCount++; //Change here to prevent multiple requests having the same completedcount
	sem_post(&statMutex);//ERRORCHECK

	logger(LOG, "SEND", &buffer[5], hit);
	len = (long)lseek(file_fd, (off_t)0, SEEK_END); /* lseek to the file end to find the length */ //ERRORCHECK
	(void)lseek(file_fd, (off_t)0, SEEK_SET);		/* lseek back to the file start ready for reading */ //ERRORCHECK
	/* print out the response line, stock headers, and a blank line at the end. */
	int age = job->dispatchCount - job->arrivalCount;
	age = age < 1 ? 0 : age;
	sem_wait(&statMutex);//ERRORCHECK
	(void)sprintf(buffer, HDRS_OK, VERSION, len, fstr, job->arrivalCount,job->arrivalTime,job->dispatchCount,job->dispatchTime,job->completedCount,job->completedTime,age,tStats->thread_id,tStats->count,tStats->hTMLCount,tStats->imageCount);//ERRORCHECK
	sem_post(&statMutex);//ERRORCHECK


	logger(LOG, "Header", buffer, hit);
	dummy = write(fd, buffer, strlen(buffer));//ERRORCHECK

	/* send file in 8KB block - last block may be smaller */
	while ((ret = read(file_fd, buffer, BUFSIZE)) > 0)//ERRORCHECK
	{
		dummy = write(fd, buffer, ret);//ERRORCHECK
	}

	close(file_fd); /*FIXED MEM LEAK*/ //ERRORCHECK
endRequest:
	sleep(1); /* allow socket to drain before signalling the socket is closed */ //ERRORCHECK
	close(fd);//ERRORCHECK
}

/* Worker thread function*/
void *worker(void *arg)
{
	int threadID = (int)(long)arg;
	ThreadStats tStats = {threadID,0,0,0};
	while (1)
	{
		pthread_mutex_lock(&bufMutex);//ERRORCHECK
		while (buf.waiting == 0) //if buffer is empty, block
			pthread_cond_wait(&consCond, &bufMutex);//ERRORCHECK
		
		Job nextJob = unloadBuf();

		nextJob.dispatchTime = getServerTime();
		sem_wait(&statMutex);//ERRORCHECK
		stats.dispatchCount++;
		nextJob.dispatchCount = stats.dispatchCount;
		sem_post(&statMutex);//ERRORCHECK

		pthread_cond_signal(&prodCond); //Awaken the master thread - there's room in buf //ERRORCHECK
		pthread_mutex_unlock(&bufMutex); //ERRORCHECK

		switch(nextJob.contentType){
			case 'I':
				tStats.imageCount++;
				break;
			case 'H':
				tStats.hTMLCount++;
				break;
		}
		tStats.count++;

		web(&nextJob, &tStats);
	}
}

/* Called by master thread to load an incoming request*/
void addJob(Job* newJob)
{
	//process fd to determine content type
	char contentType;
	memset(newJob->readBuf, 0, BUFSIZE+1); //ERRORCHECK
	long ret, i;
	ret = read(newJob->socketfd, newJob->readBuf, BUFSIZE);  /* read Web request in one go */ //ERRORCHECK

	if (ret == 0 || ret == -1)
	{ /* read failure stop now */
		logger(FORBIDDEN, "failed to read browser request", "", newJob->socketfd);
		//end request
		sleep(1); /* allow socket to drain before signalling the socket is closed */ //ERRORCHECK
		close(newJob->socketfd); //ERRORCHECK
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
		)//ERRORCHECK
	{
		contentType = 'I';
	}
	else if (strstr(newJob->readBuf, ".htm") != NULL || strstr(newJob->readBuf, ".html") != NULL)//ERRORCHECK
	{
		contentType = 'H';
	}
	else { contentType = 'E';}
	newJob->contentType = contentType;
	pthread_mutex_lock(&bufMutex);//ERRORCHECK
	while (buf.waiting == buf.capacity) //if buffer is full, block
		pthread_cond_wait(&prodCond, &bufMutex);//ERRORCHECK

	
	//had to move inside buf lock - job was consumed before arrival count was updated
	sem_wait(&statMutex);//ERRORCHECK
	stats.arrivalCount++;
	newJob->arrivalCount = stats.arrivalCount;
	sem_post(&statMutex);//ERRORCHECK

	if (loadBuf(newJob, contentType) == -1)
	{
		//mutex unlocked when buffer was full
		printf("ERROR: master thread attempted to access full buffer");//ERRORCHECK
		exit(6);
	}

	pthread_cond_broadcast(&consCond); //Awaken all workers, can't hurt//ERRORCHECK
	pthread_mutex_unlock(&bufMutex);//ERRORCHECK
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
					 argv[0], VERSION);//ERRORCHECK
		for (i = 0; extensions[i].ext != 0; i++)
			(void)printf(" %s", extensions[i].ext);//ERRORCHECK

		(void)printf("\n\tNot Supported: URLs including \"..\", Java, Javascript, CGI\n"
					 "\tNot Supported: directories / /etc /bin /lib /tmp /usr /dev /sbin \n"
					 "\tNo warranty given or implied\n\tNigel Griffiths nag@uk.ibm.com\n");//ERRORCHECK
		exit(0);//ERRORCHECK
	}
	if (!strncmp(argv[2], "/", 2) || !strncmp(argv[2], "/etc", 5) ||
		!strncmp(argv[2], "/bin", 5) || !strncmp(argv[2], "/lib", 5) ||
		!strncmp(argv[2], "/tmp", 5) || !strncmp(argv[2], "/usr", 5) ||
		!strncmp(argv[2], "/dev", 5) || !strncmp(argv[2], "/sbin", 6))//ERRORCHECK
	{
		(void)printf("ERROR: Bad top directory %s, see nweb -?\n", argv[2]);//ERRORCHECK
		exit(3);//ERRORCHECK
	}
	if (chdir(argv[2]) == -1)//ERRORCHECK
	{
		(void)printf("ERROR: Can't Change to directory %s\n", argv[2]);//ERRORCHECK
		exit(4);//ERRORCHECK
	}
	if (atoi(argv[3]) < 1)//ERRORCHECK
	{	
		(void)printf("ERROR: Number of worker threads must be > 1 %s\n", argv[3]);//ERRORCHECK
		exit(4);//ERRORCHECK
	}
	if (atoi(argv[4]) < 1)//ERRORCHECK
	{
		(void)printf("ERROR: buffer must be > 1 %s\n", argv[4]);//ERRORCHECK
		exit(5);//ERRORCHECK
	}

	logger(LOG, "nweb starting", argv[1], getpid());
	/* setup the network socket */
	if ((listenfd = socket(AF_INET, SOCK_STREAM, 0)) < 0)//ERRORCHECK
	{
		logger(ERROR, "system call", "socket", 0);
	}
	port = atoi(argv[1]);//ERRORCHECK
	if (port < 1025 || port > 65000)
	{
		logger(ERROR, "Invalid port number (try 1025->65000)", argv[1], 0);
	}
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);//ERRORCHECK
	serv_addr.sin_port = htons(port);//ERRORCHECK
	if (bind(listenfd, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0)//ERRORCHECK
	{
		logger(ERROR, "system call", "bind", 0);
	}
	if (listen(listenfd, 64) < 0)//ERRORCHECK
	{
		logger(ERROR, "system call", "listen", 0);
	}

	/* Set schedAlg */
	if (strcmp(argv[5], "ANY") && strcmp(argv[5], "FIFO") && strcmp(argv[5], "HPIC") && strcmp(argv[5], "HPHC")) //ERRORCHECK
	{
		(void)printf("ERROR: schedAlg must be one of \"ANY\", \"FIFO\", \"HPIC\", \"HPHC\", entered %s\n", argv[4]);//ERRORCHECK
		exit(1);//ERRORCHECK
	}
	else{ schedAlg = argv[5];}
	
	
	/* Initialize Buffer */
	int bufferSize = atoi(argv[4]);//ERRORCHECK
	initBuf(bufferSize);


	/*Initialize pThread stuff, stats struct*/
	sem_init(&statMutex,0,1);//ERRORCHECK
	stats.arrivalCount = 0;
	stats.completedCount = 0;
	stats.dispatchCount = 0;
	stats.startTime = getServerTime();
	
	pthread_mutex_init(&bufMutex, NULL);//ERRORCHECK
	pthread_cond_init(&prodCond, NULL);//ERRORCHECK
	pthread_cond_init(&consCond, NULL);//ERRORCHECK
	int numThreads = atoi(argv[3]);//ERRORCHECK
	pthread_t threads[numThreads];
	for (long i = 1; i <= numThreads; i++)
	{
		pthread_create(&threads[i], NULL, worker, (void *)i);//ERRORCHECK
	}


	/* Master Thread Loop*/
	for (hit = 1;; hit++)
	{
		length = sizeof(cli_addr);
		if ((socketfd = accept(listenfd, (struct sockaddr *)&cli_addr, &length)) < 0)//ERRORCHECK
		{
			logger(ERROR, "system call", "accept", 0);
		}
		long arrivalTime = getServerTime();
		Job newJob = {socketfd, hit, 0, 0, 0, arrivalTime};
		addJob(&newJob);
	}

	/* We never get here, Master thread is infinite loop
	pthread_mutex_destroy(&bufMutex);
	pthread_cond_destroy(&prodCond);
	pthread_cond_destroy(&consCond);
	*/
}
