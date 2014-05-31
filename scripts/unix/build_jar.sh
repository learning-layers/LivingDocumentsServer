#!/bin/sh
#
# ---------------------------------------------------------------------
# Build war script
# ---------------------------------------------------------------------
#

TARGET=jar
TARGET_FILE="livingdocuments-boot-1.0.0.$TARGET"
ENVIRONMENT=prod
CONTAINER=tomcat

. ./build.sh
