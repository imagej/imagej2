#!/bin/sh

mkdir build

echo COMPILING CODEBLOCKS SOURCE
javac -g -source 1.5 -target 1.5 -g -deprecation -d build -classpath . src/codeblocks/*.java src/codeblocks/rendering/*.java src/codeblockutil/*.java src/workspace/*.java

cd build

echo CREATING BUILD/CODEBLOCKS.JAR
jar cf codeblocks.jar codeblocks codeblockutil workspace

echo CLEANING UP
rm -rf codeblocks codeblockutil workspace
cd ..

echo DONE