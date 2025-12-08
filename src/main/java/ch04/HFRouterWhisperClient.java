package ch04;

import ch03.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Hugging Face Whisper Client for speech-to-text transcription
 * This client sends MP3 audio files to the Hugging Face Inference API
 * using the openai/whisper-large-v3 model for transcription.
 * Features:
 * - Supports MP3 audio files up to 25MB
 * - Uses raw audio bytes with Content-Type: audio/mpeg
 * - Implements rate limiting for free tier (45-second delays)
 * Prerequisites:
 * - Set HF_TOKEN environment variable with Hugging Face API token
 * - MP3 files should be placed in src/main/resources/ch04/target_TheOnePlaceICantGo/
 * Rate Limits:
 * - Free tier: ~1 request per minute
 * - Paid tiers: Check Hugging Face pricing
 * API Endpoint:
 * See: <a href="https://router.huggingface.co/hf-inference/models/openai/whisper-large-v3">
 * HuggingFace Whisper Model
 * </a>
 * Response Formats:
 * 1. {"text": "transcription text"}
 * 2. {"generated_text": "transcription text"}
 * 3. [{"text": "transcription text"}]
 */
public class HFRouterWhisperClient {
    private static final Logger log = LoggerFactory.getLogger(HFRouterWhisperClient.class);

    // Hugging Face API token from environment variable
    private final static String HF_TOKEN = Utils.getRequiredEnv("HF_TOKEN");

    // Hugging Face Inference API endpoint for Whisper model
    private final static String ENDPOINT =
            "https://router.huggingface.co/hf-inference/models/openai/whisper-large-v3";

    // HTTP client with appropriate timeouts for audio processing
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)      // Connection timeout
            .readTimeout(300, TimeUnit.SECONDS)        // 5-minute timeout for processing
            .build();

    // JSON parser for API responses
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Main entry point for audio transcription
     * Processes all MP3 files in the target directory, sending each to the
     * Whisper API for transcription and saving results as text files.
     * @param args Command line arguments (not used)
     * @throws Exception If any file or network error occurs
     */
    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            log.error("No config file path passed as argument!");
            return;
        }

        Path configPath = Paths.get(args[0]);
        if (!Files.exists(configPath)) {
            log.error("Config file not found: {}", configPath.toAbsolutePath());
            return;
        }
        JsonNode config = mapper.readTree(configPath.toFile());

        // Validate API token
        if (HF_TOKEN == null || HF_TOKEN.trim().isEmpty()) {
            log.error("HF_TOKEN environment variable not set!");
            log.error("Set it with: export HF_TOKEN=your_token_here");
            return;
        }

        String mp3FolderPath = config.get("audio_dir").asText();
        String outputFolderPath = config.get("output_dir").asText();

        // Ensure output folder exists: remove old, create new
        Path outputDir = Paths.get(outputFolderPath);

        if (Files.exists(outputDir)) {
            log.warn("Output directory exists → deleting: {}", outputDir.toAbsolutePath());

            // Recursively delete directory
            try (Stream<Path> paths = Files.walk(outputDir)) {
                paths
                        .sorted(Comparator.reverseOrder()) // delete children first
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                log.error("Failed to delete {}", p, e);
                            }
                        });
            }
        }

        Files.createDirectories(outputDir);

        log.info("Created clean output folder: {}", outputDir.toAbsolutePath());

        log.info("Using endpoint: {}", ENDPOINT);

        // Directory containing MP3 files to transcribe
        Path audioDir = Paths.get(mp3FolderPath);
        if (!Files.exists(audioDir)) {
            log.error("Directory not found: {}", audioDir.toAbsolutePath());
            return;
        }

        // Get all MP3 files sorted alphabetically
        List<Path> mp3Files;
        try (Stream<Path> stream = Files.list(audioDir)) {
            mp3Files = stream
                    .filter(p -> p.toString().endsWith(".mp3"))
                    .sorted()
                    .toList();
        }

        log.info("Found {} MP3 files for transcription", mp3Files.size());

        // Where to save combined result
        String combinedOutputPath = outputFolderPath + "combined_transcription.txt";
        int maxLineLength = 120; // symbol limit

        // Create/reset combined file
        Files.writeString(Path.of(combinedOutputPath), "");
        long lastRequestTime = System.currentTimeMillis();

        for (Path mp3 : mp3Files) {

            String txtOut = outputFolderPath + mp3.getFileName().toString().replace(".mp3", ".txt");

            // Skip if transcription already exists
            if (Files.exists(Path.of(txtOut))) {
                log.info("Skipping (already transcribed): {}", txtOut);
                continue;
            }

            log.info("Transcribing: {}", mp3.getFileName());
            String text = transcribe(mp3);

            long now = System.currentTimeMillis();
            long elapsed = now - lastRequestTime;
            long delay = 15000;
            long waitTime = delay-elapsed;
            if (waitTime>0) {
                log.info("Waiting {} ms before next request...", waitTime);
                // Rate limiting for free Hugging Face tier
                log.info("Waiting {} seconds before next request...", waitTime/1000);
                Thread.sleep(waitTime);
            }

            // Save transcription if successful
            if (text != null && !text.trim().isEmpty()) {

                Files.writeString(Path.of(txtOut), text.trim());
                log.info("Transcription saved → {}", txtOut);

                // Preview in console
                int previewTextLength = 150;
                String preview = text.length() > previewTextLength ? text.substring(0, previewTextLength) + "..." : text;
                log.info("Text preview: {}", preview);

                // Append to combined output
                appendWrappedText(combinedOutputPath, text, maxLineLength);

            } else {
                log.warn("Empty transcription for: {}", mp3.getFileName());
            }

            lastRequestTime = System.currentTimeMillis();
        }

        log.info("All transcriptions completed successfully!");
    }

    /**
     * Transcribes an MP3 file using Hugging Face Whisper API
     * Sends raw audio bytes with Content-Type: audio/mpeg to the API
     * and parses the JSON response to extract the transcription text.
     *
     * @param mp3 Path to the MP3 file
     * @return Transcription text, or null if failed
     */
    private static String transcribe(Path mp3) {
        try {
            byte[] audioBytes = Files.readAllBytes(mp3);
            String fileName = mp3.getFileName().toString();

            // Calculate file size in KB
            long fileSizeKB = Files.size(mp3) / 1024;
            log.info("File {}: {} KB", fileName, fileSizeKB);

            // Whisper API has 25MB limit
            if (fileSizeKB > 25000) {
                log.error("File too large ({} MB). Maximum 25MB", fileSizeKB / 1024);
                return null;
            }

            // Create request with raw audio bytes
            RequestBody body = RequestBody.create(
                    audioBytes,
                    MediaType.parse("audio/mpeg")
            );

            // Build HTTP request with authentication
            Request request = new Request.Builder()
                    .url(ENDPOINT)
                    .addHeader("Authorization", "Bearer " + HF_TOKEN)
                    .addHeader("Content-Type", "audio/mpeg")
                    .post(body)
                    .build();

            log.info("Sending {} ({} KB) to Whisper API...", fileName, fileSizeKB);

            // Execute API call
            try (Response response = client.newCall(request).execute()) {
                String responseStr = response.body().string();

                // Check for API errors
                if (!response.isSuccessful()) {
                    log.error("HF API error {} for {}: {}",
                            response.code(), fileName,
                            responseStr.length() > 200 ?
                                    responseStr.substring(0, 200) + "..." : responseStr);
                    return null;
                }

                // Parse JSON  response
                JsonNode jsonNode = mapper.readTree(responseStr);

                // Handle different response formats
                // Format 1: {"text": "transcription"}
                if (jsonNode.has("text")) {
                    return jsonNode.get("text").asText();
                }

                // Format 2: {"generated_text": "transcription"}
                if (jsonNode.has("generated_text")) {
                    return jsonNode.get("generated_text").asText();
                }

                // Format 3: [{"text": "transcription"}] (array format)
                if (jsonNode.isArray() && !jsonNode.isEmpty()) {
                    JsonNode first = jsonNode.get(0);
                    if (first.has("text")) {
                        return first.get("text").asText();
                    }
                    if (first.has("generated_text")) {
                        return first.get("generated_text").asText();
                    }
                }

                log.error("Unknown response format for {}", fileName);
                return null;

            } catch (Exception e) {
                log.error("API error for {}: {}", fileName, e.getMessage());
                return null;
            }

        } catch (Exception e) {
            log.error("File processing error for {}: {}", mp3.getFileName(), e.getMessage());
            return null;
        }
    }

    private static void appendWrappedText(
            String combinedPath,
            String text,
            int maxLen
    ) throws IOException {

        List<String> wrappedLines = wrapText(text, maxLen);

        StringBuilder sb = new StringBuilder();
        for (String line : wrappedLines) {
            sb.append(line).append("\n");
        }
        sb.append("\n");
        Files.writeString(
                Path.of(combinedPath),
                sb.toString(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    private static List<String> wrapText(String text, int maxLen) {
        List<String> result = new ArrayList<>();

        for (String paragraph : text.split("\n")) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) {
                result.add("");
                continue;
            }

            String[] words = paragraph.split(" ");
            StringBuilder line = new StringBuilder();

            for (String w : words) {
                if (line.length() + w.length() + 1 > maxLen) {
                    result.add(line.toString());
                    line = new StringBuilder();
                }
                if (!line.isEmpty()) line.append(" ");
                line.append(w);
            }

            if (!line.isEmpty()) {
                result.add(line.toString());
            }
        }

        return result;
    }

}