#!/bin/sh

cd "$(dirname "$0")/.." &&
mvn "$@" &&
VERSION="$(sed -n 's/^.<version>\(.*\)<\/version>.*/\1/p' < pom.xml)" &&
cd app &&
ZIP=target/imagej-$VERSION-application.zip &&
unzip -o $ZIP &&
contents="$(unzip -l $ZIP | sed -n 's/^.*\(ImageJ.app\/.*[^\/]\)$/\1/p')" &&
possibly_obsoletes="$(printf "%s\n%s\n%s" \
		"$contents" "$contents" "$(find ImageJ.app -type f)" |
	grep -ve /.checksums$ -e /db.xml.gz$ |
	sort | uniq -u)" &&
if test -n "$possibly_obsoletes"
then
	printf "The following files might be outdated:\n\n%s\nDo you want to delete them? " \
		"$possibly_obsoletes" &&
	read line &&
	case "$line" in
	y*|Y*)
		echo "$possibly_obsoletes" |
		tr '\n' '\0' |
		xargs -0 rm
		;;
	esac
fi
