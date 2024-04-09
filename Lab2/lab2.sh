#! /bin/bash
echo 'Compiling Program 1'
g++ -c lab2_1.cpp
echo 'Linking Program 1'
g++ -o lab2_1 lab2_1.o -lpthread
echo 'Program 1 has been successfully started'
./lab2_1
echo 'Program 1 has been successfully finished'

echo " "

echo 'Compiling Program 2'
g++ -c lab2_2.cpp
echo 'Linking Program 2'
g++ -o lab2_2 lab2_2.o -lpthread
echo 'Program 2 has been successfully started'
./lab2_2
echo 'Program 2 has been successfully finished'

echo " "

echo 'Compiling Program 3'
g++ -c lab2_3.cpp
echo 'Linking Program 3'
g++ -o lab2_3 lab2_3.o -lpthread
echo 'Program 3 has been successfully started'
./lab2_3
echo 'Program 3 has been successfully finished'

