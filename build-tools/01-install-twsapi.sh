#!/bin/bash -xeiou pipefail

BUILD_UTILS_ROOT=$(cd "$(dirname "${0}")" && pwd)
WORK_DIR=${BUILD_UTILS_ROOT}/workdir

TWS_API_URL="http://interactivebrokers.github.io/downloads/twsapi_macunix.971.01.jar"
TWSAPI_POM=${BUILD_UTILS_ROOT}/twsapi-pom.xml

if [ ! -d ${WORK_DIR} ]; then
    mkdir ${WORK_DIR}
fi

cd ${WORK_DIR}
wget ${TWS_API_URL}
unzip $(basename ${TWS_API_URL})

cp ${TWSAPI_POM} ${WORK_DIR}/IBJts/source/JavaClient/pom.xml

cd ${WORK_DIR}/IBJts/source/JavaClient
mvn install
