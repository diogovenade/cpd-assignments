import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;

import org.json.JSONArray;
import org.json.JSONObject;

public class BotClient {
    private static final String BASE_URL = "http://localhost:11434";
    private static final String CHAT_ENDPOINT = "/api/chat";
    private final HttpClient http = HttpClient.newHttpClient();
    private final String model;

    public BotClient(String model) {
        this.model = model;
    }

    /**
     * Sends a chat request to Ollama and returns the assistant's reply text.
     *
     * @param history A JSONArray of past messages, each an object with "role" and "content".
     * @param userQuery The new user message to send.
     * @return The assistant's content field from the response.
     */
    public CompletableFuture<String> askBotAsync(JSONArray history, String userQuery) {
        try {

            JSONArray messages = new JSONArray();
            for (int i = 0; i < history.length(); i++) {
                messages.put(history.getJSONObject(i));
            }
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", userQuery));

            JSONObject payload = new JSONObject()
                    .put("model", model)
                    .put("messages", messages)
                    .put("stream", false);


            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + CHAT_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(payload.toString()))
                    .build();

            return http.sendAsync(req, BodyHandlers.ofString())
                    .thenApply(resp -> {
                        try {
                            JSONObject body = new JSONObject(resp.body());
                            if (body.has("message")) {
                                JSONObject msg = body.getJSONObject("message");
                                return msg.getString("content");
                            } else if (body.has("messages")) {
                                JSONArray msgs = body.getJSONArray("messages");
                                if (msgs.length() > 0) {
                                    JSONObject lastMsg = msgs.getJSONObject(msgs.length() - 1);
                                    return lastMsg.getString("content");
                                }
                            } else if (body.has("error")) {
                                return "[Bot error: " + body.getString("error") + "]";
                            }
                            return "[Bot error: Unexpected response format]";
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "[Bot error: " + e.getMessage() + "]";
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture("[Bot error: " + e.getMessage() + "]");
        }
    }

    public CompletableFuture<String> askBotMention(String message){
        try{
            JSONArray messageArray = new JSONArray();
            messageArray.put(new JSONObject()
                    .put("role", "user")
                    .put("content", message));
            JSONObject payload = new JSONObject()
                    .put("model", model)
                    .put("messages", messageArray)
                    .put("stream", false);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + CHAT_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(payload.toString()))
                    .build();
            return http.sendAsync(req, BodyHandlers.ofString())
                    .thenApply(resp -> {
                        try {
                            JSONObject body = new JSONObject(resp.body());
                            if (body.has("message")) {
                                JSONObject msg = body.getJSONObject("message");
                                return msg.getString("content");
                            } else if (body.has("messages")) {
                                JSONArray msgs = body.getJSONArray("messages");
                                if (msgs.length() > 0) {
                                    JSONObject lastMsg = msgs.getJSONObject(msgs.length() - 1);
                                    return lastMsg.getString("content");
                                }
                            } else if (body.has("error")) {
                                return "[Bot error: " + body.getString("error") + "]";
                            }
                            return "[Bot error: Unexpected response format]";
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "[Bot error: " + e.getMessage() + "]";
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture("[Bot error: " + e.getMessage() + "]");
        }
    }
}
