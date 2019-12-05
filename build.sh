#!/usr/bin/env bash
docker build --build-arg JARFILE=./target/device-mapper-cti-exec.jar -t mapper-cti:1.1 .

