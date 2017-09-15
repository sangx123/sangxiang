#!/bin/sh
if [ $# -eq 0 ]
then
  echo "No arguments supplied - supply branch name/tag (master|develop_new|VERSION_4.2.0_RELEASE_BUILD_333)"
else
echo "Updating source code to branch " $1
echo ">>>>> Fetching from server..."
./fetch_subs.sh
echo ">>>>> Checkout to branch " $1
./checkout_subs.sh $1
echo ">>>>> Pull to branch " $1
./pull_subs.sh $1
echo "<<<<< Update source code to branch " $1 "DONE"
fi

