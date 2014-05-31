@echo off

if exist rm out -r

call gradle -b ../../build.gradle -Penv=%ENVIRONMENT% -Ptrg=%TARGET% -Pcnt=%CONTAINER% -x test clean build

if not exist out mkdir out
copy ..\..\livingdocuments-boot\build\libs\%TARGET_FILE% out\%TARGET_FILE% /Y
copy ..\..\livingdocuments-core\src\main\resources\application.properties out\application.properties /Y

call clean.bat
