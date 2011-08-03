#!/bin/sh
cd "$(dirname "$0")"/../ui/imagej
mvn dependency:copy-dependencies
find ../../core -name '*Event.java' | \
  sed -e 's/.*src\/main\/java\///' -e 's/\//./g' -e 's/\.java$//' |
  xargs java \
    -cp 'target/classes:target/test-classes:target/dependency/*' \
    imagej.debug.TypeHierarchy
