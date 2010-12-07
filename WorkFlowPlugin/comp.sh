#!/bin/sh

mkdir build

echo COMPILING CODEBLOCKS SOURCE
/System/Library/Frameworks/JavaVM.framework/Versions/1.5/Commands/javac -g -source 1.5 -target 1.5 -g -deprecation -d build -classpath .:lib/TableLayout.jar:lib/jfreechart-1.0.0-rc1.jar:lib/jcommon-1.0.0-rc1.jar src/codeblocks/*.java src/codeblocks/rendering/*.java src/codeblockutil/*.java src/workspace/typeblocking/*.java src/workspace/*.java src/controller/*.java src/renderable/*.java

cd build

echo CREATING BUILD/CODEBLOCKS.JAR
cp ../src/codeblocks/*.png codeblocks/
cp ../src/codeblocks/*.wav codeblocks/

/System/Library/Frameworks/JavaVM.framework/Versions/1.5/Commands/jar cf codeblocks.jar codeblocks codeblockutil  controller workspace renderable

echo CLEANING UP
rm -rf codeblocks codeblockutil controller workspace renderable
cd ..

echo DONE CREATING JAR
