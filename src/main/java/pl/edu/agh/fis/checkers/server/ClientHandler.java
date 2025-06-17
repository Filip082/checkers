package pl.edu.agh.fis.checkers.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final String clientId;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.clientId = UUID.randomUUID().toString().substring(0, 8);
    }

    public String getClientId() {
        return clientId;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Register this client with the server
            Server.registerClient(this);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equalsIgnoreCase("quit")) {
                    break;
                }

                // Check if this is a pairing request
                if (inputLine.startsWith("PAIR_WITH:")) {
                    String targetId = inputLine.substring("PAIR_WITH:".length());
                    Server.handlePairRequest(clientId, targetId);
                }
                // Otherwise forward as regular message
                else {
                    System.out.println("Message from " + clientId + ": " + inputLine);
                    Server.forwardMessage(clientId, inputLine);
                }
            }
        } catch (IOException ex) {
            System.out.println("Exception in ClientHandler: " + ex.getMessage());
        } finally {
            try {
                Server.removeClient(clientId);
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException ex) {
                System.out.println("Error closing connection: " + ex.getMessage());
            }
        }
    }
}
