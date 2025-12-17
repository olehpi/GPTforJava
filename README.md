## Chapter 3
### ChatClient– Simple Java example of calling LLMs via Hugging Face Router (OpenAI-compatible) 
#### [ch03/p1ChatClient/ChatClient.java]
This standalone Java class shows a clean, fully working way to query large language models on Hugging Face completely for free (as of November 2025) using the official OpenAI-compatible router endpoint.

### What the program does
1. Reads a conversation transcript (file `Listing 3-5.txt` from Chapter 3 of the book).
2. Builds a two-message prompt:
    - System: "You are a helpful assistant."
    - User: "Summarize this conversation and explain it to me like I'm a manager with little technical experience" + the entire transcript.
3. Sends the request to the free Hugging Face router:  
   `https://router.huggingface.co/v1/chat/completions`
4. Prints the model’s answer to the console in clean text form.

### Default model

```
Qwen/Qwen2.5-7B-Instruct:fastest
```

How to run

Get a free Hugging Face access token: https://huggingface.co/settings/tokens (role read is enough)
Set the environment variable and run:
```
%set HF_TOKEN=hf_твой_токен_с_huggingface.co/settings/tokens  
%gradlew ChatClientCh3
```

### Slack Channel Reader – Simple Java Bot
#### [ch03/p2Slack/ChannelReaderSlackBot.java]

A minimal, standalone Java program that **fetches and beautifully prints** all messages from a public or private Slack channel within a specified date range — using the official Slack Bolt SDK (via `slack-api-client`).

Perfect for:
- Exporting meeting notes
- Building datasets from team discussions
- Quick channel audits or backups
- Learning Slack API in Java

### What it does

1. Connects to your Slack workspace using a **Bot User OAuth Token**
2. Pulls all messages from a given channel between two dates (UTC)
3. For each message:
   - Resolves the real username (not just `U123ABC`)
   - Converts Slack timestamp (`1732790423.123456`) → human-readable date/time
   - Prints:  
     `User: john.doe`  
     `Timestamp: 2025-11-27T14:27:03`  
     `Message: Hey team, let's ship this!`

Messages are displayed **in chronological order** (oldest first).

### How to run (step by step)

1. **Install a bot in your workspace**  
   → https://api.slack.com/apps → Create New App → "From scratch"  
   Give it a name like `all-ai-learning`

2. **Add required scopes** (under OAuth & Permissions):
   - `channels:history` (or `groups:history` / `im:history` depending on channel type)
   - `users:read`

3. **Install the app to your workspace** and copy the **Bot User OAuth Token** (`xoxb-...`)

4. Replace the token and channel ID in the code:

```
Slack LogiIn   https://slack.com/signin 
channel #all-ai-learning 
%set SLACK_BOT_TOKEN=xxxx-xxxxxxxxxxxxxx-xxxxxxxxxxxxxx-xxxxxxxxxxxxxxxxxxxxxxxxxxxx 
%set SLACK_CHANNEL_ID=xxxxxxxxxxxxxx 
%gradlew ChannelReaderSlackBot
```

Set the date range (UTC!):

JavaLocalDateTime startTimeUTC = LocalDateTime.of(2025, Month.NOVEMBER, 1, 0, 0);
LocalDateTime endTimeUTC = LocalDateTime.of(2025, Month.DECEMBER, 1, 0, 0);

Run the program → all messages will be printed to console

Security note
Never commit real tokens! In real projects:

Use environment variables: System.getenv("SLACK_BOT_TOKEN")
Or .properties / .env files (excluded via dotenv-java)

Example output
textUser: alice
Timestamp: 2025-11-27T10:23:41
Message: Morning everyone! Ready for standup?

User: bob.engineer
Timestamp: 2025-11-27T10:24:15
Message: LGTM! Merging the PR now
Lightweight, zero external services, works offline after token setup.
Great starting point for Slack bots, analytics tools, or AI training data collectors.

### Slack Bot – Send Message Example (`ChannelReaderSlackBotAnswer`)
#### [ch03/p2Slack/ChannelReaderSlackBotAnswer.java]

A clean, production-ready Java example that **posts a message to any Slack channel** using the official Slack SDK.

Perfect for:
- Testing your bot token
- Building notification services
- Learning Slack API + Java in 2025
- Starting point for AI-powered Slack bots

### Features (2025 best practices)

- **No hardcoded secrets** – token and channel loaded from environment variables
- **Fail-fast design** – stops immediately with clear instructions if config is missing
- **Git-safe** – you can commit this code without leaking tokens
- Uses latest Slack Java SDK (`slack-api-client`)

```
I bot 2025-11-28T15:47:22.123 Rocket
```

### How to run
```
Slack LogiIn   https://slack.com/signin 
channel #all-ai-learning 
%set SLACK_BOT_TOKEN=xxxx-xxxxxxxxxxxxxx-xxxxxxxxxxxxxx-xxxxxxxxxxxxxxxxxxxxxxxxxxxx 
%set SLACK_CHANNEL_ID=xxxxxxxxxxxxxx 
%gradlew ChannelReaderSlackBotAnswer
```
Required Bot Scopes
Make sure your bot has this scope:

chat:write

Add it at: https://api.slack.com/apps → Your App → OAuth & Permissions → Scopes
Expected output
```
textMessage successfully sent to the channel!
And in Slack you’ll see:
```
```
text@YourBot
I bot 2025-11-28T15:47:22 Rocket
Ready to evolve into a full AI summarizer, auto-responder, or monitoring bot.
```

### Telegram Echo Bot – Clean Java 2025 Example (`TelegramBotSpeakWithUser`)
#### [ch03/p3Telegram/TelegramBotSpeakWithUser.java]
A minimal, production-ready Telegram bot written in pure Java that **replies to every message** with:  
`Hi, User! You told: <your text>`

Perfect for:
- Testing your Telegram Bot Token
- Learning the official TelegramBots 7.x library (Long Polling)
- Starting point for AI assistants, notifiers, or customer support bots

### Features (2025 best practices applied)

- **Zero hardcoded secrets** – token loaded from environment variable via `Utils.getRequiredEnv()`
- **Fail-fast startup** – stops immediately with clear message if `TELEGRAM_BOT_TOKEN` is missing
- **SLF4J logging** – no `printStackTrace()`, beautiful console output
- **Graceful shutdown** – works forever until Ctrl+C
- **Git-safe** – you can commit this code today without leaking tokens

### How to run

#### 1. Create a bot (if you don’t have one)
Talk to @BotFather → `/newbot` → get your token like  
`7234567890:AAHxxxxxxxxxxxxxxxxxxxxxxxxxxxx

#### 2. Run (Windows)
```cmd
set TELEGRAM_BOT_TOKEN=7234567890:AAHxxxxxxxxxxxxxxxxxxxxxxxxxxxx
gradlew runTelegramBot        # or your task name
```
3. Run  
 You will see:
text2025-11-28 19:15:23.456 INFO  TelegramBotSpeakWithUser - Bot started!
Write anything to your bot in Telegram → instant reply!
Example conversation
You: Hi my bot!
Bot: Hi, User! You told: Hi my bot!
You: How are you?
Bot: Hi, User! You told: How are you?
Required dependencies (already in build.gradle)
gradle
implementation 'org.telegram:telegrambots-longpolling:7.10.0'
implementation 'org.slf4j:slf4j-simple:2.0.13'
Code highlights

Uses modern Long Polling (no webhooks, no server needed)
try-with-resources + Thread.join() keeps bot alive forever
Proper exception handling with SLF4J
Ready to extend: just replace the reply text with LLM call, database, etc.

One file. Zero config files. Works out of the box.

### Telegram → Private Channel Forwarder Bot (Java + Hugging Face ready)
#### [ch03/p3Telegram/TelegramChannelBotAddMessageToPrivateChannelById.java]

A lightweight, production-ready Telegram bot written in **pure Java** that:

- Listens to messages from users in private chats (or groups)
- Echoes a reply back to the user
- **Forwards every incoming message into your private Telegram channel** (even if the channel is hidden/invite-only)

Perfect for:
- Logging all user interactions
- Building announcement channels
- Creating audit trails
- Feeding messages into AI pipelines (e.g. send to Hugging Face / LLM later)

Built with the official **Telegram Bots Java SDK** (2025 version) using **OkHttp** and **SLF4J**.

## Features

- Works with **private channels** (`-100xxxxxxxxxx` IDs)
- No webhook setup — uses reliable long polling
- Secure: Bot token and channel ID loaded from environment variables
- Zero external services required
- Easily extendable (add AI answers, filters, commands, etc.)

## How to Get Your Private Channel ID (2025 method)

Private channel IDs are hidden in the app. Follow this once:

1. Add your bot as **administrator** to the private channel
2. Send any message in the channel (you or anyone)
3. Open in browser:  
   `https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getUpdates`
4. Find the `chat.id` under `channel_post` → it will look like `-1001234567890`
5. Use that number as `TELEGRAM_CHANNEL_ID`

## Setup & Run

### 1. Create a bot
Talk to [@BotFather](https://t.me/BotFather), create a bot → get the token.

### 2. Set environment variables (Windows example)

```cmd
set TELEGRAM_BOT_TOKEN=800000000:AAF-iIp1Og7MIXgkIToааааtvgnlFPqpfEg
set TELEGRAM_CHANNEL_ID=-1001234500000
set HF_TOKEN=hf_xxx  :: optional, for future AI features
3. Run with Gradle
gradlew run               :: starts the Telegram bot (default main class)
:: or explicitly:
gradlew TelegramChannelBotAddMessageToPrivateChannelById run
The bot will start and stay online indefinitely.
Try it!

Message your bot in Telegram → "Hello"
Bot replies: "You wrote: Hello"
Check your private channel → you'll see:Message from User: Hello

Done! All user messages are now logged in your private channel.
```

### Telegram → bot that automatically posts messages to your **private Telegram channel** (-100xxxxxx) with zero external services.
#### [ch03/p3Telegram/TelegramChannelBotAddMessageToPrivateChannelById.java]


#### What it does
- On startup → immediately sends a welcome message to your private channel
- Every N seconds → sends a recurring message (fully configurable)
- When any user writes to the bot → replies to the user **and** forwards the exact message to your private channel
- Perfect for logging, auto-announcements, reminders, habit trackers, or feeding messages into an AI pipeline

#### Features
- Works with **private channels** (no public link needed)
- Scheduled messages using `java.util.Timer`
- Echo-reply to users + forwarding everything to the channel
- All secrets loaded from environment variables (GitHub-safe)
- Professional SLF4J logging (no `System.out.println` in production code)
- Long-polling (no webhook server required)

#### Quick Start (2 minutes)

1. **Create a bot**  
   Talk to [@BotFather](https://t.me/BotFather) → `/newbot` → copy the token

2. **Get your private channel ID** (starts with `-100`)
    - Add the bot as administrator to the channel
    - Send any message in the channel
    - Open `https://api.telegram.org/bot<YOUR_TOKEN>/getUpdates`
    - Copy the `chat.id` value (e.g. `-1001789456123`)

3. **Set environment variables**
```cmd
# Windows
set TELEGRAM_BOT_TOKEN=123456789:AAFxxxxxxxxxxxxxxxxxxxxxxxxxxxx
set TELEGRAM_CHANNEL_ID=-1001789456123
```

### Audio Splitter – Precise MP3 Segmenter for Whisper / LLM Pipelines
#### [ch04/AudioSplitter.java]
This Java utility splits a long podcast or audio file (MP3) into fixed-length segments (default: 29 seconds) while preserving perfect audio continuity and quality — specifically designed for downstream processing with OpenAI Whisper, local transcription models, or RAG/LLM pipelines.

#### Why this splitter exists
Most simple audio splitters (like `ffmpeg -segment_time`) introduce small gaps, overlaps, or re-encoding artifacts that confuse Whisper's VAD (voice activity detection) and cause:
- Misaligned timestamps
- Repeated or skipped words at boundaries
- Poor context flow when feeding chunks to LLMs

This tool uses **JavaCV (FFmpegFrameGrabber + FFmpegFrameRecorder)** to perform frame-accurate, re-encode-once splitting directly from the decoded audio stream — resulting in perfectly clean, gapless segments that Whisper transcribes flawlessly end-to-end.

#### Features
- Frame-accurate cutting (no gaps, no overlaps, no silence injection)
- Preserves original sample rate, bitrate, and stereo channels
- Outputs high-quality MP3 segments using `libmp3lame`
- Optimized for the famous This American Life episode #811 — "The One Place I Can't Go" (but works with any MP3)
- Ideal segment length: ~29 seconds → fits comfortably under most Whisper context limits while maintaining narrative flow

#### Input & Output
Download the audio in the folder wsrc/main/resources/ch04/source_TheOnePlaceICantGo/: https://www.thisamericanlife.org/811/the-one-place-i-cant-go
Input:  src/main/resources/ch04/source_TheOnePlaceICantGo/811.mp3
→ Splits into:
src/main/resources/ch04/target_TheOnePlaceICantGo/
├── segment_1.mp3
├── segment_2.mp3
├── segment_3.mp3
└── ...
textEach segment is exactly 29 seconds (last one may be shorter).

#### Perfect companion for
- Local Whisper (faster-whisper, whisper.cpp, Insanely Fast Whisper)
- RAG pipelines over long-form podcasts
- Dataset creation for fine-tuning speech models

Just run `AudioSplitter.main()` → get clean chunks → feed to your `docker compose` Whisper pipeline → get perfect full transcript.

#### How to run
Go to GPTforJava\go.bat
go ch04.AudioSplitter

### Whisper Transcription Script (Chapter 04)
**Location:** `src/main/java/ch04/whisper/bat/`

#### What it does
Automatically transcribes **all** `.mp3` files located in  
`src/main/resources/ch04/target_TheOnePlaceICantGo/`  
and saves clean, timestamp-free text into  
`src/main/resources/ch04/target_TheOnePlaceICantGo/texts/`

#### Output files (created inside `texts/` folder)
- `segment_1_output.txt`, `segment_2_output.txt`, … – one transcript per audio segment
- `full_transcript.txt` – complete merged transcript with a blank line between each segment

#### How to run
1. Make sure **Docker Desktop** is running
2. The required image `nosana/whisper:latest` must already be present locally  
   (it was downloaded the first time you ever used this script – no further downloads needed)
3. Simply **double-click** `run_whisper.bat`  
   or run it from a terminal:

```bat
cd src\main\java\ch04\whisper\bat
run_whisper.bat
```

### Whisper Transcription Service
**File:** `ch04/whisper-compose.yml`

```yaml
services:
  whisper:
    image: nosana/whisper:latest
    container_name: whisper_transcriber
    volumes:
      - ./resources/ch04/target_TheOnePlaceICantGo:/audio
    command: >
      bash -c '
        mkdir -p /audio/texts &&
        rm -f /audio/texts/full_transcript.txt &&
        for f in /audio/*.mp3; do
          name=$(basename "$f")
          base=${name%.mp3}
          echo "Processing $name ..."

          python openai-whisper.py -p /audio/$name -o /dev/null 2>/dev/null |
            sed -E "s/\$$ ([0-9]{2}:){2}[0-9]{3} --> ([0-9]{2}:){2}[0-9]{3}\ $$[[:space:]]*//g" \
            > /audio/texts/${base}_output.txt

          cat /audio/texts/${base}_output.txt >> /audio/texts/full_transcript.txt
          echo "" >> /audio/texts/full_transcript.txt
        done
        echo "Done!"
      '
```      
#### What it does
Automatically transcribes all .mp3 files located in
resources/ch04/target_TheOnePlaceICantGo using OpenAI Whisper inside a Docker container.
#### Output
When finished, you’ll find inside resources/ch04/target_TheOnePlaceICantGo:
texttexts/
├── some_segment_output.txt
├── another_part_output.txt
├── …
└── full_transcript.txt   # complete concatenated transcript
How to run (exactly the command you use)
From the project root (where whisper-compose.yml lives):

#### Run
docker compose -f whisper-compose.yml up

Or simply (Docker Compose v2+ automatically finds the file if it has the standard name):
Bashdocker compose up whisper
Just drop your .mp3 files into resources/ch04/target_TheOnePlaceICantGo, run the command, wait for the Done! message in the logs, and your clean text transcripts will be ready. No extra flags or paths needed!

### Whisper Large v3 – Production-Ready Speech-to-Text Transcription
#### [src/main/java/ch04/whisper_large_v3]
A clean, fully-featured, ready-to-use Python project for transcribing any number of audio files with OpenAI’s **whisper-large-v3** (the best open-source ASR model as of 2025).

Works both locally (Windows) and inside an optimized Docker container (CPU-only, ~3 GB image).

### Features
- Zero-hassle model loading (`low_cpu_mem_usage=True`, `safetensors`, float16 on GPU)
- Full configuration via JSON with validation
- Detailed per-file metadata, statistics and processing report
- Nicely formatted transcripts (sentence splitting, optional line wrapping, paragraph breaks)
- One-click Windows batch script (`run-my-audio.bat`)
- All transcripts + single consolidated `ALL_TRANSCRIPTS.txt`
- Smart Hugging Face cache redirection (`E:/cach/huggingface`) – never fills your C: drive
- Production-grade Docker image (python:3.12-slim + pre-bundled model + non-root user)

### Project structure
whisper_large_v3/
├── src/
│   └── whisper_app/
│       ├── main.py                  # Entry point
│       ├── config.py                # JSON config loading + validation
│       ├── hf_cache.py              # Moves HF cache to E:/cach/huggingface (local dev only)
│       ├── input_output_manager.py  # Audio discovery & summary file creation
│       ├── statistics_reports.py    # Stats generation
│       └── text_formatter.py        # Beautiful transcript formatting
├── audio/                               # ← drop your .mp3/.wav/.flac files here
├── output/transcripts/                  # ← results appear here
├── config.json                          # Default config (used in Docker)
├── config-local.json                    # Local development override
├── run-my-audio.bat                     # One-click Windows launcher
├── Dockerfile                           # Optimized CPU image with baked-in model
├── requirements.txt
└── .dockerignore
text### Quick start (Windows local)

1. Install dependencies once:
   ```bash
   pip install -r requirements.txt

Put your audio files into
E:\A\**GPTforJava**\src\main\resources\ch04\target_TheOnePlaceICantGo
Double-click run-my-audio.bat or run:Bashpython src/whisper_app/main.py --config config-local.json

Docker (recommended for clean environment)
Bash# Build (only the first time)
docker build -t whisper-v3 .

# Run on your audio folder (one-click batch file does exactly this)
docker run --rm --user root ^
-v "E:\A\ai\whisper_large_v3\config.json:/app/config.json" ^
-v "E:\A\ai\GPTforJava\src\main\resources\ch04\target_TheOnePlaceICantGo:/app/audio" ^
whisper-v3
Transcripts + ALL_TRANSCRIPTS.txt + PROCESSING_STATS.txt will appear in …\target_TheOnePlaceICantGo\transcripts\.
Customization
Edit config.json or config-local.json:
JSON{
"audio_dir": "/app/audio",
"model_id": "openai/whisper-large-v3",
"generate_kwargs": {
"language": "en",      // null = auto-detect
"task": "transcribe",  // or "translate" (to English)
"temperature": 0.1,
"num_beams": 3
}
}

### Hugging Face Whisper Client (Java + OkHttp)
#### [ch04/HFRouterWhisperClient.java]


A set of audio utilities and Hugging Face / Whisper integrations: Java clients and utilities for preparing, splitting and batch-transcribing audio files, plus bot examples (Slack / Telegram). Supports local and cloud scenarios (Docker, Hugging Face Inference Router).

#### Key features
Sends raw MP3 bytes (Content-Type: audio/mpeg) — exactly as required by the current HF router
Handles all known response formats ("text", "generated_text", array format)
Automatic 15-second delay between requests (respects free-tier rate limits)
Enforces 25 MB file size limit
Saves individual .txt transcriptions + one merged combined_transcription.txt
Word-wrapping at 120 characters in the combined file for easy reading
Skips already-transcribed files
Cleans and recreates the output folder on every run
Reads input/output paths from a JSON config file
Full SLF4J logging

#### Requirements
Java 11+
HF_TOKEN environment variable set with your Hugging Face access token
MP3 files placed in the specified folder
Maven/Gradle (dependencies listed below)

#### Quick start
set HF_TOKEN=hf_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
go ch04.HFRouterWhisperClient E:\A\ai\GPTforJava\src\main\resources\ch04\hr_router_whisper_client_config.json
ch04/hr_router_whisper_client_config.json
{
   "audio_dir": "src/main/resources/ch04/target_TheOnePlaceICantGo/",
   "output_dir": "src/main/resources/ch04/target_TheOnePlaceICantGo/transcripts/",
   "options": {"wait_for_model": true }
}

#### Security
- Never commit real tokens. Use `HF_TOKEN` via environment variables or a secret manager.


### Text to Image
#### [ch04\whisper_large_v3\src\whisper_app\text_to_image.py.java]

Short description
- This script generates an image using Stable Diffusion (v1.5) via the `diffusers` library.
- It downloads or loads the model from the Hugging Face cache and produces an image from a text prompt.

Dependencies
- Python 3.8+
- `torch`
- `diffusers`
- `transformers` (if required by your `diffusers` pipeline)
- `accelerate` (optional, for optimized inference)

Environment variables (Windows)
- `HF_HOME` — root of the Hugging Face cache (example):
  `set HF_HOME=E:\cach\huggingface`
- `HF_HUB_CACHE` — model cache folder inside `HF_HOME` (example):
  `set HF_HUB_CACHE=E:\cach\huggingface\hub`
- (Optional) `HF_TOKEN` — if accessing private models

How to run
1. Install dependencies:
   `pip install torch diffusers transformers`
2. Set required environment variables (see above).
3. Run the script:
   `python E:\A\ai\GPTforJava\src\main\java\ch04\whisper_large_v3\src\whisper_app\text_to_image.py`

Configurable parameters (inside the script)
- `prompt` — the text prompt to generate the image
- `negative_prompt` — negative prompt to reduce unwanted features
- `num_inference_steps`, `guidance_scale` — sampling options
- `width`, `height` — output image size
- `seed` / `generator.manual_seed(...)` — deterministic output
- `output_file` — path where the generated image is saved

Notes
- The script caches models in the local Hugging Face cache to avoid repeated downloads.
- Adjust `torch` device settings (CPU / CUDA) depending on your hardware.
- For large models and fast results, run on a GPU with sufficient VRAM or use a lightweight checkpoint.
