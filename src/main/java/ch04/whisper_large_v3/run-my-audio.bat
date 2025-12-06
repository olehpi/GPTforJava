@echo off
echo ========================================
echo Whisper Transcription for Your Audio Files
echo !!!!!! set your paths to
echo !!!!!! 1) set CONFIG=your full path to whisper_large_v3\config.json
echo !!!!!! 2) set AUDIO_DIR=your full path to GPTforJava\src\main\resources\ch04\target_TheOnePlaceICantGo
echo ========================================
echo.

set CONFIG=your full path to whisper_large_v3\config.json
set AUDIO_DIR=your full path to GPTforJava\src\main\resources\ch04\target_TheOnePlaceICantGo

echo Config: %CONFIG%
echo Audio: %AUDIO_DIR%
echo.

if not exist "%AUDIO_DIR%" (
    echo ERROR: Audio directory not found!
    echo %AUDIO_DIR%
    pause
    exit /b 1
)

if not exist "%CONFIG%" (
    echo ERROR: Config file not found!
    echo %CONFIG%
    pause
    exit /b 1
)

echo Starting transcription...
echo This may take several minutes...
echo.

docker run --rm --user root ^
  -v "%CONFIG%:/app/config.json" ^
  -v "%AUDIO_DIR%:/app/audio" ^
  whisper-v3

echo.
echo Transcription completed!
echo Check results in: %AUDIO_DIR%\transcripts\
pause
