#!/bin/bash
IMAGEJ_JAR=../ij.jar
JAVA_INCLUDE_PATH=/System/Library/Frameworks/JavaVM.framework/Headers
PYTHON_INCLUDE_PATH=/opt/local/Library/Frameworks/Python.framework/Versions/2.6/include/python2.6

javac -cp $IMAGEJ_JAR *.java
javah -jni PythonLink
cc -c -I$JAVA_INCLUDE_PATH \
      -I$PYTHON_INCLUDE_PATH PythonLink.c
cc -dynamiclib -o libPythonLink.jnilib \
   -framework JavaVM -lpython PythonLink.o
