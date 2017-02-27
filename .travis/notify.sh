#!/bin/sh
curl -fs "https://jenkins.imagej.net/job/$1/buildWithParameters?token=$TOKEN_NAME&repo=$TRAVIS_REPO_SLUG&commit=$TRAVIS_COMMIT&pr=$TRAVIS_PULL_REQUEST"
