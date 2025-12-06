import hf_cache
hf_cache.setup_cache()

import torch
from transformers import AutoModelForSpeechSeq2Seq, AutoProcessor, pipeline
import librosa
import numpy as np
import time
import sys

from config import WhisperConfig
from input_output_manager import find_and_display_audio_files, create_all_transcripts_file
from statistics_reports import StatisticsReport
from text_formatter import format_transcript_text


def setup_model(config: WhisperConfig):

    device = "cuda:0" if torch.cuda.is_available() else "cpu"
    dtype = torch.float16 if torch.cuda.is_available() else torch.float32

    print("=" * 60)
    print(f"Loading {config.model_id} model...")
    print(f"Device: {device}, Precision: {dtype}")
    print("=" * 60)

    # Load the model with optimized settings
    model = AutoModelForSpeechSeq2Seq.from_pretrained(
        config.model_id,
        dtype=dtype,
        low_cpu_mem_usage=True,
        use_safetensors=True
    )
    model.to(device)
    print(f"Model loaded on device: {device}")

    # Load the processor using config
    processor = AutoProcessor.from_pretrained(
        config.model_id,
        feature_extractor_kwargs=config.feature_extractor_kwargs_dict
    )

    # Create the ASR pipeline using config
    pipe = pipeline(
        "automatic-speech-recognition",
        model=model,
        tokenizer=processor.tokenizer,
        feature_extractor=processor.feature_extractor,
        dtype=dtype,
        device=device,
        generate_kwargs=config.generate_kwargs_dict
    )

    print("Model ready for transcription")
    print("=" * 60)

    return pipe, device, config.model_id


def transcribe_audio_file(audio_path, file_index, total_files, pipe, model_id, device, output_dir, config):
    """Transcribe a single audio file and save the result"""
    try:
        print(f"\n[{file_index}/{total_files}] Processing: {audio_path.name}")
        print("-" * 40)

        # Load audio file using config sampling rate
        start_load = time.time()
        audio, sr = librosa.load(audio_path, sr=config.sampling_rate)
        audio = audio.astype(np.float32)
        load_time = time.time() - start_load

        duration = len(audio) / sr
        print(f"  Duration: {duration:.2f} seconds")
        print(f"  Load time: {load_time:.2f} sec")

        # Transcription
        print("  Transcribing...")
        start_transcribe = time.time()
        result = pipe(audio)
        transcribe_time = time.time() - start_transcribe

        text = result["text"]
        print(f"  Transcription time: {transcribe_time:.2f} sec")
        print(f"  Text length: {len(text)} characters")

        # Save the result
        output_file = output_dir / f"{audio_path.stem}_transcript.txt"

        # Create metadata header with config info
        metadata = f"""File: {audio_path.name}
Processing date: {time.strftime('%Y-%m-%d %H:%M:%S')}
Audio duration: {duration:.2f} seconds
File size: {audio_path.stat().st_size / (1024 * 1024):.2f} MB
Load time: {load_time:.2f} sec
Transcription time: {transcribe_time:.2f} sec
Model: {model_id}
Device: {device}
Language: {config.generate_kwargs.language}
Task: {config.generate_kwargs.task}
Temperature: {config.generate_kwargs.temperature}
Num beams: {config.generate_kwargs.num_beams}

{'=' * 50}
TRANSCRIPTION:
{'=' * 50}

"""

        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(metadata)
            f.write(format_transcript_text(text))

        print(f"    Result saved to: {output_file.name}")

        # Smart preview
        preview_length = min(200, len(text))
        if text:
            print(f"    Preview: {text[:preview_length]}{'...' if len(text) > preview_length else ''}")

        return {
            'success': True,
            'file': audio_path.name,
            'text': text,
            'output_file': output_file,
            'load_time': load_time,
            'transcribe_time': transcribe_time,
            'total_time': load_time + transcribe_time,
            'duration': duration
        }

    except Exception as e:
        print(f"    Error processing {audio_path.name}: {e}")
        import traceback
        traceback.print_exc()
        return {
            'success': False,
            'file': audio_path.name,
            'error': str(e)
        }


def main():
    """Main function."""
    try:
        # DownLoad config
        config = WhisperConfig.load_config()

        # create source folder
        config.output_dir.mkdir(parents=True, exist_ok=True)

        print( "  Configuration loaded:")
        print(f"  Audio directory: {config.audio_dir}")
        print(f"  Output directory: {config.output_dir}")
        print(f"  Model: {config.model_id}")
        print(f"  Language: {config.generate_kwargs.language}")
        print(f"  Task: {config.generate_kwargs.task}")
        print(f"  Temperature: {config.generate_kwargs.temperature}")
        print(f"  Num beams: {config.generate_kwargs.num_beams}")

        # set up model
        pipe, device, model_id = setup_model(config)

        # find audio files
        audio_files = find_and_display_audio_files(config.audio_dir_path)

        # process audio files
        print(f"\nStarting processing of {len(audio_files)} files...")
        print("=" * 60)

        results = []
        total_start_time = time.time()

        for i, audio_file in enumerate(audio_files, 1):
            result = transcribe_audio_file(
                audio_file,
                i,
                len(audio_files),
                pipe,
                model_id,
                device,
                config.output_dir,
                config
            )
            results.append(result)

            # Small pause between files (optional)
            if i < len(audio_files):
                time.sleep(0.1)

        total_time = time.time() - total_start_time

        # create summary text file
        print("\n" + "=" * 60)
        print("CREATING SUMMARY REPORT")
        print("=" * 60)

        all_texts_file = create_all_transcripts_file(results, config.output_dir)

        report = StatisticsReport(
            results=results,
            audio_files=audio_files,
            total_time=total_time,
            model_id=model_id,
            device=device,
            output_dir=config.output_dir,
            all_texts_file=all_texts_file
        )

        stats_file = report.create_statistics_report()
        report.print_processing_summary()
        report.print_file_list()

        # Additional statistics
        successful = sum(1 for r in results if r['success'])
        if successful > 0:
            total_duration = sum(r.get('duration', 0) for r in results if r['success'])
            print(f"\n Additional Statistics:")
            print(f"   Success rate: {successful}/{len(results)} ({successful/len(results)*100:.1f}%)")
            print(f"   Total audio duration: {total_duration:.2f} sec")
            print(f"   Real-time factor: {total_time/total_duration:.3f}x")

    except Exception as e:
        print(f"  Fatal error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()