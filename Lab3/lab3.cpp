#include <iostream>
#include <fcntl.h>
#include <unistd.h>
#include <sys/resource.h>
#include <cstdlib>
#include <cstring>

#define CHANNELS 2
#define SIZE 100

int channel[CHANNELS] = {0};

typedef struct{
	int flag;
}t_Args;

void select_channel(int arg){
	switch(arg){
		case 1:
			pipe(channel);
			break;
		case 2:
			pipe2(channel, O_NONBLOCK);
			break;
		case 3:
			pipe(channel);
			fcntl(channel[0], F_SETFL, O_NONBLOCK);
			fcntl(channel[1], F_SETFL, O_NONBLOCK);
			break;
		default:
			std::cerr << "Wrong argument!" << '\n';
			break;
	}
}

char* get_data(){
	struct rusage usage;
	if(getrusage(RUSAGE_SELF, &usage) == 0){
		long time = usage.ru_utime.tv_usec;
		char* msg = (char*)malloc(SIZE);
		sprintf(msg, "%ld microseconds\n", time);
		return msg;	
	}
	else return nullptr;
}

void *first(void *arg1){
	std::cout << "1st stream started" << '\n';
	t_Args *args = (t_Args*) arg1;
	while(args->flag == 0){
		char* data = get_data();
		ssize_t rv = write(channel[1], data, strlen(data));
		if(rv == 0) std::cout << "End Of File" << '\n';
		else if(rv == -1) std::cerr << "Something went wrong" << '\n';
		else if(rv > 0) std::cout << "Success! Message sent" << '\n';
		sleep(1);
	}
	std::cout << "1st stream stopped" << '\n';
	pthread_exit((void*)"first");
}

void *second(void *arg2){
	std::cout << "2nd stream started" << '\n';
	t_Args *args = (t_Args*) arg2;
	while(args->flag == 0){
		char msg[50];
		ssize_t rv = read(channel[0], msg, sizeof(msg));
		if(rv == 0) std::cout << "End Of File" << '\n';
		else if(rv == -1) std::cout << "Something went wrong" << '\n';
		else if(rv > 0){
		       	std::cout << "Success! Message received" << '\n';
			std::cout << msg << '\n';
		}
		sleep(1);
	}
	std::cout << "2nd stream stopped" << '\n';
	pthread_exit((void*)"second");
}


int main(int argc, char** argv){
	t_Args arg1 = {0};
	t_Args arg2 = {0};
	if(argc != 2){
		std::cerr << "Wrong number of arguments" << '\n';
		return 1;
	} 
	else{
		int arg = atoi(argv[1]);
		select_channel(arg);
	}
	pthread_t id1, id2;
	pthread_create(&id1, nullptr, first, &arg1);
	pthread_create(&id2, nullptr, second, &arg2);
	std::cout << "Enter any character to continue" << '\n';
	getchar();
	std::cout << "Program is working" << '\n';
	arg1.flag = 1;
	arg2.flag = 1;
	pthread_join(id1, nullptr);
	pthread_join(id2, nullptr);
	close(channel[0]);
	close(channel[1]);
	std::cout << "Program successfully executed!" << '\n';
	return 0;
}
