"""
Input/Output manager for handling audio files.
Provides functions for finding and managing audio files for transcription.
"""

import sys
from pathlib import Path
from typing import List, Set, Optional, Dict

from text_formatter import format_transcript_text


def find_and_display_audio_files(audio_dir: Path, extensions: Optional[Set[str]] = None) -> List[Path]:
    """
    Find all audio files in the directory and display them.

    Args:
        audio_dir: Directory to search for audio files
        extensions: Set of audio file extensions to look for.
                    If None, uses default extensions.

    Returns:
        List of found audio file paths

    Raises:
        SystemExit: If no audio files are found
    """
    # Default audio extensions if not provided
    if extensions is None:
        extensions = {'.mp3', '.wav', '.flac', '.m4a', '.aac', '.ogg'}

    audio_files = []

    # Find all files with specified extensions
    for ext in extensions:
        audio_files.extend(list(audio_dir.glob(f"*{ext}")))

    # If no files found, show what's in the directory and exit
    if not audio_files:
        print(f"No audio files found in directory")
        print(f"Available files in {audio_dir}:")
        for item in audio_dir.iterdir():
            print(f"  - {item.name}")
        sys.exit(1)

    # Display found files
    print(f"\nFound {len(audio_files)} audio files:")
    for i, file in enumerate(audio_files, 1):
        size_mb = file.stat().st_size / (1024 * 1024)
        print(f"{i:3d}. {file.name} ({size_mb:.1f} MB)")

    print("=" * 60)

    return audio_files

def create_all_transcripts_file(results: List[Dict], output_dir: Path) -> Path:
    """
    Create a file containing all transcriptions in one place.

    Args:
        results: List of transcription results (each containing 'success', 'file', 'text')
        output_dir: Directory where the summary file will be saved

    Returns:
        Path to the created summary file
    """
    all_texts_file = output_dir / "ALL_TRANSCRIPTS.txt"

    with open(all_texts_file, 'w', encoding='utf-8') as f:
        f.write("=" * 60 + "\n")
        f.write("SUMMARY OF ALL TRANSCRIPTIONS\n")
        f.write("=" * 60 + "\n\n")

        for result in results:
            if result['success']:
                # f.write(f"\n\n{'='*40}\n")
                # f.write(f"FILE: {result['file']}\n")
                # f.write(f"{'='*40}\n")
                f.write(format_transcript_text(result['text'] + "\n"))

    return all_texts_file