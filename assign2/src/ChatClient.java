import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatClient {

    private static String sessionToken = null;
    private static final AtomicBoolean running = new AtomicBoolean(true);

    public static void main(String[] args) {
        if (args.length < 2) {
            return;
        }

        String hostname = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number: " + args[1]);
            return;
        }

        SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();

        try (SSLSocket socket = (SSLSocket) ssf.createSocket();) {

            String[] protos = { "TLSv1.3" };
            socket.setEnabledProtocols(protos);
            String[] suites = { "TLS_AES_128_GCM_SHA256" };

            socket.setEnabledCipherSuites(suites);

            SocketAddress srvrAddr = new InetSocketAddress(hostname, port);

            socket.connect(srvrAddr);

            PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Connected to chat server at " + hostname + ":" + port);

            // --- Authentication ---
            String authResponse;
            do {
                System.out.println("Server: " + serverIn.readLine());
                System.out.print("Enter authentication ('<username> <password>' or 'TOKEN: <your_token>'): ");
                String authInput = consoleIn.readLine();
                if (authInput == null) {
                    System.err.println("No authentication input provided. Exiting.");
                    return;
                }
                serverOut.println(authInput);
                authResponse = serverIn.readLine();
                System.out.println("Server: " + authResponse);

                if (authResponse != null && authResponse.startsWith("TOKEN:")) {
                    sessionToken = authResponse.substring(6);
                    System.out.println("(Session token stored for potential future use)");
                    authResponse = serverIn.readLine();
                    System.out.println("Server: " + authResponse);
                }

            } while (authResponse == null || !authResponse.startsWith("Authenticated successfully"));

            // --- Room List ---
            String line;
            while ((line = serverIn.readLine()) != null) {
                if (line.equals("END_OF_ROOMS")) break;
                System.out.println("Server: " + line);
            }

            // --- Room Selection / Chatting ---
            while (running.get()) {
                System.out.println("Server: " + serverIn.readLine());
                System.out.print("> ");
                String roomInput = consoleIn.readLine();

                if (roomInput == null || roomInput.equalsIgnoreCase("exit")) {
                    serverOut.println("exit");
                    break;
                }

                serverOut.println(roomInput);

                String roomResponse = serverIn.readLine();
                if (roomResponse == null) {
                    System.out.println("Server disconnected unexpectedly.");
                    break;
                }
                System.out.println("Server: " + roomResponse);

                if (roomResponse.startsWith("Entering room:")) {
                    Thread serverListener = Thread.startVirtualThread(() -> readServerMessages(serverIn));

                    try {
                        String userInput;
                        while (running.get() && (userInput = consoleIn.readLine()) != null) {
                            if (!serverListener.isAlive()) {
                                System.err.println("Server connection lost.");
                                running.set(false);
                                break;
                            }
                            if (userInput.trim().isEmpty()) {
                                continue;
                            }
                            serverOut.println(userInput);
                            if (serverOut.checkError()) {
                                System.err.println("Error sending message. Server connection might be lost.");
                                running.set(false);
                                serverListener.interrupt();
                                break;
                            }
                        }
                    } catch (IOException e) {
                        if (running.get()) {
                            System.err.println("Error reading console input: " + e.getMessage());
                        }
                    } finally {
                        running.set(false);
                        if(serverListener.isAlive()) {
                            try {
                                socket.shutdownInput();
                            } catch (IOException ignored) { }
                        }
                    }
                } else {
                    System.err.println("Could not enter room.");
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("Error: Unknown host " + hostname);
        } catch (IOException e) {
            System.err.println("Error connecting to or communicating with server: " + e.getMessage());
            // e.printStackTrace();
        } finally {
            System.out.println("Client shutting down.");
        }
    }

    private static void readServerMessages(BufferedReader serverIn) {
        try {
            String serverMessage;
            while (running.get() && (serverMessage = serverIn.readLine()) != null) {
                System.out.println(serverMessage); // Print message from server/other users/Bot
            }
        } catch (IOException e) {
            if (running.get()) {
                System.err.println("\nConnection to server lost or closed.");
            }
        } finally {
            running.set(false);
            System.out.println("(Server listener thread stopped. Press Enter to exit if needed.)");

        }
    }
}