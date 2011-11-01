#!/bin/sh

# This script is meant to be run by Jenkins whenever ImageJ2's Subversion
# repository changed. It will update the Git mirror accordingly.

test -d .git || {
	git init &&
	git config core.bare true &&
	git remote add origin \
		git@dev.imagejdev.org:imagej.git &&
	git config remote.origin.fetch 'refs/heads/svn/*:refs/remotes/*' &&
	git config remote.origin.push 'refs/remotes/*:refs/heads/svn/*' &&
	git config --add remote.origin.push refs/remotes/trunk:refs/heads/master &&
	git fetch
}
test -d .git/svn || git svn init -s http://dev.imagejdev.org/svn/imagej
git svn fetch
git push
