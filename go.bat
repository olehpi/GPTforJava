@echo off
set CLASS=%1
if "%CLASS%"=="" (
    echo ERROR: No main class provided.
    echo Usage: go ClassName [args...]
    exit /b 1
)
shift

REM Collect all the remaining arguments into one string with quotation marks
set ARGS=
:loop
if "%1"=="" goto endloop
set ARGS=%ARGS% "%~1"
shift
goto loop
:endloop

REM Remove extra spaces at the beginning
set ARGS=%ARGS:~1%

gradlew run -PmainClass=%CLASS% --args=%ARGS%
