#! /bin/bash
echo 'Compiling'
g++ -c lab4_parent.cpp
g++ -c lab4_child.cpp
echo 'Linking'
g++ -o lab4_parent lab4_parent.o
g++ -o lab4_child lab4_child.o
echo 'input some args for parent'
export PATH="${PATH}:$(pwd)"
