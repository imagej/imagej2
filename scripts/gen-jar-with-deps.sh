#!/bin/bash

# Script to generate a single ImageJ JAR including all dependencies.

set -e

ROOT=`cd "$(dirname $0)/.." ; pwd`
cd $ROOT/ui/app
mvn package dependency:copy-dependencies
mkdir -p src/main/assembly/all/META-INF/annotations
java -cp 'target/test-classes:target/classes:target/dependency/*' \
  imagej.util.CombineAnnotations
mvn -P deps,swing package
