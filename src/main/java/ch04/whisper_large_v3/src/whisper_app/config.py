# docker run --rm --user root -v "E:\A\ai\whisper_large_v3\config.json:/app/config.json" -v "E:\A\ai\GPTforJava\src\main\resources\ch04\target_TheOnePlaceICantGo:/app/audio" whisper-v3
# local development: python main.py --config ..\..\config-local.json

"""
Configuration management for Whisper transcription.
Provides validation and loading from JSON.
"""

import json
from pathlib import Path
from typing import Dict, Any
from dataclasses import dataclass, asdict, field
import argparse


@dataclass
class GenerateKwargs:
    """Generate parameters for Whisper pipeline with validation."""
    task: str = "transcribe"
    language: str = "en"
    temperature: float = 0.0
    num_beams: int = 5

    def __post_init__(self):
        """Validate generate kwargs."""
        if self.task not in ["transcribe", "translate"]:
            raise ValueError(f"Invalid task: {self.task}. Must be 'transcribe' or 'translate'")

        """Validate generate kwargs."""
        if self.task not in ["transcribe", "translate"]:
            raise ValueError(f"Invalid task: {self.task}. Must be 'transcribe' or 'translate'")

        # Temperature validation
        if self.temperature < 0.0:
            raise ValueError(f"Temperature must be >= 0.0, got {self.temperature}")

        # Provide guidance based on temperature value
        if self.temperature == 0.0:
            print("  Using greedy decoding (temperature=0.0)")
        elif 0.0 < self.temperature <= 0.5:
            print(f"  Using low temperature sampling ({self.temperature})")
        elif 0.5 < self.temperature <= 1.0:
            print(f" ️  Using moderate temperature sampling ({self.temperature})")
        else:
            print(f" ️  Using high temperature sampling ({self.temperature}) - results may be very random")

        # Beam search validation
        if self.num_beams < 1:
            raise ValueError(f"num_beams must be >= 1, got {self.num_beams}")

        if self.num_beams == 1:
            print("  Using greedy search (num_beams=1)")
        elif 2 <= self.num_beams <= 5:
            print(f"  Using beam search with {self.num_beams} beams")
        elif 6 <= self.num_beams <= 10:
            print(f" ️  Using large beam search ({self.num_beams} beams) - may be slower")
        else:
            print(f" ️  Using very large beam search ({self.num_beams} beams) - will be slow")


@dataclass
class FeatureExtractorKwargs:
    """Feature extractor parameters."""
    return_attention_mask: bool = True
    do_normalize: bool = True
    padding: bool = True


@dataclass
class WhisperConfig:
    """Configuration for Whisper transcription with validation."""

    # Paths
    audio_dir: str = "/app/audio"
    output_subdir: str = "transcripts"

    # Model settings
    model_id: str = "openai/whisper-large-v3"
    sampling_rate: int = 16000

    # Nested configurations
    generate_kwargs: GenerateKwargs = field(default_factory=GenerateKwargs)
    feature_extractor_kwargs: FeatureExtractorKwargs = field(default_factory=FeatureExtractorKwargs)

    def __post_init__(self):
        """Validate configuration."""
        # Validate sampling rate
        if self.sampling_rate not in [16000, 32000, 48000]:
            print(f" ️  Warning: Unusual sampling rate: {self.sampling_rate}. Recommended: 16000")

        # Validate model ID
        valid_models = [
            "openai/whisper-tiny", "openai/whisper-base", "openai/whisper-small",
            "openai/whisper-medium", "openai/whisper-large", "openai/whisper-large-v2",
            "openai/whisper-large-v3"
        ]
        if self.model_id not in valid_models:
            print(f" ️  Warning: Model {self.model_id} not in known Whisper models")

    @classmethod
    def from_json(cls, json_path: Path) -> 'WhisperConfig':
        """Load and validate configuration from JSON file."""
        if not json_path.exists():
            raise FileNotFoundError(f"Config file not found: {json_path}")

        with open(json_path, 'r', encoding='utf-8') as f:
            data = json.load(f)

        # Handle nested structures
        generate_data = data.pop('generate_kwargs', {})
        feature_data = data.pop('feature_extractor_kwargs', {})

        try:
            config = cls(**data)
            config.generate_kwargs = GenerateKwargs(**generate_data)
            config.feature_extractor_kwargs = FeatureExtractorKwargs(**feature_data)
        except TypeError as e:
            raise ValueError(f"Invalid config structure: {e}")

        return config

    @classmethod
    def load_config(cls) -> 'WhisperConfig':
        """Load configuration from command line arguments or JSON file."""
        parser = argparse.ArgumentParser(description="Whisper transcription")
        parser.add_argument("--config", type=Path,
                            help="Path to JSON config file (required)")

        # Можно добавить возможность переопределения отдельных параметров
        parser.add_argument("--language", type=str,
                            help="Override language from config")
        parser.add_argument("--task", type=str,
                            help="Override task from config")

        args = parser.parse_args()

        if not args.config:
            parser.error("--config argument is required")

        config = cls.from_json(args.config)

        # Override specific parameters if provided
        if args.language:
            config.generate_kwargs.language = args.language
        if args.task:
            config.generate_kwargs.task = args.task

        return config

    def to_dict(self) -> Dict[str, Any]:
        """Convert config to dictionary."""
        return asdict(self)

    def save_json(self, json_path: Path) -> None:
        """Save configuration to JSON file."""
        with open(json_path, 'w', encoding='utf-8') as f:
            json.dump(self.to_dict(), f, indent=2, ensure_ascii=False)

    @property
    def generate_kwargs_dict(self) -> Dict[str, Any]:
        """Get generate parameters as dictionary for pipeline."""
        return {
            "task": self.generate_kwargs.task,
            "language": self.generate_kwargs.language,
            "temperature": self.generate_kwargs.temperature,
            "num_beams": self.generate_kwargs.num_beams
        }

    @property
    def feature_extractor_kwargs_dict(self) -> Dict[str, Any]:
        """Get feature extractor parameters as dictionary."""
        return {
            "return_attention_mask": self.feature_extractor_kwargs.return_attention_mask,
            "do_normalize": self.feature_extractor_kwargs.do_normalize,
            "padding": self.feature_extractor_kwargs.padding
        }

    @property
    def audio_dir_path(self) -> Path:
        """Get audio directory as Path."""
        return Path(self.audio_dir)

    @property
    def output_dir(self) -> Path:
        """Get full output directory path."""
        return self.audio_dir_path / self.output_subdir