param(
    [string]$mp3Folder,
    [string]$outputFolder
)

# Ensure output folder exists
if (-not (Test-Path $outputFolder)) {
    New-Item -ItemType Directory -Path $outputFolder | Out-Null
}

# Full transcript file path
$fullOutput = Join-Path $outputFolder "full_transcript.txt"

# Remove old full transcript if exists
if (Test-Path $fullOutput) { Remove-Item $fullOutput }

# Loop through all MP3 files in the input folder
Get-ChildItem "$mp3Folder\*.mp3" | ForEach-Object {
    $mp3 = $_.Name
    $base = $_.BaseName
    $txtOutput = Join-Path $outputFolder ($base + "_output.txt")

    Write-Host "Processing $mp3 ..."

    # Run Docker Whisper container; stderr suppressed, stdout filtered to remove timestamps
    $cmd = "python openai-whisper.py -p /audio/$mp3 -o /audio 2>/dev/null"
    docker run --rm -v "${mp3Folder}:/audio" -it nosana/whisper:latest bash -c $cmd |
        ForEach-Object { ($_ -replace '\[\d{2}:\d{2}\.\d{3} --> \d{2}:\d{2}\.\d{3}\]\s*','') } |
        Out-File -FilePath $txtOutput -Encoding UTF8

    # Append segment text to the full transcript
    if (Test-Path $txtOutput) {
        Get-Content $txtOutput | Add-Content $fullOutput
        Add-Content $fullOutput ""  # empty line between segments
    }
}

Write-Host "Done! All segments have been merged into $fullOutput"

