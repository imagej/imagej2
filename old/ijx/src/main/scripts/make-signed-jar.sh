#!/bin/sh

cd "$(dirname "$0")"

if ! type jarsigner > /dev/null 2>&1
then
	test -x "$JAVA_HOME"/bin/jarsigner ||
	JAVA_HOME="$(../fiji --print-java-home)"/.. ||
	exit 1
	export PATH=$PATH:"$JAVA_HOME"/bin
fi

jarsigner $(cat .git/jarsignerrc) -signedjar signed-ij.jar ij.jar dscho
