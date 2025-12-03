@echo off
set CLASS=%1
if "%CLASS%"=="" set CLASS=ch04.AudioSplitter
gradlew run -PmainClass=%CLASS%