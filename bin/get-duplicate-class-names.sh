#!/bin/sh

#
# get-duplicate-class-names.sh
#

names=$(git ls-files \*.java |
	sed -e 's|.*/||' -e 's|\.java$||' |
	sort |
	uniq -d)
for name in $names
do
	printf '\t%s\n' $name
	git ls-files \*/$name.java
done
