/* Generic */
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <pthread.h>

/* Network */
#include <netdb.h>
#include <sys/socket.h>

#define BUF_SIZE 250

pthread_barrier_t bar;

// Get host information (used to establishConnection)
struct addrinfo *getHostInfo(char *host, char *port)
{
  int r;
  struct addrinfo hints, *getaddrinfo_res;
  // Setup hints
  memset(&hints, 0, sizeof(hints));
  hints.ai_family = AF_INET;
  hints.ai_socktype = SOCK_STREAM;
  if ((r = getaddrinfo(host, port, &hints, &getaddrinfo_res)))
  {
    fprintf(stderr, "[getHostInfo:21:getaddrinfo] %s\n", gai_strerror(r));
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
                           info->ai_protocol)) < 0)
    {
      perror("[establishConnection:35:socket]");
      continue;
    }

    if (connect(clientfd, info->ai_addr, info->ai_addrlen) < 0)
    {
      close(clientfd);
      perror("[establishConnection:42:connect]");
      continue;
    }

    freeaddrinfo(info);
    return clientfd;
  }

  freeaddrinfo(info);
  return -1;
}

// Send GET request
void GET(int clientfd, char *path)
{
  char req[1000] = {0};
  sprintf(req, "GET %s HTTP/1.0\r\n\r\n", path);
  send(clientfd, req, strlen(req), 0);
}

void *run(void *arg)
{
  char **argv = (char **)arg;
  int clientfd;
  char buf[BUF_SIZE];

  while (1)
  {
  // Establish connection with <hostname>:<port>
  clientfd = establishConnection(getHostInfo(argv[1], argv[2]));
  if (clientfd == -1)
  {
    fprintf(stderr,
            "[main:73] Failed to connect to: %s:%s%s \n",
            argv[1], argv[2], argv[5]);
    return (void *)3;
  }

    pthread_barrier_wait(&bar);
    GET(clientfd, argv[5]);
    pthread_barrier_wait(&bar);
    while (recv(clientfd, buf, BUF_SIZE, 0) > 0)
    {
      fputs(buf, stdout);
      memset(buf, 0, BUF_SIZE);
    }
  }
  close(clientfd);
}

int main(int argc, char **argv)
{

  /*Validate args*/
  if (argc != 6)
  { //Only one file for now
    fprintf(stderr, "USAGE: %s <hostname> <port> <threads> <schedalg> <filename1>\n", argv[0]);
    return 1;
  }
  if (atoi(argv[3]) < 1)
  {
    (void)printf("ERROR: Number of threads must be > 1 %s\n", argv[3]);
    exit(4);
  }
  if (!strncmp(argv[4], "CONCUR", 7) && !strncmp(argv[4], "FIFO", 5))
  {
    (void)printf("ERROR: Scheduling must be CONCUR or FIFO: %s\n", argv[4]);
    exit(5);
  }
  //AS of now, only does concur.
  int numThreads = atoi(argv[3]);
  pthread_barrier_init(&bar, NULL, numThreads);
  pthread_t threads[numThreads];
  for (size_t i = 0; i < numThreads; i++)
  {
    pthread_create(&threads[i], NULL, run, (void *)argv);
  }

  for (size_t i = 0; i < numThreads; i++)
  {
    pthread_join(threads[i], NULL);
  }

  return 0;
}
