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
You: Привет бот
Bot: Hi, User! You told: Привет бот
You: How are you?
Bot: Hi, User! You told: How are you?
Required dependencies (already in build.gradle)
gradleimplementation 'org.telegram:telegrambots-longpolling:7.10.0'
implementation 'org.slf4j:slf4j-simple:2.0.13'
Code highlights

Uses modern Long Polling (no webhooks, no server needed)
try-with-resources + Thread.join() keeps bot alive forever
Proper exception handling with SLF4J
Ready to extend: just replace the reply text with LLM call, database, etc.

One file. Zero config files. Works out of the box.