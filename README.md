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