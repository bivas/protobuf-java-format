#!/bin/bash

BASEPATH=$(cd "$(dirname "$0")/"; pwd)
PROTOPATH="$BASEPATH/src/test/resources"
PROTOFILES=`find $PROTOPATH -iname *.proto | xargs $1`

protoc -I=$PROTOPATH --java_out=$BASEPATH/src/test/java $PROTOFILES
echo "ok"
