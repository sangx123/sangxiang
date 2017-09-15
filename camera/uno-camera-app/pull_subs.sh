#!/bin/sh
if [ $# -eq 0 ]
then
  echo "No arguments supplied - supply git branch (master|develop_new)"
else
    git pull origin $1
    cd submodules/server-api && git pull origin $1 && cd ../..
    cd submodules/device-communication && git pull origin $1 && cd ../..
    #git submodule foreach --recursive git pull origin $1 
    #cd submodules/android-actors && git pull origin $1 && cd ../.. \
    #&& \
    #cd submodules/device-registration && git pull origin $1 && cd ../.. \
    #&& \
    #cd submodules/nxdiscovery && git pull origin $1 && cd ../.. \
    #&& \
    #cd submodules/device-communication && git pull origin $1 && cd ../.. \
    #&& \
    #cd submodules/server-api && git pull origin $1 && cd ../..
fi


