#!/bin/bash
set -x

mvn -T 1C clean package -Dmaven.test.skip=true -DskipTests -Dsonar.skip=true -am
