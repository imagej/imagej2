#!/bin/bash

# A simple script to add header & copyright to a Java source file.

# Intended to be used with other *nix commands; e.g.:
#   find . -name '*.java' -exec bash scripts/add-header.sh {} \;

file="$1"
name="$(basename "$file")"
basedir="$(dirname "$0")/.."
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
