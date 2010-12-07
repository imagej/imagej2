#!/bin/sh

mkdir api

echo CREATING CODEBLOCKS JAVADOCS
/System/Library/Frameworks/JavaVM.framework/Versions/1.5/Commands/javadoc -d api -sourcepath src @packages
echo DONE.  OPEN api/index.html
