#! /bin/bash

if hash greadlink 2>/dev/null;
then
  READLINK=greadlink
else
  READLINK=readlink
fi

echo READLINK=${READLINK}

SCRIPT_NAME=`${READLINK} -f $0`
SCRIPT_DIR=`dirname ${SCRIPT_NAME}`
SRC_DIR=`${READLINK} -f ${SCRIPT_DIR}/..`

if [ -z "${OUTPUT_NAME}" ];
then
  OUTPUT_NAME="BioLayout"
fi

echo OUTPUT_NAME=${OUTPUT_NAME}
echo SCRIPT_NAME=${SCRIPT_NAME}
echo SCRIPT_DIR=${SCRIPT_DIR}
echo SRC_DIR=${SRC_DIR}

cd ${SRC_DIR}
VERSION="3.5"

GIT_REV=`git rev-parse HEAD`
BUILDS_DIR="${SCRIPT_DIR}/builds"
BUILD_DIR="${BUILDS_DIR}/${VERSION}"

echo VERSION=${VERSION}
echo GIT_REV=${GIT_REV}
echo BUILD_DIR=${BUILD_DIR}

rm -rf ${BUILD_DIR}
mkdir -p ${BUILD_DIR}

if hash makensis 2>/dev/null;
then
  # Windows
  cd ${SRC_DIR}/nsis-installer
  makensis installer.nsi
  if [ "$?" != "0" ];
  then
      exit $?
  fi
  cp "${SRC_DIR}/nsis-installer/${OUTPUT_NAME}-${VERSION}-installer.exe" \
    ${BUILD_DIR}
else
  echo "makensis not found, skipping Windows"
fi

if hash appdmg 2>/dev/null;
then
  # OS X
  cd ${SRC_DIR}/target/osx
  cat ${SCRIPT_DIR}/dmg.spec.json.template | sed \
    -e "s/_BASE_NAME_/${OUTPUT_NAME}/g" \
    -e "s|_SCRIPT_DIR_|${SCRIPT_DIR}|g" > \
    dmg.spec.json
  rm -f "${BUILD_DIR}/${OUTPUT_NAME}-${VERSION}.dmg" && \
    appdmg dmg.spec.json "${BUILD_DIR}/${OUTPUT_NAME}-${VERSION}.dmg"
else
  #	Install with "brew install node && npm install -g appdmg"
  echo "appdmg not found, skipping OSX"
fi

# Everything else
cp "${SRC_DIR}/target/${OUTPUT_NAME}-${VERSION}.jar" \
  "${BUILD_DIR}/${OUTPUT_NAME}-${VERSION}.jar"

# Source code
cd ${SRC_DIR}
git archive --format zip -9 \
  --output "${BUILD_DIR}/${OUTPUT_NAME}-${VERSION}-source.zip" ${GIT_REV}

cp ${SRC_DIR}/src/main/resources/Resources/Images/Splash.png ${BUILD_DIR}
cp ${SRC_DIR}/src/main/resources/Resources/Images/Icon512x512.png ${BUILD_DIR}

rm ${BUILDS_DIR}/current
ln -s ${BUILD_DIR} ${BUILDS_DIR}/current
