package ch06.ollama;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
public class ModerationOllamaClient {
    private static final Logger logger = LoggerFactory.getLogger(ModerationOllamaClient.class);

    public boolean isFlagged(String userMessage) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            ChatRequest request = new ChatRequest(
                    Constants.MODEL,
                    List.of(
                            new Message("system", Constants.Moderator.SYSTEM_MESSAGE),
                            new Message("user", userMessage)
                    ),
                    Constants.Moderator.MAX_TOKENS
            );

            String json = mapper.writeValueAsString(request);

            HttpURLConnection conn = (HttpURLConnection) new URL(Constants.ENDPOINT).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
            }

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);

                JsonNode root = mapper.readTree(sb.toString());
                String result = root.at("/choices/0/message/content")
                        .asText()
                        .trim()
                        .toUpperCase();

                return result.contains("FLAG");
            }

        } catch (Exception e) {
            logger.error("Error processing message", e);
        }

        return true; // fail-closed
    }

    record Message(String role, String content) {}
    record ChatRequest(String model, List<Message> messages, int max_tokens) {}
}
