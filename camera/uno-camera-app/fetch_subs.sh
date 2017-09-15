#!/bin/sh
git fetch
git submodule foreach --recursive git fetch
