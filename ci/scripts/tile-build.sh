#!/bin/sh

set -e -x

apt-get update
apt install maven -y --fix-missing
apt-get install zip -y --fix-missing
apt-get install ruby -y --fix-missing
apt-get install ruby-bundler -y --fix-missing
gem install bosh_cli --no-ri --no-rdoc

wget "https://bootstrap.pypa.io/get-pip.py" -O /dev/stdout | python
pip install mkdocs

TILE_GEN_DIR=$1
SOURCE_DIR=$2
HISTORY_DIR=$3
TARGET_DIR=$4

(cd ${TILE_GEN_DIR}; pip install -r requirements.txt)

BIN_DIR="$( cd "${TILE_GEN_DIR}/bin" && pwd )"

TILE="${BIN_DIR}/tile"

HISTORY=`ls ${HISTORY_DIR}/tile-history-*.yml`
if [ -n "${HISTORY}" ]; then
	cp ${HISTORY} ${SOURCE_DIR}/tile-history.yml
fi

(cd ${SOURCE_DIR}; mvn package -DskipTests=true; ${TILE} build)

VERSION=`grep '^version:' ${SOURCE_DIR}/tile-history.yml | sed 's/^version: //'`
HISTORY="tile-history-${VERSION}.yml"

cp ${SOURCE_DIR}/product/*.pivotal ${TARGET_DIR}
cp ${SOURCE_DIR}/tile-history.yml ${TARGET_DIR}/tile-history-${VERSION}.yml