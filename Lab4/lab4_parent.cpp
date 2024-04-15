#include <iostream>
#include <cstdlib>
#include <sys/wait.h>
#include <unistd.h>

int main(int argc, char** argv){
	std::cout << "Parent started..." << '\n';
	pid_t pid = getpid();
	pid_t ppid = getppid();
	pid_t child = fork();
	if(child == 0){
		std::cout << "Here I come" << '\n';
		int result = execvp("lab4_child", argv);
		if(result == -1) perror("Child: Something went wrong...");
	}else if(child > 0){
		std::cout << "Parent: pid:" << pid << " parent pid: " << ppid
			<< " child id: " << child << '\n';
		int code = 0;
		while(waitpid(child, &code, WNOHANG) == 0) usleep(500000);
		std::cout << "Parent: child exit code: " << WEXITSTATUS(code) << '\n';
	}else perror("Fork: Something went wrong...");
	std::cout << "Parent finished..." << '\n';
	return(0);
}
