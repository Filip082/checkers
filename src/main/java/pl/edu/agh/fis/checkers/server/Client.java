package pl.edu.agh.fis.checkers.server;

import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        String hostname = args.length > 0 ? args[0] : "localhost";
        int port = 8080;

        try (Socket socket = new Socket(hostname, port)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            // Thread to handle server messages
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println("Server: " + serverMessage);

                        // If this message contains our ID, display it prominently
                        if (serverMessage.startsWith("YOUR_ID:")) {
                            System.out.println("\n=== Your client ID is: " +
                                    serverMessage.substring("YOUR_ID:".length()) + " ===\n");
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server");
                }
            }).start();

            // Main input loop
            System.out.println("Connected to server. Waiting for your client ID...");
            System.out.println("Commands:");
            System.out.println("  PAIR_WITH:<clientId> - Request to pair with another client");
            System.out.println("  <message> - Send message to paired client");
            System.out.println("  quit - Disconnect\n");

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                if (userInput.equalsIgnoreCase("quit")) {
                    out.println(userInput);
                    break;
                }
                out.println(userInput);
            }
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
