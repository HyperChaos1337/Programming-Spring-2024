#include <iostream>
#include <cstdlib>
#include <cstring>
#include <fcntl.h>
#include <pthread.h>
#include <semaphore.h>
#include <signal.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <sys/resource.h>
#include <unistd.h>

const int MEMORY = 1000;
const int EXIT_CODE = 17;

bool flag = false;
sem_t *writer;
const char *writer_name = "write_sem";
sem_t *reader;
const char *reader_name = "read_sem";
int posix_shmem;
void *addr = malloc(MEMORY);

int get_data(){
	struct rusage usage;
	if(getrusage(RUSAGE_SELF, &usage) == 0){
		long time = usage.ru_utime.tv_usec;
		return time;	
	}
	else return -1;
}

void *first(void *arg) {
    std::cout << "Writer Thread started..." << '\n';
    while (flag == false) {
        int data = get_data();
        std::cout << "Result: " << data << '\n';
        memcpy(addr, &data, sizeof(data));
        fflush(stdout);
        sem_post(writer);
        sem_wait(reader);
        sleep(1);
    }

    std::cout << "Writer Thread finished..." << '\n';
    pthread_exit((void *)EXIT_CODE);
}

void handle_signal(int num) {
    std::cout << '\n' << "SIGINT" << num << '\n';
    sem_close(writer);
    sem_unlink(writer_name);
    sem_close(reader);
    sem_unlink(reader_name);

    munmap(addr, MEMORY/10);
    close(posix_shmem);
    shm_unlink("lab6_memory");
    exit(0);
}

int main(){

    std::cout << "Writer Main started..." << '\n';
    signal(SIGINT, handle_signal);
    pthread_t id;

    posix_shmem = shm_open("lab6_memory", O_RDWR | O_CREAT, S_IRWXU);
    ftruncate(posix_shmem, sizeof(int));
    addr = mmap(addr, sizeof(int), PROT_WRITE | PROT_READ, MAP_SHARED, posix_shmem, 0);
    writer = sem_open(writer_name, O_CREAT, (mode_t)0777, 0);
    reader = sem_open(reader_name, O_CREAT, (mode_t)0777, 0);

    pthread_create(&id, NULL, first, NULL);

    std::cout << "Writer Main awaits for input..." << '\n';
    getchar();
    std::cout << "Keyboard input received!" << '\n';

    flag = true;

    int ret = 0;
    pthread_join(id, (void **)&ret);

    sem_close(writer);
    sem_unlink(writer_name);
    sem_close(reader);
    sem_unlink(reader_name);

    munmap(addr, MEMORY/10);
    close(posix_shmem);
    shm_unlink("lab6_memory");

    std::cout << "Writer Thread returned: " << ret << '\n';
    std::cout << "Writer Thread finished working!" << '\n';
    
    return 0;
    
}
