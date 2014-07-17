@echo off

set "TARGET=jar"
set "TARGET_FILE=ld-boot-1.0.0.%TARGET%"
set "ENVIRONMENT=prod"

call build.bat
