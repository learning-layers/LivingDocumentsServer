#!/bin/sh
#
# ---------------------------------------------------------------------
# Build war script
# ---------------------------------------------------------------------
#

rm -rf out

gradle -b ../../build.gradle -Penv=$ENVIRONMENT -Ptrg=$TARGET -Pcnt=$CONTAINER -x test clean build

mkdir -p out
mv ../../livingdocuments-boot/build/libs/$TARGET_FILE out/$TARGET_FILE
cp ../../livingdocuments-core/src/main/resources/application.properties out/application.properties

. ./clean.sh

