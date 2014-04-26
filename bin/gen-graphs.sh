#!/bin/bash

#
# gen-graphs.sh
#

# Script to generate ImageJ dependency graphs using maven-graph-plugin.
# Called automatically by Jenkins during the ImageJ-daily job.
# Requires graphviz to be installed.

# Usage: bin/gen-graphs.sh

set -e

ROOT=`cd "$(dirname $0)/.." ; pwd`
cd "$ROOT"

GRAPH_CMD="
  dot
  -Gnodesep=0.3
  -Goverlap=scale
  -Gsplines=true
  -Gepsilon=0.0001
  -Tjpg
  -o dependency-graph.jpg
  dependency-graph.dot
"

mvn graph:reactor
cd target && $GRAPH_CMD
