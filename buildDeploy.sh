#!/usr/bin/env bash
./build.sh
docker tag mapper-cti:1.0 qopbot/mapper-cti:1.0
docker push qopbot/mapper-cti:1.0

