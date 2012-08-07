#!/bin/sh

cd "$(dirname "$0")/.." &&
mvn "$@" &&
VERSION="$(sed -n 's/^\t<version>\(.*\)<\/version>.*/\1/p' < pom.xml)" &&
cd app &&
unzip -o target/imagej-$VERSION-application.zip &&
possibly_obsoletes="$(find ImageJ.app/ ! -cnewer target/imagej-$VERSION-application.zip |
	grep -ve /\\.checksums$ -e /db.xml.gz$)" &&
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
