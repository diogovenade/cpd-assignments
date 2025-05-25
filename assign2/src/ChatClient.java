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

        BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
        boolean userRequestedExit = false;
        while (true) {
            running.set(true);
            boolean reconnect = false;
            try {
                reconnect = !runClient(hostname, port, consoleIn);
            } catch (UserExitException e) {
                userRequestedExit = true;
                break;
            } catch (Exception e) {
                System.err.println("Unexpected error: " + e.getMessage());
                reconnect = true;
            }
            if (userRequestedExit) break;
            // Always retry, never break the loop unless user requested exit
            System.out.println("Connection lost or failed. Retrying in 2 seconds...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                break;
            }
            System.out.println("Reconnecting...");
        }
        System.out.println("Client shutting down.");
    }

    private static boolean runClient(String hostname, int port, BufferedReader consoleIn) throws UserExitException {
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

            System.out.println("Connected to chat server at " + hostname + ":" + port);
            System.out.println("Client local port: " + socket.getLocalPort());

            // --- Authentication ---
            String authResponse;
            boolean triedToken = false;
            do {
                System.out.println("Server: " + serverIn.readLine());
                String authInput;
                if (sessionToken != null && !triedToken) {
                    System.out.println("Using stored session token.");
                    authInput = "TOKEN: " + sessionToken;
                    triedToken = true;
                } else {
                    System.out.print("Enter authentication ('<username> <password>' or 'TOKEN: <your_token>'): ");
                    authInput = consoleIn.readLine();
                    if (authInput == null) {
                        System.err.println("No authentication input provided. Exiting.");
                        return false;
                    }
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

                // If token was invalid, clear it so user is prompted next time
                if (triedToken && (authResponse == null || authResponse.toLowerCase().contains("invalid"))) {
                    sessionToken = null;
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
                String in = serverIn.readLine();
                if (in == null) {
                    System.out.println("Server disconnected unexpectedly.");
                    return true; // trigger reconnect
                }
                System.out.println("Server: " + in);
                if (!in.startsWith("Previous session found in")) {
                    System.out.print("> ");
                    String roomInput = consoleIn.readLine();

                    if (roomInput == null || roomInput.equalsIgnoreCase("exit")) {
                        serverOut.println("exit");
                        throw new UserExitException(); // Ensure main loop breaks and does not reconnect
                    }

                    serverOut.println(roomInput);
                }

                String roomResponse = serverIn.readLine();
                if (roomResponse == null) {
                    System.out.println("Server disconnected unexpectedly.");
                    return true; // trigger reconnect
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
            return true; // always trigger reconnect
        } catch (UserExitException e) {
            throw e; // propagate to main to break the loop
        } catch (IOException e) {
            System.err.println("Error connecting to or communicating with server: " + e.getMessage());
            return true; // trigger reconnect
        }
        return false;
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

    private static class UserExitException extends Exception {}
}