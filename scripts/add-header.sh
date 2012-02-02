#!/bin/bash

# A simple script to add header & copyright to a Java source file.

# Intended to be used with other *nix commands; e.g.:
#   find . -name '*.java' -exec bash scripts/add-header.sh {} \;

test $# = 0 && {
	find . -name '*.java' -print0 |
	xargs -0r sh "$0"
	return
}

basedir="$(dirname "$0")/.."

for file
do
	test // = "$(head -n 1 $file 2>/dev/null)" &&
	continue

	name="$(basename "$file")"
	tmp="$file.tmp"

	echo '//' > "$tmp"
	echo "// $name" >> "$tmp"
	echo '//' >> "$tmp"
	echo >> "$tmp"
	echo '/*' >> "$tmp"
	cat "$basedir/LICENSE.txt" >> "$tmp"
	echo '*/' >> "$tmp"
	echo >> "$tmp"
	cat "$file" >> "$tmp"

	mv -f "$tmp" "$file"
done
