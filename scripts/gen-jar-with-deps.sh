#!/bin/bash

# Script to generate a single ImageJ JAR including all dependencies.
# Called automatically by Jenkins during the ImageJ-daily job.

# Usage: bash scripts/gen-jar-with-deps.sh

set -e

ROOT=`cd "$(dirname $0)/.." ; pwd`
cd $ROOT/ui/app
mvn package dependency:copy-dependencies
mkdir -p src/main/assembly/all/META-INF/annotations
java -cp 'target/test-classes:target/classes:target/dependency/*' \
  imagej.util.CombineAnnotations
mvn -P deps,swing package
