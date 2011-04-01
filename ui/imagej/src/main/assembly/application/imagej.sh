#!/bin/sh
java -Xmx512m -cp 'plugins:plugins/*:jar/*' imagej.ImageJ $@
