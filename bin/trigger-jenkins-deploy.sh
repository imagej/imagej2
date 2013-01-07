#!/bin/sh

usage () {
	test $# = 0 || echo "$*" >&2
	cat >&2 << EOF
Usage: $ARGV0 [<options>...] (launcher|app) <token> [<branch>]

Use this script to trigger Jenkins to build and deploy ImageJ. It will
push the current commit to GitHub, under a temporary branchname, and
then tell Jenkins to go ahead. After a successful deployment, you can
remove that temporary branch from the repository.

Choose whether to build and deploy the ImageJ launcher ('launcher') or
ImageJ release itself ('app').

The token must match the secret stored in the Jenkins job.

If no branch name is provided, a temporary one is generated from the
current time.

Options:

--no-push	Do not push the branch but assume it was already pushed.
EOF
	exit 1
}

ARGV0=$0

NO_PUSH=
while test $# -gt 0
do
	case "$1" in
	--no-push)
		NO_PUSH=t
		;;
	-*)
		usage "Unknown option: $1"
		;;
	*)
		break
		;;
	esac
	shift
done

case $# in [23]) ;; *) usage;; esac

case "$1" in
launcher)
	JOB=ImageJ-launcher
	;;
app)
	JOB=ImageJ-release-build
	;;
*)
	usage "Unknown project: $1"
	;;
esac

TOKEN="$2"

if test $# -lt 3
then
	BRANCHNAME=tmp-$(date +%Y%m%d%H%M%S)
else
	BRANCHNAME="$3"
fi

# make sure the branch is pushed
test -n "$NO_PUSH" ||
git push github.com:imagej/imagej HEAD:refs/heads/$BRANCHNAME

# trigger the build
curl "http://jenkins.imagej.net/job/$JOB/buildWithParameters?token=$TOKEN&branch=$BRANCHNAME"

cat << EOF
Jenkins should build $JOB now:

	http://jenkins.imagej.net/job/$JOB/

Upon success, you may want to delete the temporary branch:

	git push github.com:imagej/imagej :$BRANCHNAME
EOF
