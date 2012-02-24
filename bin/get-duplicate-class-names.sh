#!/bin/sh

names=$(git ls-files \*.java |
	grep -ve '^extra/' -e '^opencl/' -e '^zoomviewer/' |
	sed -e 's|.*/||' -e 's|\.java$||' |
	sort |
	uniq -d)
for name in $names
do
	printf '\t%s\n' $name
	git ls-files \*/$name.java
done
