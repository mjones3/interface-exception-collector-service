#!/bin/bash

set -x

./mvnw -T 1C -Pnative clean native:compile -Dmaven.test.skip=true -DskipTests -Dsonar.skip=true -am
