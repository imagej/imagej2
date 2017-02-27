#!/bin/sh
dir="$(dirname "$0")"
test "$TRAVIS_SECURE_ENV_VARS" = true \
  -a "$TRAVIS_PULL_REQUEST" = false \
  -a "$TRAVIS_BRANCH" = master &&
  mvn -Pdeploy-to-imagej deploy --settings "$dir/settings.xml" ||
  mvn install
