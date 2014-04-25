#!/bin/bash

#
# gen-jar-with-deps.sh
#

# Script to generate a single ImageJ JAR including all dependencies.
# Called automatically by Jenkins during the ImageJ-daily job.

# Usage: bin/gen-jar-with-deps.sh

set -e

ROOT=`cd "$(dirname $0)/.." ; pwd`
ALL='src/main/assembly/all'

cd "$ROOT"

# build individual JARs and copy dependencies
mvn package dependency:copy-dependencies

# combine SciJava plugin annotations
mkdir -p "$ALL/META-INF/annotations"
java -cp 'target/classes:target/dependency/*' \
  org.scijava.annotations.AnnotationCombiner

# add source code
echo "Copying source files..."
#
# NB: This is a lame HACK because I am too stupid to figure out how to write
# a proper Maven assembly descriptor that includes the source code of all
# modules in the toplevel directory structure alongside the class files.
#
# TODO: The way to do it is to add a profile with the source dependencies
# using <classifier>sources</classifier>. Then activate the profile in the
# mvn command above so that the sources get copied as well.
#
files=`find .. -name '*.java' | grep 'src/main/java/'`
for f in $files
do
  dest=`echo $f | sed -e 's/.*\/src\/main\/java\///'`
  dir=`echo $dest | sed -e 's/\/[^\/]*\.java//'`
  mkdir -p "$ALL/$dir"
  cp "$f" "$ALL/$dest"
done

# generate combined JAR file
mvn -P deps package

# clean up
rm -rf "$ALL/imagej"

# show results
pwd
ls -l target/*-all.jar
