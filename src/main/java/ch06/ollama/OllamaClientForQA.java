package ch06.ollama;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class OllamaClientForQA {
    private static final Logger logger = LoggerFactory.getLogger(OllamaClientForQA.class);
 //   private final String endpoint = "http://localhost:11434/v1/chat/completions"; // for go ch06.ollama.TechSupportOllamaBot Ollama local endpoint
    private final String endpoint = "http://ollama:11434/v1/chat/completions"; // for doker compose Ollama local endpoint
    private final String model = "llama3.2:1b"; // Your local model
    private final int max_tokens = 128;

    private final String systemMessage;
    private final String initialInstructions;

    public OllamaClientForQA(String systemMessage, String initialInstructions) {
        this.systemMessage = systemMessage;
        this.initialInstructions = initialInstructions;
    }

    public String sendMessage(String userMessage) {
        String answer = "";
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Build the JSON request with roles
            List<Message> messages = new ArrayList<>();
            messages.add(new Message("system", systemMessage));
            messages.add(new Message("user", initialInstructions));
            messages.add(new Message("user", userMessage));

            ChatRequest chatRequest = new ChatRequest(model, messages, max_tokens);
            String jsonInput = mapper.writeValueAsString(chatRequest);

            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonInput.getBytes());
                os.flush();
            }

            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JsonNode root = mapper.readTree(response.toString());
                answer = root.at("/choices/0/message/content").asText();
            } else {
                answer = "Error: " + code;
            }

            connection.disconnect();

        } catch (Exception e) {
            logger.error("Unexpected error while processing Ollama request", e);
            answer = "An unexpected error occurred while generating the response.";
        }
        return answer;
    }

    // Helper classes for JSON serialization
    static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    static class ChatRequest {
        public String model;
        public List<Message> messages;
        public int max_tokens;

        public ChatRequest(String model, List<Message> messages, int max_tokens) {
            this.model = model;
            this.messages = messages;
            this.max_tokens = max_tokens;
        }
    }
}
