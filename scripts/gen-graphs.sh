#!/bin/sh

# Script to generate ImageJ dependency graphs using maven-graph-plugin.
# Requires graphviz to be installed.

set -e

ROOT=`cd "$(dirname $0)/.." ; pwd`
COMPONENTS=(. core gui)

GRAPH_CMD="
  neato
  -Goverlap=scale
  -Gsplines=true
  -Gepsilon=0.0001
  -Tsvg
  -o dependency-graph.svg
  dependency-graph.dot
"

for c in ${COMPONENTS[*]}
do
  cd $ROOT/$c
  mvn graph:reactor
  cd target && $GRAPH_CMD
done
