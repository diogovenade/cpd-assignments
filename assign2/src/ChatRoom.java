import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChatRoom {
    private final String name;
    private final boolean isAI;
    private final List<String> messages = new ArrayList<>();
    private final List<ClientSession> users = new ArrayList<>();
    private final Lock lock = new ReentrantLock();
    private final BotClient botClient = new BotClient("llama3");

    public ChatRoom(String name, boolean isAI) {
        this.name = name;
        this.isAI=isAI;
    }

    public boolean isAI() {
        return isAI;
    }

    private JSONArray getHistory() {
        JSONArray history = new JSONArray();
        for (String message : messages) {
            String[] parts = message.split(": ", 2);
            if (parts.length == 2) {
                String sender = parts[0].trim();
                String content = parts[1].trim();
                JSONObject msg = new JSONObject();

                if (sender.equalsIgnoreCase("Bot")) {
                    msg.put("role", "assistant");
                    msg.put("content", content);
                } else {
                    msg.put("role", "user");
                    // Include username in content so bot "knows" who spoke
                    msg.put("content", sender + " said: " + content);
                }
                history.put(msg);
            }
        }
        return history;
    }


    public void addUser(String username, BufferedReader in, PrintWriter out) throws IOException {
        ClientSession session = new ClientSession(username, out);
        lock.lock();
        try {
            users.add(session);
            broadcast("[" + username + " enters the room]");
            // replay history
            for (String msg : messages) out.println(msg);
        } finally {
            lock.unlock();
        }

        if (isAI) {
            JSONArray history = getHistory();
            botClient.askBotAsync(history, "A new user named " + username + " just joined. Welcome the user. If anything was said summarize in 2 sentences what other users has been saying so far. If not just tell him he is the first. I want a fast simple response")
                    .thenAccept(intro -> {
                        String botLine = "Bot: " + intro;
                        lock.lock();
                        try {
                            messages.add(botLine);
                            broadcast(botLine);
                        } finally {
                            lock.unlock();
                        }
                    });
        }

        String line;
        while ((line = in.readLine()) != null) {
            if (line.equalsIgnoreCase("\\q")) {
                // explicit leave command
                out.println("[You left the room]");
                break;
            }
            if (line.trim().isEmpty()) {
                continue;
            }
            String message = username + ": " + line;
            lock.lock();
            try {
                messages.add(message);
                broadcast(message);
            } finally {
                lock.unlock();
            }

            if (line.toLowerCase().contains("@bot")){
                botClient.askBotMention(line)
                        .thenAccept(intro -> {
                            String botLine = "Bot: " + intro;
                            lock.lock();
                            try {
                                messages.add(botLine);
                                broadcast(botLine);
                            } finally {
                                lock.unlock();
                            }
                        });
            }
        }

        // once loop exits, clean up
        removeUser(session);
    }


    private void removeUser(ClientSession session) {
        lock.lock();
        try {
            users.remove(session);
            broadcast("[" + session.username + " left the room]");
        } finally {
            lock.unlock();
        }
    }

    private void broadcast(String message) {
        lock.lock();
        try {
            for (ClientSession user : users) {
                user.out.println(message);
            }
        } finally {
            lock.unlock();
        }
    }

    private static class ClientSession {
        String username;
        PrintWriter out;

        ClientSession(String username, PrintWriter out) {
            this.username = username;
            this.out = out;
        }
    }
}
