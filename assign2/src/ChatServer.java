import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.util.*;
import java.util.concurrent.locks.*;

public class ChatServer {

    private static final Map<String, String> users = new HashMap<>();
    private static final Map<String, ChatRoom> rooms = new HashMap<>();
    private static final Map<String, String> tokens = new HashMap<>();
    private static final Map<String, String> tokenToRoom = new HashMap<>(); // Maps tokens to last room
    private static final Lock userLock = new ReentrantLock();
    private static final Lock roomLock = new ReentrantLock();
    private static final Lock tokenLock = new ReentrantLock();
    private static final Lock tokenRoomLock = new ReentrantLock();

    public static void main(String[] args) {
        if (args.length < 1) {
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
        loadRooms();

        SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        try (SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(port)) {
            String[] protos = { "TLSv1.3" };
            serverSocket.setEnabledProtocols(protos);
            String[] suites = { "TLS_AES_128_GCM_SHA256" };

            serverSocket.setEnabledCipherSuites(suites);

            System.out.println("Chat server listening on port " + port);

            while (true) {
                SSLSocket socket = (SSLSocket) serverSocket.accept();
                Thread.startVirtualThread(() -> handleClient(socket));
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            //e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String tokenOrCreds;
            String[] usernameAndToken;
            while (true) {
                out.println("Welcome to the server! Authenticate using 'TOKEN: <your_token>' or '<username> <password>'.");
                tokenOrCreds = in.readLine();

                if (tokenOrCreds == null) {
                    continue;
                }

                usernameAndToken = authenticate(tokenOrCreds, out);
                if (usernameAndToken == null) {
                    continue;
                }
                break;
            }

            String username = usernameAndToken[0];
            String userToken = usernameAndToken[1];

            out.println("Authenticated successfully as " + username + ".");
            
            // Check if user has a previous room to resume
            String lastRoom = null;
            tokenRoomLock.lock();
            try {
                lastRoom = tokenToRoom.get(userToken);
            } finally {
                tokenRoomLock.unlock();
            }
            
            boolean hasResumedRoom = false;
            
            if (lastRoom != null) {
                roomLock.lock();
                ChatRoom room = null;
                try {
                    room = rooms.get(lastRoom);
                } finally {
                    roomLock.unlock();
                }
                
                if (room != null) {
                    out.println("Previous session found in room: " + lastRoom);
                    showRoomList(out);
                    
                    out.println("Enter room name to join, or press Enter to rejoin '" + lastRoom + "', or type 'exit' to quit:");
                    String input = in.readLine();
                    
                    if (input != null && !input.trim().isEmpty() && !input.equalsIgnoreCase("exit")) {
                        processRoomJoin(input, username, userToken, in, out);
                        hasResumedRoom = true;
                    } else if (input == null || input.equalsIgnoreCase("exit")) {
                        return;
                    } else {
                        tokenRoomLock.lock();
                        try {
                            tokenToRoom.put(userToken, lastRoom);
                        } finally {
                            tokenRoomLock.unlock();
                        }
                        
                        out.println("Entering room: " + lastRoom + (room.isAI() ? " (AI)" : ""));
                        out.println("Type '\\q' to exit this room.");
                        room.addUser(username, in, out);
                        out.println("Left room: " + lastRoom);
                        hasResumedRoom = true;
                    }
                } else {
                    showRoomList(out);
                }
            } else {
                showRoomList(out);
            }

            while (!hasResumedRoom) {
                out.println("Enter room name to join, or type 'exit' to quit:");
                String roomName = in.readLine();

                if (roomName == null || roomName.equalsIgnoreCase("exit")) {
                    break;
                }

                if (roomName.trim().isEmpty()) {
                    out.println("Room name cannot be empty.");
                    continue;
                }

                processRoomJoin(roomName, username, userToken, in, out);
                hasResumedRoom = true;
            }

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

                processRoomJoin(roomName, username, userToken, in, out);
            }

        } catch (IOException e) {
            System.out.println("Client connection dropped or error: " + e.getMessage());
        } finally {
            System.out.println("Client disconnected.");
        }
    }
    
    private static void processRoomJoin(String roomName, String username, String userToken, 
                                       BufferedReader in, PrintWriter out) throws IOException {
        boolean isAIRoom = roomName.startsWith("AI:");
        ChatRoom room;
        boolean created = false;

        roomLock.lock();
        try {
            room = rooms.get(roomName);
            if (room == null) {
                room = new ChatRoom(roomName, isAIRoom);
                rooms.put(roomName, room);
                created = true;
            }
        } finally {
            roomLock.unlock();
        }
        if (created) saveRooms();
        
        // Store the user's current room with their token
        tokenRoomLock.lock();
        try {
            tokenToRoom.put(userToken, roomName);
        } finally {
            tokenRoomLock.unlock();
        }

        out.println("Entering room: " + roomName + (isAIRoom ? " (AI)" : ""));
        out.println("Type '\\q' to exit this room.");
        room.addUser(username, in, out);
        out.println("Left room: " + roomName);
    }

    private static void showRoomList(PrintWriter out) {
        roomLock.lock();
        try {
            if (rooms.isEmpty()) {
                out.println("No rooms available. You can create a new one by entering a name or you can join an AI room by entering 'AI:<room_name>'.");
            } else {
                out.println("Available rooms:");
                for (String roomName : rooms.keySet()) {
                    out.println(" - " + roomName + (rooms.get(roomName).isAI() ? " (AI)" : ""));
                }
            }
            out.println("END_OF_ROOMS");
        } finally {
            roomLock.unlock();
        }
    }

    private static String[] authenticate(String input, PrintWriter out) {
        String username;
        String token;
        
        if (input.startsWith("TOKEN:")) {
            token = input.substring(6).trim();
            if (token.isEmpty()) {
                out.println("Invalid token format.");
                return null;
            }
            tokenLock.lock();
            try {
                username = tokens.get(token);
                if (username == null) {
                    out.println("Invalid or expired token.");
                    return null;
                }
            } finally {
                tokenLock.unlock();
            }
            return new String[] {username, token};
        }

        String[] parts = input.split(" ", 2);
        if (parts.length != 2) {
            out.println("Invalid credentials format. Please use '<username> <password>'.");
            return null;
        }

        username = parts[0];
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

        token = UUID.randomUUID().toString();
        tokenLock.lock();
        try {
            tokens.put(token, username);
        } finally {
            tokenLock.unlock();
        }
        out.println("TOKEN:" + token);

        return new String[] {username, token};
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

    private static void loadRooms() {
        File file = new File("rooms.txt");
        if (!file.exists()) {
            System.out.println("rooms.txt not found, starting with no pre-loaded rooms.");
            return;
        }

        roomLock.lock();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    String roomName = parts[0];
                    boolean isAI = Boolean.parseBoolean(parts[1]);
                    rooms.put(roomName, new ChatRoom(roomName, isAI));
                }
            }
            System.out.println("Rooms loaded from rooms.txt");
        } catch (IOException e) {
            System.err.println("Failed to load rooms from rooms.txt: " + e.getMessage());
        } finally {
            roomLock.unlock();
        }
    }

    private static void saveRooms() {
        roomLock.lock();
        try (PrintWriter pw = new PrintWriter(new FileWriter("rooms.txt"))) {
            for (Map.Entry<String, ChatRoom> entry : rooms.entrySet()) {
                pw.println(entry.getKey() + ":" + entry.getValue().isAI());
            }
            System.out.println("Rooms saved to rooms.txt");
        } catch (IOException e) {
            System.err.println("Failed to save rooms to rooms.txt: " + e.getMessage());
        } finally {
            roomLock.unlock();
        }
    }
}
