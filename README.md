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