#! /bin/bash
echo 'Compiling'
g++ -c lab1.cpp
echo 'Linking'
g++ -o lab1 lab1.o -lpthread
echo "Program has been successfully started"
./lab1
echo "Program has been successfully finished"

chmod +x ./lab1.sh
