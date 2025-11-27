package ch02;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ListModels {
    public static void main(String[] args) {
        try {
            String hfToken = System.getenv("HF_TOKEN");
            if (hfToken == null || hfToken.isEmpty()) {
                System.out.println("ERROR: HF_TOKEN environment variable is not set");
                return;
            }

            // Simple, always-working endpoint: top 50 text-generation models by downloadscls
            URL url = new URL("https://huggingface.co/api/models?pipeline_tag=text-generation&limit=5&sort=downloads&direction=-1");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + hfToken);
            conn.setRequestProperty("User-Agent", "Java-HF-Client/1.0");

            int code = conn.getResponseCode();

            if (code == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line).append("\n");
                }
                in.close();

                System.out.println("Top 50 most downloaded text-generation models on Hugging Face (November 2025):");
                System.out.println(response);
            } else {
                System.out.println("Request failed. Response Code: " + code);
            }
            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}