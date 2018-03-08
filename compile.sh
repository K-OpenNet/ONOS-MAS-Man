#!/usr/bin/env bash

RESULT_DIR=$1
EXP_INDEX=$2

if [ ! -e $RESULT_DIR/$EXP_INDEX ]
then
    mkdir -p $RESULT_DIR/$EXP_INDEX
fi

if [ -e ./*.csv ]
then
    cp ./*.csv $RESULT_DIR/$EXP_INDEX/
elif [ -e ./out/*.csv ]
then
    cp ./out/*.csv $RESULT_DIR/$EXP_INDEX/
fi

rm -rf out
mkdir out
cp config.json out/config.json
cp initialstate.json out/initialstate.json
javac -cp $(pwd)/src:$(pwd)/lib/* -d out $(find . -name *.java)