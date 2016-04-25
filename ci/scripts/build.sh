#!/bin/sh

set -e -x

apt-get update
apt install maven -y --fix-missing

cd source

mvn package -DskipTests=true
pwd
ls
