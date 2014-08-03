@echo off

if exist rm out/data -r
cd ..\..
for /d /r . %%d in (data) do @if exist "%%d" rd /s/q "%%d"
call gradle clean
cd scripts\windows
