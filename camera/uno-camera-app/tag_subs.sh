#!/bin/sh
if [ $# -eq 0 ]
then
  echo "No arguments supplied - supply tag name (e.g. VERSION_4.2.0_RELEASE_BUILD_333)"
else
    git tag $1
    git submodule foreach --recursive git tag $1
fi

