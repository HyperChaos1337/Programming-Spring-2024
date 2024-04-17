#!/bin/bash
echo 'Compiling Writer'
g++ -c lab6_writer.cpp
echo 'Linking Writer'
g++ -o lab6_writer lab6_writer.o -lpthread
echo 'Success. Now open second terminal and launch script for reader'
./lab6_writer
