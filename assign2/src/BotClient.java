import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.*;

public class BotClient {
    private static final String API_URL = "http://localhost:11434/api/chat";  // Use /api/chat for chat-style messages
    private final String model;

    public BotClient(String model) {
        this.model = model;
    }

    public String askBot(List<String> historyLines, String userQuery) {
        try {
            // 1) Build JSON payload manually
            StringBuilder sb = new StringBuilder();
            sb.append("{")
                    .append("\"model\":\"").append(model).append("\",")
                    .append("\"stream\":false,")
                    .append("\"messages\":[")
                    .append("{\"role\":\"system\",\"content\":\"You are a helpful assistant.\"}");

            // Append conversation history
            for (String line : historyLines) {
                // Skip any bot messages
                if (line.startsWith("Bot:")) continue;

                // Include full message as-is, e.g. "miguel: Eu gosto de futebol"
                String content = escapeJson(line.trim());

                sb.append(",{\"role\":\"user\",\"content\":\"").append(content).append("\"}");
            }


            // Add user message
            sb.append(",{\"role\":\"user\",\"content\":\"").append(escapeJson(userQuery)).append("\"}");

            sb.append("]}");

            byte[] payload = sb.toString().getBytes(StandardCharsets.UTF_8);

            HttpURLConnection conn = (HttpURLConnection) URI.create(API_URL).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setFixedLengthStreamingMode(payload.length);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload);
            }

            int status = conn.getResponseCode();
            InputStream stream = (status >= 200 && status < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            StringBuilder resp = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    resp.append(line);
                }
            }

            if (status < 200 || status >= 300) {
                return "[Bot HTTP " + status + ": " + resp + "]";
            }

            // Extract "content" from response JSON
            Pattern p = Pattern.compile("\"content\"\\s*:\\s*\"((?:\\\\\"|[^\"])*)\"");
            Matcher m = p.matcher(resp.toString());
            if (m.find()) {
                return m.group(1)
                        .replace("\\n", "\n")
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\");
            } else {
                return "[Bot error: no content field]";
            }

        } catch (IOException e) {
            return "[Bot error: " + e.getMessage() + "]";
        }
    }

    // JSON string escaper
    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
