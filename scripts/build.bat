@echo off

if exist application.properties rm application.properties
if exist livingdocuments-boot-1.0.0.jar rm livingdocuments-boot-1.0.0.jar

call gradle -b ../build.gradle -Penv=prod -Ptrg=jar -Pcnt=tomcat -x test clean build

copy ..\livingdocuments-boot\build\libs\livingdocuments-boot-1.0.0.jar livingdocuments-boot-1.0.0.jar /Y
copy ..\livingdocuments-core\src\main\resources\application.properties application.properties /Y

call clean.bat
