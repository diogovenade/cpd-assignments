import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.*;

public class ChatServer {

    private static final Map<String, String> users = new HashMap<>();
    private static final Map<String, ChatRoom> rooms = new HashMap<>();
    private static final Map<String, String> tokens = new HashMap<>();
    private static final Lock userLock = new ReentrantLock();
    private static final Lock roomLock = new ReentrantLock();
    private static final Lock tokenLock = new ReentrantLock();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java ChatServer <port>");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number: " + args[0]);
            return;
        }

        loadUsers();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Chat server listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                Thread.startVirtualThread(() -> handleClient(socket));
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("Welcome to the server! Authenticate using 'TOKEN: <your_token>' or '<username> <password>'.");
            String tokenOrCreds = in.readLine();

            if (tokenOrCreds == null) {
                System.out.println("Client provided no authentication string. Closing connection.");
                return;
            }

            String username = authenticate(tokenOrCreds, out);
            if (username == null) {
                System.out.println("Authentication failed for a client. Closing connection.");
                return;
            }

            out.println("Authenticated successfully as " + username + ".");

            while (true) {
                out.println("Enter room name to join, or type 'exit' to quit:");
                String roomName = in.readLine();

                if (roomName == null || roomName.equalsIgnoreCase("exit")) {
                    break;
                }

                if (roomName.trim().isEmpty()) {
                    out.println("Room name cannot be empty.");
                    continue;
                }

                boolean isAIRoom = roomName.startsWith("AI:");
                ChatRoom room;

                roomLock.lock();
                try {
                    room = rooms.computeIfAbsent(roomName, rn -> new ChatRoom(rn, isAIRoom));
                } finally {
                    roomLock.unlock();
                }

                out.println("Entering room: " + roomName + (isAIRoom ? " (AI)" : ""));
                room.addUser(username, in, out);

                out.println("Left room: " + roomName);
            }

        } catch (IOException e) {
            System.out.println("Client connection dropped or error: " + e.getMessage());
        } finally {
            System.out.println("Client disconnected.");
        }
    }

    private static String authenticate(String input, PrintWriter out) {
        if (input.startsWith("TOKEN:")) {
            String tokenValue = input.substring(6).trim();
            if (tokenValue.isEmpty()) {
                out.println("Invalid token format.");
                return null;
            }
            tokenLock.lock();
            try {
                String username = tokens.get(tokenValue);
                if (username == null) {
                    out.println("Invalid or expired token.");
                    return null;
                }
                return username;
            } finally {
                tokenLock.unlock();
            }
        }

        String[] parts = input.split(" ", 2);
        if (parts.length != 2) {
            out.println("Invalid credentials format. Please use '<username> <password>'.");
            return null;
        }

        String username = parts[0];
        String password = parts[1];

        if (username.isEmpty() || password.isEmpty()) {
            out.println("Username or password cannot be empty.");
            return null;
        }

        userLock.lock();
        try {
            if (!users.containsKey(username)) {
                users.put(username, password);
                saveUsers();
                out.println("New user registered: " + username);
            } else if (!users.get(username).equals(password)) {
                out.println("Authentication failed: Incorrect password for " + username + ".");
                return null;
            }
        } finally {
            userLock.unlock();
        }

        String generatedToken = UUID.randomUUID().toString();
        tokenLock.lock();
        try {
            tokens.put(generatedToken, username);
        } finally {
            tokenLock.unlock();
        }
        out.println("TOKEN:" + generatedToken);

        return username;
    }

    private static void loadUsers() {
        File file = new File("users.txt");
        if (!file.exists()) {
            System.out.println("users.txt not found, starting with no pre-loaded users.");
            return;
        }

        userLock.lock();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
            System.out.println("Users loaded from users.txt");
        } catch (IOException e) {
            System.err.println("Failed to load users from users.txt: " + e.getMessage());
        } finally {
            userLock.unlock();
        }
    }

    private static void saveUsers() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("users.txt"))) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                pw.println(entry.getKey() + ":" + entry.getValue());
            }
            System.out.println("Users saved to users.txt");
        } catch (IOException e) {
            System.err.println("Failed to save users to users.txt: " + e.getMessage());
        }
    }
}
