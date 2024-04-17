#include <iostream>
#include <fstream>
#include <cstdlib>
#include <unistd.h>
#include <semaphore.h>
#include <poll.h>
#include <fcntl.h>

#define N 10

int main(){

	std::cout << "Program 1 started..." << '\n';
	fcntl(STDIN_FILENO, F_SETFL, O_NONBLOCK);
	const char* sem_name = "lab5_sem";
	FILE *file;
	sem_t *sem = sem_open(sem_name, O_CREAT, (mode_t)0777, 1);
	file = fopen("lab5.txt", "a+");
	struct pollfd fds[1];
	int timeout = 0;
	fds[0].fd = fileno(stdin);
	fds[0].events = POLLIN;
	char x = '1';
	while(true){
		sem_wait(sem);
		for(int i = 0; i < N; i++){
			fprintf(file, "%c", x);
			std::cout << x;
			fflush(file);
			fflush(stdout);
			sleep(1);
		}
		sem_post(sem);
		sleep(1);
		if(ppoll(fds, 1, nullptr, nullptr) > 0){
			std::cout  << "Key Pressed" << '\n';
			break;
		}else continue;
	}
	fclose(file);
	sem_close(sem);
	sem_unlink(sem_name);
	std::cout << "Program 1 finished..." << '\n';
	return 0;
	
}
