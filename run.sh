#!/usr/bin/env bash

if [ $# -lt 2 ]
then
    echo "miss some parameters"
    exit 1
fi

./compile.sh $1 $2

pushd $(pwd)/out
java -cp .:../lib/* Controller.Controller
popd