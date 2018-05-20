#!/bin/bash
set -xe

mkdir storage

docker-compose build
docker-compose pull
docker-compose up
