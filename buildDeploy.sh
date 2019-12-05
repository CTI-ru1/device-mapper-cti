#!/usr/bin/env bash
./build.sh
docker tag mapper-cti:1.1 qopbot/mapper-cti:1.1
docker push qopbot/mapper-cti:1.1

