package ch03.p1ChatClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ChatClient {

    // Top free model with provider suffix for speed (free tier)
    private static final String MODEL = "Qwen/Qwen2.5-7B-Instruct:fastest";  // ~GPT-3.5 level, instant on free
    // private static final String MODEL = "meta-llama/Llama-3.3-70B-Instruct:cheapest";  // Better quality, low cost

    // Official free Router endpoint (OpenAI-compatible, November 2025)
    private static final String ENDPOINT = "https://router.huggingface.co/v1/chat/completions";

    public static void main(String[] args) throws IOException {
        String content = Files.readString(Path.of("src/main/java/ch03/p1ChatClient/Listing 3-5.txt"));
        // Read token from environment (free tier: ~1000 req/hour)
        String hfToken = System.getenv("HF_TOKEN");
        if (hfToken == null || hfToken.isEmpty()) {
            System.err.println("ERROR: HF_TOKEN not set. Run: set HF_TOKEN=hf_your_token");
            return;
        }

        // Exact prompt from the book (system + user messages)
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", "You are a helpful assistant."));
        messages.add(new Message("user",
                "Summarize this conversation and explain it to me like I'm a manager with little technical experience " +
                content));

        // Build OpenAI-compatible request (router handles messages natively)
        ChatRequest requestBody
                = new ChatRequest(MODEL, messages, 0.0f, 1000, 0.1f, 1, 1);

        String jsonInput;
        try {
            jsonInput = new ObjectMapper().writeValueAsString(requestBody);
            System.out.println("Sending request:\n" + jsonInput + "\n");
        } catch (JsonProcessingException e) {
            System.err.println("Failed to serialize request to JSON: " + e.getMessage());
            return;
        }

        try {
            URI uri = URI.create(ENDPOINT);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + hfToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Send JSON payload
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            StringBuilder response = new StringBuilder();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode == 200 ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
            }

            System.out.println("Response from " + MODEL + ":\n");

            if (responseCode == 200) {
                // Parse OpenAI-style: {"choices": [{"message": {"content": "..."}}]}
                JsonNode jsonResponse = new ObjectMapper().readTree(response.toString());
                String answer = jsonResponse.get("choices").get(0).get("message").get("content").asText();
                System.out.println(answer);
            } else {
                System.out.println("HTTP Error: " + responseCode);
                System.out.println(response);
            }

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    // === OpenAI-Compatible Classes (Direct from Book) ===
    static class ChatRequest {
        @JsonProperty("model")
        private final String model;

        @JsonProperty("messages")
        private final List<Message> messages;

        @JsonProperty("temperature")
        private final float temperature;

        @JsonProperty("max_tokens")
        private final int max_tokens;

        @JsonProperty("top_p")
        private final float top_p;

        @JsonProperty("frequency_penalty")
        private final int frequency_penalty;

        @JsonProperty("presence_penalty")
        private final int presence_penalty;

        public ChatRequest(String model, List<Message> messages, float temperature, int max_tokens,
                           float top_p, int frequency_penalty, int presence_penalty) {
            this.model = model;
            this.messages = messages;
            this.temperature = temperature;
            this.max_tokens = max_tokens;
            this.top_p = top_p;
            this.frequency_penalty = frequency_penalty;
            this.presence_penalty = presence_penalty;
        }
    }

    static class Message {
        @JsonProperty("role")
        private final String role;

        @JsonProperty("content")
        private final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}