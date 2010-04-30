#!/bin/bash
#java -cp . CellProfilerTest $*
java -cp .:../ij.jar PipelineRunner $*
