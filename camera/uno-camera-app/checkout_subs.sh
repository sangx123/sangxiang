#!/bin/sh
if [ $# -eq 0 ]
then
  echo "No arguments supplied - supply branch name (master|develop_new)"
else
    git checkout $1
    #git submodule foreach --recursive git checkout $1
    cd submodules/server-api && git checkout $1 && cd ../..
    cd submodules/device-communication && git checkout $1 && cd ../..
    #cd submodules/nxdiscovery && git push origin $1 && cd ../.. \
    #&& \
    #cd submodules/device-communication && git push origin $1 && cd ../.. \
    #&& \
    #cd submodules/server-api && git push origin $1 && cd ../..
fi

