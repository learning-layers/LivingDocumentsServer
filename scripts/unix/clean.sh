#!/bin/sh
#
# ---------------------------------------------------------------------
# Cleanup script
# ---------------------------------------------------------------------
#

rm -rf out/data .gradle
find ../../ -name data -exec rm -rf {} \;

gradle -b ../../build.gradle clean

