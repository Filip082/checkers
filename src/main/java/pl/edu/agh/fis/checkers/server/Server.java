package pl.edu.agh.fis.checkers.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 8080;
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    private static final Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();
    private static final Map<String, Game> activeGames = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                pool.execute(clientHandler);
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static synchronized void registerClient(ClientHandler client) {
        connectedClients.put(client.getClientId(), client);
        client.sendMessage("YOUR_ID:" + client.getClientId());
        System.out.println("Registered client: " + client.getClientId());
    }

    public static synchronized void handlePairRequest(String requesterId, String targetId) {
        ClientHandler requester = connectedClients.get(requesterId);
        ClientHandler target = connectedClients.get(targetId);

        if (requester == null || target == null) {
            if (requester != null) {
                requester.sendMessage("ERROR:Client not found");
            }
            return;
        }

        if (activeGames.containsKey(gameId)) {
            requester.sendMessage("ERROR:One or both clients are already paired");
            return;
        }

        // Create the pair
        String pairId = UUID.randomUUID().toString();
        activePairs.put(requesterId, new Pair(pairId, target));
        activePairs.put(targetId, new Pair(pairId, requester));

        requester.sendMessage("PAIRED_WITH:" + targetId);
        target.sendMessage("PAIRED_WITH:" + requesterId);

        System.out.println("Paired clients: " + requesterId + " and " + targetId);
    }

    public static synchronized void forwardMessage(String senderId, String message) {
        Pair pair = activePairs.get(senderId);
        if (pair != null) {
            pair.getPartner().sendMessage("FROM:" + senderId + ":" + message);
        }
    }

    public static synchronized void removeClient(String clientId) {
        connectedClients.remove(clientId);
        Pair pair = activePairs.remove(clientId);
        if (pair != null) {
            ClientHandler partner = pair.getPartner();
            if (partner != null) {
                partner.sendMessage("PARTNER_DISCONNECTED:" + clientId);
                activePairs.remove(partner.getClientId());
            }
        }
        System.out.println("Client disconnected: " + clientId);
    }
}
