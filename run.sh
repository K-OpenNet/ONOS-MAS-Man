#!/usr/bin/env bash

if [ $0 -lt 2 ]
then
    echo "miss some parameters"
    exit 2
fi

./compile.sh $1 $2

pushd $(pwd)/out
java -cp .:../lib/* Controller.Controller
popd