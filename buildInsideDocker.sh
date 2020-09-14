#!/bin/sh
 
rm -rf publications
#docker rm -f utils
docker container rm -f utilsc

docker build -t utils:latest . &&
docker run --name utilsc utils:latest

docker cp utilsc:/project/publications ./