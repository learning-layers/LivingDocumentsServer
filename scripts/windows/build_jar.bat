@echo off

set "TARGET=jar"
set "TARGET_FILE=livingdocuments-boot-1.0.0.%TARGET%"
set "ENVIRONMENT=prod"
set "CONTAINER=tomcat"

call build.bat
