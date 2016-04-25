#!/bin/sh

set -e -x

apt-get update
apt install maven -y --fix-missing

cd "$1"

mvn package -DskipTests=true
