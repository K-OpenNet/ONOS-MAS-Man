#!/usr/bin/env bash

RESULT_DIR=$1
EXP_INDEX=$2

if [ ! -e $RESULT_DIR/$EXP_INDEX ]
then
    mkdir -p $RESULT_DIR/$EXP_INDEX
fi

if [ -e ./*.txt ]
then
    cp ./*.txt $RESULT_DIR/$EXP_INDEX/
elif [ -e ./out/*.txt ]
then
    cp ./out/*.txt $RESULT_DIR/$EXP_INDEX/
fi

rm -rf out
mkdir out
cp config.json out/config.json
cp initialstate.json out/initialstate.json
javac -cp $(pwd)/src:$(pwd)/lib/* -d out $(find . -name *.java)