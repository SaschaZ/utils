#!/bin/sh
 
rm -rf publications
#docker rm -f utils
docker container rm -f utilsc

docker build -t utils:latest . &&
docker run --name utilsc utils:latest

mkdir -p ./publications/android
docker cp utilsc:/project/android/build/libs ./publications/android
mkdir -p ./publications/android
docker cp utilsc:/project/android/build/outputs/aar ./publications/android

mkdir -p ./publications/android-testing
docker cp utilsc:/project/android-testing/build/libs ./publications/android-testing
mkdir -p ./publications/android-testing
docker cp utilsc:/project/android-testing/build/outputs/aar ./publications/android-testing


mkdir -p ./publications/core
docker cp utilsc:/project/core/build/libs ./publications/core

mkdir -p ./publications/core-testing
docker cp utilsc:/project/core-testing/build/libs ./publications/core-testing


mkdir -p ./publications/jdk
docker cp utilsc:/project/jdk/build/libs ./publications/jdk

mkdir -p ./publications/jdk-testing
docker cp utilsc:/project/jdk-testing/build/libs ./publications/jdk-testing