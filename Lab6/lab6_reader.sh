#!/bin/bash
echo 'Compiling Reader'
g++ -c lab6_reader.cpp
echo 'Linking Reader'
g++ -o lab6_reader lab6_reader.o -lpthread
echo 'Success'
./lab6_reader
