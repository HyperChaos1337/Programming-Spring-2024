#include <iostream>
#include <unistd.h>

typedef struct{
	int flag;
	char sym;
}t_Args;

void *first(void *arg1){
	std::cout << "1st stream started" << '\n';
	t_Args *args = (t_Args*) arg1;
	while(args->flag == 0){
		putchar(args->sym);
		fflush(stdout);
		sleep(1);
	}
	std::cout << "1st stream stopped" << '\n';
	pthread_exit((void*)"first");
}

void *second(void *arg2){
	std::cout << "2nd stream started" << '\n';
	t_Args *args = (t_Args*) arg2;
	while(args->flag == 0){
		putchar(args->sym);
		fflush(stdout);
		sleep(1);
	}
	std::cout << "2nd stream stopped" << '\n';
	pthread_exit((void*)"second");
}

int main(){
	t_Args arg1 = {0, '1'};
	t_Args arg2 = {0, '2'};
	pthread_t id1, id2;
	pthread_create(&id1, nullptr, first, &arg1);
	pthread_create(&id2, nullptr, second, &arg2);
	std::cout << "Enter any character to continue" << '\n';
	getchar();
	std::cout << "Program is working" << '\n';
	arg1.flag = 1;
	arg2.flag = 1;
	size_t stacksize;
	pthread_attr_t attribute;
	void* stackaddress;
	pthread_getattr_np(id1, &attribute);
	pthread_attr_getstack(&attribute, &stackaddress, &stacksize);
	std::cout << "Stack 1 Size = " << stacksize  << '\n';
	std::cout << "Stack 1 Address = " << stackaddress  << '\n';
	char *ex1, *ex2;
	pthread_join(id1, (void**) &ex1);
	pthread_join(id2, (void**) &ex2);
	std::cout << "ex1 = " << ex1 << '\n';
	std::cout << "ex2 = " << ex2 << '\n';
	std::cout << "Program successfully executed!" << '\n';
	return 0;
}
