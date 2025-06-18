package pl.edu.agh.fis.checkers.server;

import pl.edu.agh.fis.checkers.game.Game;
import pl.edu.agh.fis.checkers.game.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.CountDownLatch;

import static pl.edu.agh.fis.checkers.game.Game.Prompt;

public class Client {
    public static void main(String[] args) {
        String hostname = args.length > 0 ? args[0] : "localhost";
        int port = 8080;
        final CountDownLatch pauseLatch = new CountDownLatch(1);
        Color playerColor = null;
        Color currentPlayerColor = Color.WHITE;

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
                playerColor = Color.BLACK;
            } else {
                out.println("JOIN:" + System.console().readLine("Enter game ID: "));
                playerColor = Color.WHITE;
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
//            new Thread(() -> {
//                try {
//                    String serverMessage;
//                    while ((serverMessage = in.readLine()) != null) {
//                        System.out.println("Server: " + serverMessage);
//                        pauseLatch.countDown();
//                    }
//                } catch (IOException e) {
//                    System.out.println("Disconnected from server");
//                }
//            }).start();

            Game game = new Game();

            System.out.print("\033[2J\033[H\033[s");
            while (true) {
                game.printGame();

                if (playerColor != currentPlayerColor) {
                    pauseLatch.await();
                }
                String input = game.readMove();
                if (input.contains("EXIT") || input.contains("Q")) {
                    break;
                }


                Move move;
                try {
                    move = game.processMove(input);
                } catch (InvalidMoveException | ForbiddenPositionException e) {
                    continue;
                }
                game.makeMove(move);
            }

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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
