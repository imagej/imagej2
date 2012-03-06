#!/bin/bash

#
# gen-jar-with-deps.sh
#

# Script to generate a single ImageJ JAR including all dependencies.
# Called automatically by Jenkins during the ImageJ-daily job.

# Usage: bin/gen-jar-with-deps.sh

set -e

ROOT=`cd "$(dirname $0)/.." ; pwd`
EXTRA='src/main/assembly/all'

cd "$ROOT/ui/app"

# build individual JARs and copy dependencies
mvn package dependency:copy-dependencies

# combine SezPoz annotations
mkdir -p "$EXTRA/META-INF/annotations"
java -cp 'target/test-classes:target/classes:target/dependency/*' \
  imagej.util.CombineAnnotations

# add source code
echo "Copying source files..."
# NB: This is a lame HACK because I am too stupid to figure out how to write a
# proper Maven assembly descriptor that includes the source code of all IJ2
# modules in the toplevel directory structure alongside the class files.
files=`find ../../core ../../ui -name '*.java' | grep 'src/main/java/'`
for f in $files
do
  dest=`echo $f | sed -e 's/.*\/src\/main\/java\///'`
  dir=`echo $dest | sed -e 's/\/[^\/]*\.java//'`
  mkdir -p "$EXTRA/$dir"
  cp "$f" "$EXTRA/$dest"
done

# generate combined JAR file
mvn -P deps,swing package

# clean up
rm $EXTRA/META-INF/annotations/imagej.*
rm -rf "$EXTRA/imagej"
