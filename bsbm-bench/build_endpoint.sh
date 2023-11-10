#!/usr/bin/env bash

if ! (( $# == 1 ))
then
    1>&2 echo "$0 (run_dir)"
    exit -1
fi

mkdir -p $1

cd ../qendpoint

mvn clean install -DskipTests -U

cd ../bsbm-bench

cp ../qendpoint/qendpoint-backend/target/qendpoint-backend-*-exec.jar $1/endpoint.jar
