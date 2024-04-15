#include <iostream>
#include <cstdlib>
#include <unistd.h>

#define EXIT_CODE 10

int main(int argc, char** argv){
	std::cout << "Child started..." << '\n';
	pid_t pid = getpid();
	pid_t ppid = getppid();
	std::cout << "pid: " <<  pid << " parent pid: " << ppid << '\n';
	std::cout << "CLI arguments:" << '\n';
	for(int i = 0; i < argc; i++) std::cout << argv[i] << " ";
	std::cout << '\n' << "Child finished..." << '\n';
	exit(EXIT_CODE);
}
