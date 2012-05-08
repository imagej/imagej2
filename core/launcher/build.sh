#!/bin/sh

die () {
	echo "$*" >&2
	exit 1
}

if test -z "$JAVA_HOME"
then
	JAVA_HOME="$(ImageJ --print-java-home)" &&
	test -d "$JAVA_HOME" ||
	die "JAVA_HOME not set" >&2
fi

if test ! -d "$JAVA_HOME/include"
then
	if test -d "$JAVA_HOME/../include"
	then
		JAVA_HOME="$JAVA_HOME/.."
	else
		die "JAVA_HOME has no include/ directory"
	fi
fi

CWD="$(dirname "$0")"
gcc -g \
	-fno-stack-protector \
	-o "$CWD"/target/ImageJ \
	-I "$CWD"/src/main/c/ \
	-I "$JAVA_HOME"/include/ \
	-I "$JAVA_HOME"/include/linux/ \
	-I "$JAVA_HOME"/include/win32/ \
	"$CWD"/src/main/c/ImageJ.c \
	-ldl
