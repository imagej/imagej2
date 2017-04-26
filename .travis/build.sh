#!/bin/sh
dir="$(dirname "$0")"
if [ "$TRAVIS_SECURE_ENV_VARS" = true \
  -a "$TRAVIS_PULL_REQUEST" = false \
  -a "$TRAVIS_BRANCH" = master ]
then
  mvn -Pdeploy-to-imagej deploy --settings "$dir/settings.xml"
else
  mvn install
fi
