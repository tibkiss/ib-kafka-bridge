#!/bin/bash -xeiou pipefail

BUILD_UTILS_ROOT=$(cd "$(dirname "${0}")" && pwd)
WORK_DIR=${BUILD_UTILS_ROOT}/workdir
TWS_API_JAR=${WORK_DIR}/IBJts/source/JavaClient/target/tws-api-9.71.01.jar

JSAB_IB_RE_ACTOR_REPO="https://github.com/jsab/ib-re-actor"
JSAB_IB_RE_ACTOR_PATCH="${BUILD_UTILS_ROOT}/jsab.ib-re-actor.tws-package-name.patch"

if [ ! -d ${WORK_DIR} ]; then
    mkdir ${WORK_DIR}
fi

cd ${WORK_DIR}
git clone ${JSAB_IB_RE_ACTOR_REPO}
cd $(basename ${JSAB_IB_RE_ACTOR_REPO})

cat ${JSAB_IB_RE_ACTOR_PATCH} | patch -p1

lein install
