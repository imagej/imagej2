#!/bin/bash

#
# gen-jar-with-deps.sh
#

# Script to generate a single ImageJ JAR including all dependencies and
# source code. Called automatically by Jenkins during the ImageJ-daily job.

# Usage: bin/gen-jar-with-deps.sh

set -e

ROOT=`cd "$(dirname $0)/.." ; pwd`
cd "$ROOT"

# generate combined JAR file
mvn -Pdeps package

# show results
pwd
ls -l target/*-all.jar
