#!/usr/bin/env bash

cd ${BASE_PATH} || exit
java -jar ${BASE_PATH}/group-center-docker.jar --spring.config.location=file:application.yml
