#!/usr/bin/env bash

rm -rf out
mkdir out
cp config.json out/config.json
javac -cp $(pwd)/src:$(pwd)/lib/* -d out $(find . -name *.java)