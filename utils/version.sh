#! /bin/bash

TAG=`git describe`
BRANCH=`git rev-parse --abbrev-ref HEAD`

if [ "${BRANCH}" == "master" ];
then
    #echo ${TAG}
    echo ‘3.3’
else
    echo ${TAG}-${BRANCH}
fi