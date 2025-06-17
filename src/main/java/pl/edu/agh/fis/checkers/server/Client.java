package pl.edu.agh.fis.checkers.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.CountDownLatch;

public class Client {
    public static void main(String[] args) {
        String hostname = args.length > 0 ? args[0] : "localhost";
        int port = 8080;
        final CountDownLatch pauseLatch = new CountDownLatch(1);

        try (Socket socket = new Socket(hostname, port)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            do {
                String login = System.console().readLine("Login: ");
                String password = System.console().readLine("Password: ");
                out.println("LOGIN:" + login + ":" + password);
            } while (!in.readLine().equalsIgnoreCase("LOGIN_SUCCESS"));


            if (System.console().readLine("Create new game? (y/n)").equalsIgnoreCase("y")) {
                out.println("CREATE_GAME");
            } else {
                out.println("JOIN:" + System.console().readLine("Enter game ID: "));
            }
            out.println("");

            String line;
            do {
                line = in.readLine();
                if (line.equalsIgnoreCase("WAIT_TURN")) {
                    out.println("MOVE");
                }
                System.out.println(line);
            } while (!line.equalsIgnoreCase("MOVE_OR_COMMAND"));

//             Thread to handle server messages
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println("Server: " + serverMessage);
                        pauseLatch.countDown();
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server");
                }
            }).start();

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                if (userInput.equalsIgnoreCase("quit")) {
                    out.println(userInput);
                    System.out.println(in.readLine());
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
