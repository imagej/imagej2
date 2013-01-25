#!/bin/bash

#
# tag-release.sh
#

# A script for tagging a release of the ImageJ projects.

set -e

msg () {
	printf '\n%s\n' "$*" >&2
}

DIR="$(dirname "$0")/.."

cd "$DIR"

old="$1"
new="$2"
imagej1="$3"
scifio="$4"

if [ -z "$old" -o -z "$new" -o -z "$imagej1" -o -z "$scifio" ];
then
	cat >&2 << EOF
Usage:
   tag-release.sh old.version new.version imagej1.version scifio.version

E.g.:
   tag-release.sh 2.0.0-SNAPSHOT 2.0.0-beta3 1.46r 4.4.0
EOF
	exit 1
fi

tag="v$new"

echo "====== Configuration ======"
echo "Old version = $old"
echo "New version = $new"
echo "ImageJ1 version = $imagej1"
echo "SCIFIO version = $scifio"
echo "Tag = $tag"

cd "$DIR"

msg '====== Fetching the latest commits and tags ======'
git remote update --prune

if [ -n "$(git tag -l "$tag")" ];
then
	msg "Tag '$tag' already exists. Delete it, or use a different version."
	exit 1
fi

msg '====== Updating master branch to the latest ======'
git checkout master
git merge 'HEAD@{u}'

msg '====== Updating version numbers ======'

# update project versions
mvn versions:set -DoldVersion="$old" -DnewVersion="$new" -DgenerateBackupPoms=false

# verify there are no remaining SNAPSHOT versions (except whitelisted ones)
files="$(git grep -l "$old" |
grep -v NOTICE.txt |
grep -v bin/tag-release.sh |
grep -v doc/release-steps.txt)" && {
	msg "There are still references to '$old'"
	msg "$files"
	exit 1
}

# add needed properties to toplevel POM
sed -E -i'' -e 's_(</project.rootdir>)_\1\
		<imagej.version>${project.version}</imagej.version>\
		<imglib2.version>${project.version}</imglib2.version>\
		<imagej1.version>'"$imagej1"'</imagej1.version>\
		<scifio.version>'"$scifio"'</scifio.version>_' pom.xml

msg '====== Making release commit ======'

# create a temporary branch using "detached HEAD"
git checkout HEAD^0 > /dev/null 2>&1

msg='Release version '"$new"'

This release uses the following dependency versions:
  * SCIFIO at '"$scifio"'
  * ImageJ1 at '"$imagej1"

# do the commit
git commit . -m "$msg"

msg '====== Tagging the release ======'

# create the tag
git tag -a "$tag" -m "$msg"

msg '====== Work complete ======'
