#!/bin/bash

docker-compose up -d

docker logs -f pdp-service > ./PDP/logs.txt
docker logs -f idm-service > ./IDM/logs.txt
docker logs -f consult-service > ./Consult/logs.txt
docker logs -f gateway-service > ./Gateway/logs.txt
