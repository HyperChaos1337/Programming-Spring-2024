#! /bin/bash
echo 'Compiling'
g++ -c lab3.cpp
echo 'Linking'
g++ -o lab3 lab3.o -lpthread
echo 'Select option'
echo '1. pipe()'
echo '2. pipe2()'
echo '3. pipe()'
