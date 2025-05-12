import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

public class ChatRoom {
    private final String name;
    private final List<String> messages = new ArrayList<>();
    private final List<ClientSession> users = new ArrayList<>();
    private final Lock lock = new ReentrantLock();

    public ChatRoom(String name, boolean ignored) {
        this.name = name;
    }

    public void addUser(String username, BufferedReader in, PrintWriter out) {
        ClientSession session = new ClientSession(username, out);
        lock.lock();
        try {
            users.add(session);
            broadcast("[" + username + " enters the room]");
            for (String msg : messages) out.println(msg);
        } finally {
            lock.unlock();
        }

        Thread.startVirtualThread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    String message = username + ": " + line;
                    lock.lock();
                    try {
                        messages.add(message);
                        broadcast(message);
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (IOException ignored) {
            } finally {
                removeUser(session);
            }
        });
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
        for (ClientSession user : users) {
            user.out.println(message);
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