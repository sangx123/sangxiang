#!/bin/sh
git submodule foreach "echo --------------------------------------------- && git status && echo"
