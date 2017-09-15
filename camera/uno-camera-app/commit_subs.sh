#!/bin/sh
if [ $# -eq 0 ]
then
  echo "No arguments supplied - give commit message in quotes."
else 
  echo \"$*\"
  cd submodules/android-actors && git add . -A && git commit -m "$*"  
  cd ../.. 
  cd submodules/device-registration && git add . -A && git commit -m "$*"  
  cd ../.. 
  cd submodules/nxdiscovery && git add . -A && git commit -m "$*" 
  cd ../.. 
  cd submodules/device-communication && git add . -A && git commit -m "$*" 
  cd ../.. 
  cd submodules/server-api && git add . -A && git commit -m "$*" 
  cd ../..
fi
