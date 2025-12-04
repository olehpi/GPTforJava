@echo off
SET "SCRIPT_DIR=%~dp0"
SET "MP3FOLDER=%SCRIPT_DIR%..\..\..\..\resources\ch04\target_TheOnePlaceICantGo"
SET "OUTPUTFOLDER=%MP3FOLDER%\texts"

IF NOT EXIST "%OUTPUTFOLDER%" MKDIR "%OUTPUTFOLDER%"

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run_whisper.ps1" -mp3Folder "%MP3FOLDER%" -outputFolder "%OUTPUTFOLDER%"
echo.
echo Done! Full transcript → "%OUTPUTFOLDER%\full_transcript.txt"
pause