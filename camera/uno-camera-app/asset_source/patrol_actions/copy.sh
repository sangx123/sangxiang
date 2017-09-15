#!/bin/sh
echo Copying $1/*.png to ../../app/src/main/res/drawable-$1
cp $1/*.png ../../app/src/main/res/drawable-$1/
