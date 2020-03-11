/* Generic */
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <pthread.h>
#include <semaphore.h>

/* Network */
#include <netdb.h>
#include <sys/socket.h>

#define BUF_SIZE 250

pthread_barrier_t bar;
sem_t mutex;
int twoFiles;

// Get host information (used to establishConnection)
struct addrinfo *getHostInfo(char *host, char *port)
{
  int r;
  struct addrinfo hints, *getaddrinfo_res;
  // Setup hints
  memset(&hints, 0, sizeof(hints));//ERRORCHECK
  hints.ai_family = AF_INET;
  hints.ai_socktype = SOCK_STREAM;
  if ((r = getaddrinfo(host, port, &hints, &getaddrinfo_res)))//ERRORCHECK
  {
    fprintf(stderr, "[getHostInfo:21:getaddrinfo] %s\n", gai_strerror(r));//ERRORCHECK
    return NULL;
  }

  return getaddrinfo_res;
}

// Establish connection with host
int establishConnection(struct addrinfo *info)
{
  if (info == NULL)
    return -1;

  int clientfd;
  for (; info != NULL; info = info->ai_next)
  {
    if ((clientfd = socket(info->ai_family,
                           info->ai_socktype,
                           info->ai_protocol)) < 0)//ERRORCHECK
    {
      perror("[establishConnection:35:socket]");
      continue;
    }

    if (connect(clientfd, info->ai_addr, info->ai_addrlen) < 0)//ERRORCHECK
    {
      close(clientfd);//ERRORCHECK
      perror("[establishConnection:42:connect]");
      continue;
    }

    freeaddrinfo(info);//ERRORCHECK
    return clientfd;
  }

  freeaddrinfo(info);//ERRORCHECK
  return -1;
}

// Send GET request
void GET(int clientfd, char *path)
{
  char req[1000] = {0};
  sprintf(req, "GET %s HTTP/1.0\r\n\r\n", path);//ERRORCHECK
  send(clientfd, req, strlen(req), 0);//ERRORCHECK
}

void *runCONCUR(void *arg)
{
  char **argv = (char **)arg;
  int clientfd;
  char buf[BUF_SIZE];
  char* file;
  int file1 = 1; 

  while (1)
  {
    if (twoFiles)
    {
      if (file1){ file = argv[5];}
      else {file = argv[6];}
      file1 = !file1;
    }
    else { file = argv[5];}
    
    // Establish connection with <hostname>:<port>
    clientfd = establishConnection(getHostInfo(argv[1], argv[2]));
    if (clientfd == -1)
    {
      fprintf(stderr,
              "[main:73] Failed to connect to: %s:%s%s \n",
              argv[1], argv[2], file);//ERRORCHECK
      return (void *)3;
    }
    pthread_barrier_wait(&bar); //wait until all threads have connected to server //ERRORCHECK
    
    GET(clientfd, file);
    pthread_barrier_wait(&bar); //wait until all threads have sent request //ERRORCHECK
    
    while (recv(clientfd, buf, BUF_SIZE, 0) > 0) //ERRORCHECK
    {
      fputs(buf, stdout); //ERRORCHECK
      memset(buf, 0, BUF_SIZE); //ERRORCHECK
    }
    pthread_barrier_wait(&bar); //wait until all threads have received requests //ERRORCHECK
    close(clientfd); //ERRORCHECK
  }
}

void *runFIFO(void *arg)
{
  char **argv = (char **)arg;
  int clientfd;
  char buf[BUF_SIZE];
  char* file;
  int file1 = 1; 

  while (1)
  {
    if (twoFiles)
    {
      if (file1){ file = argv[5];}
      else {file = argv[6];}
      file1 = !file1;
    }
    else { file = argv[5];}
    
    sem_wait(&mutex); //lock mutex ahead of connection establishment to ensure FIFO behavior //ERRORCHECK
    
    // Establish connection with <hostname>:<port>
    clientfd = establishConnection(getHostInfo(argv[1], argv[2]));
    if (clientfd == -1)
    {
      fprintf(stderr,
              "[main:73] Failed to connect to: %s:%s%s \n",
              argv[1], argv[2], file); //ERRORCHECK
      return (void *)3;
    }

    GET(clientfd, file);
    sem_post(&mutex); //unlock mutex, allow next request //ERRORCHECK

    while (recv(clientfd, buf, BUF_SIZE, 0) > 0) //ERRORCHECK
    {//wait to receive concurrently
      fputs(buf, stdout);//ERRORCHECK
      memset(buf, 0, BUF_SIZE);//ERRORCHECK
    }
    pthread_barrier_wait(&bar); //wait until all threads have received requests - ruins FIFO //ERRORCHECK
   close(clientfd); //ERRORCHECK
  }
}

int main(int argc, char **argv)
{

  /*Validate args*/
  if (!(argc == 6 || argc == 7))
  {
    fprintf(stderr, "USAGE: %s <hostname> <port> <threads> <schedalg> <filename1> [filename2]\n", argv[0]); //ERRORCHECK
    return 1;
  }
  if (atoi(argv[3]) < 1)//ERRORCHECK
  {
    (void)printf("ERROR: Number of threads must be > 1 %s\n", argv[3]);//ERRORCHECK
    exit(4);//ERRORCHECK
  }
  if (!strncmp(argv[4], "CONCUR", 7) && !strncmp(argv[4], "FIFO", 5))//ERRORCHECK
  {
    (void)printf("ERROR: Scheduling must be CONCUR or FIFO: %s\n", argv[4]);//ERRORCHECK
    exit(5);//ERRORCHECK
  }
  if (argc == 7) { twoFiles = 1;}
  else {twoFiles = 0;}

  int numThreads = atoi(argv[3]);
  pthread_t threads[numThreads];
  pthread_barrier_init(&bar, NULL, numThreads);//ERRORCHECK

  if (!strcmp(argv[4], "FIFO"))//ERRORCHECK
  {
    sem_init(&mutex, 0, 1);//ERRORCHECK

    for (size_t i = 0; i < numThreads; i++)
    {
      pthread_create(&threads[i], NULL, runFIFO, (void *)argv);//ERRORCHECK
    }
  }
  else
  {
    for (size_t i = 0; i < numThreads; i++)
    {
      pthread_create(&threads[i], NULL, runCONCUR, (void *)argv);//ERRORCHECK
    }
  }
  
  for (size_t i = 0; i < numThreads; i++)
  {
    pthread_join(threads[i], NULL);//ERRORCHECK
  }

  return 0;
}
