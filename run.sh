#!/usr/bin/env bash

./compile.sh

pushd $(pwd)/out
java -cp .:../lib/* Controller.Controller
popd