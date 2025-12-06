"""
Statistics report generation and console output for transcription processing.
"""

import time
from pathlib import Path
from typing import List, Dict, Optional


class StatisticsReport:
    """
    A class for generating and managing transcription processing statistics.
    Provides the same interface as the original functions but encapsulated in a class.
    """

    def __init__(
            self,
            results: List[Dict],
            audio_files: List[Path],
            total_time: float,
            model_id: str,
            device: str,
            output_dir: Path,
            all_texts_file: Optional[Path] = None
    ):
        """
        Initialize the statistics report with processing data.

        Args:
            results: List of transcription results
            audio_files: List of input audio file paths
            total_time: Total processing time in seconds
            model_id: Model identifier
            device: Processing device used
            output_dir: Directory for output files
            all_texts_file: Optional path to summary file
        """
        self.results = results
        self.audio_files = audio_files
        self.total_time = total_time
        self.model_id = model_id
        self.device = device
        self.output_dir = output_dir
        self.all_texts_file = all_texts_file

        # Compute statistics
        self.successful = sum(1 for r in results if r['success'])
        self.failed = len(results) - self.successful
        self.stats_file = None

    def create_statistics_report(self) -> Path:
        """
        Creates a statistics report file with processing details.
        Equivalent to the original create_statistics_report() function.

        Returns:
            Path to the created statistics file
        """

        # Create statistics file
        self.stats_file = self.output_dir / "PROCESSING_STATS.txt"

        with open(self.stats_file, 'w', encoding='utf-8') as f:
            f.write("PROCESSING STATISTICS\n")
            f.write("=" * 40 + "\n")
            f.write(f"Processing date: {time.strftime('%Y-%m-%d %H:%M:%S')}\n")
            f.write(f"Total files: {len(self.audio_files)}\n")
            f.write(f"Successfully processed: {self.successful}\n")
            f.write(f"Failed: {self.failed}\n")
            f.write(f"Total processing time: {self.total_time:.2f} sec\n")
            f.write(f"Average time per file: {self.total_time / max(1, self.successful):.2f} sec\n")
            f.write(f"Model: {self.model_id}\n")
            f.write(f"Device: {self.device}\n\n")

            if self.successful > 0:
                f.write("SUCCESSFULLY PROCESSED FILES:\n")
                f.write("-" * 40 + "\n")
                for result in self.results:
                    if result['success']:
                        f.write(f"- {result['file']}\n")
                        f.write(f"  Time: {result['total_time']:.2f} sec, ")
                        f.write(f"Characters: {len(result['text'])}\n")
                        f.write(f"  Output file: {result['output_file'].name}\n")

            if self.failed > 0:
                f.write("\nFAILED FILES:\n")
                f.write("-" * 40 + "\n")
                for result in self.results:
                    if not result['success']:
                        f.write(f"- {result['file']}: {result.get('error', 'Unknown error')}\n")

        return self.stats_file

    def print_processing_summary(self) -> None:
        """
        Prints a summary of processing results to console.
        Equivalent to the original print_processing_summary() function.
        """
        print(f"\n{'='*60}")
        print("PROCESSING COMPLETED!")
        print("=" * 60)
        print(f"Total files: {len(self.audio_files)}")
        print(f"Successful: {self.successful}")
        print(f"Failed: {self.failed}")
        print(f"Total time: {self.total_time:.2f} seconds")
        print(f"\nResults saved to: {self.output_dir}")

        if self.all_texts_file:
            print(f"Summary file: {self.all_texts_file.name}")

        if self.stats_file:
            print(f"Statistics: {self.stats_file.name}")

        print("=" * 60)

    def print_file_list(self) -> None:
        """
        Prints list of created transcription files with their sizes.
        Equivalent to the original print_file_list() function.
        """
        print("\nCreated transcription files:")
        print("-" * 40)

        transcript_files = list(self.output_dir.glob("*_transcript.txt"))

        if not transcript_files:
            print("No transcription files found.")
            return

        for txt_file in transcript_files:
            size_kb = txt_file.stat().st_size / 1024
            print(f"  - {txt_file.name} ({size_kb:.1f} KB)")

        print(f"Total transcription files: {len(transcript_files)}")
