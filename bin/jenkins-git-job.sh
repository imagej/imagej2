#!/bin/sh

# This script is meant to be run by Jenkins whenever ImageJ2's Subversion
# repository changed. It will update the Git mirror accordingly.

test -d .git || {
	git init &&
	git config core.bare true &&
	git remote add origin \
	git@code.imagej.net:imagej.git &&
	git remote add github imagej-github:imagej/imagej &&
	git remote add fiji.sc hudson-imagej@fiji.sc:/srv/git/imagej2/.git &&
	for remote in origin github fiji.sc
	do
		git config remote.$remote.fetch \
			'refs/heads/svn/*:refs/remotes/*' &&
		 git config remote.$remote.push \
			 'refs/remotes/*:refs/heads/svn/*' &&
		 git config --add remote.$remote.push \
			 refs/remotes/trunk:refs/heads/master
	 done &&
	 git fetch
}
test -d .git/svn || git svn init -s http://code.imagej.net/svn/imagej
git svn fetch
for remote in origin github fiji.sc
do
        git push $remote
done
