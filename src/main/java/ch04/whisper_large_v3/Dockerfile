# ------------------------------------------------------------
# BASE IMAGE (Python 3.12)
# ------------------------------------------------------------
FROM python:3.12-slim

LABEL maintainer="olehpi <pihnastyi@gmail.com>"
LABEL description="Optimized Whisper Large v3 Transcription Service"
LABEL version="2.0"

WORKDIR /app

# ------------------------------------------------------------
# Install system dependencies
# ------------------------------------------------------------
RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg \
    libsndfile1 \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# ------------------------------------------------------------
# Install PyTorch CPU-only (critical optimization)
# ------------------------------------------------------------
RUN pip install --no-cache-dir torch torchaudio --index-url https://download.pytorch.org/whl/cpu

# ------------------------------------------------------------
# Install other Python dependencies
# ------------------------------------------------------------
COPY requirements.txt .
RUN pip install --no-cache-dir --default-timeout=100 -r requirements.txt

# ------------------------------------------------------------
# Copy only source code (IMPORTANT: no whole project)
# ------------------------------------------------------------
COPY src/ src/
COPY config.json .

# ------------------------------------------------------------
# Copy only the Whisper model snapshot
# Your structure:
# docker-cache/huggingface/hub/models--openai--whisper-large-v3/...
# ------------------------------------------------------------
COPY docker-cache/huggingface/hub/models--openai--whisper-large-v3 \
     /app/.cache/huggingface/hub/models--openai--whisper-large-v3

# ------------------------------------------------------------
# HuggingFace env vars
# ------------------------------------------------------------
ENV HF_HOME=/app/.cache/huggingface \
    HF_HUB_CACHE=/app/.cache/huggingface/hub \
    TRANSFORMERS_CACHE=/app/.cache/huggingface

# ------------------------------------------------------------
# Create unprivileged user
# ------------------------------------------------------------
RUN useradd -m -u 1000 whisperuser && \
    chown -R whisperuser:whisperuser /app
USER whisperuser

# ------------------------------------------------------------
# Final environment vars
# ------------------------------------------------------------
ENV PYTHONPATH=/app/src \
    PYTHONUNBUFFERED=1 \
    TF_CPP_MIN_LOG_LEVEL=2

VOLUME ["/app/audio"]

# ------------------------------------------------------------
# ENTRYPOINT
# ------------------------------------------------------------
ENTRYPOINT ["python", "src/whisper_app/main.py"]
CMD ["--config", "/app/config.json"]
