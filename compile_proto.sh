#!/bin/bash

PROTOPATH=$(cd "$(dirname "$0")/src/test/resources"; pwd)
PROTOFILES=`find $PROTOPATH -iname *.proto | xargs $1`

rm -rf $PROTOPATH/../java/*
protoc -I=$PROTOPATH --java_out=$PROTOPATH/../java/ $PROTOFILES
echo "ok"
