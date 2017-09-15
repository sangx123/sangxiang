#!/bin/sh
if [ $# -eq 0 ]
then
  echo "No arguments supplied - supply branch name/tag (master|develop_new|VERSION_4.2.0_RELEASE_BUILD_333)"
else
    git push origin $1
    git submodule foreach --recursive git push origin $1
    #cd submodules/nxdiscovery && git push origin $1 && cd ../.. \
    #&& \
    #cd submodules/device-communication && git push origin $1 && cd ../.. \
    #&& \
    #cd submodules/server-api && git push origin $1 && cd ../..
fi

