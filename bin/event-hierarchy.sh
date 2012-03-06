#!/bin/sh

# Script to generate hierarchy of ImageJ event classes.

# Usage: bin/event-hierarchy.sh

set -e

cd "$(dirname "$0")"/../ui/app
mvn dependency:copy-dependencies > /dev/null
find ../../core -name '*Event.java' | \
  sed -e 's/.*src\/main\/java\///' -e 's/\//./g' -e 's/\.java$//' |
  xargs java \
    -cp 'target/classes:target/test-classes:target/dependency/*' \
    imagej.debug.TypeHierarchy
