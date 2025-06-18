package pl.edu.agh.fis.checkers.server;

import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class ClientHandler implements Runnable {
    private static final String DB_URL = "jdbc:sqlite:src/main/resources/checkers.db";
    // Mapa zalogowanych klientów: nickname -> ClientHandler
    private static final ConcurrentHashMap<String, ClientHandler> loggedIn = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, ClientHandler> gameOffers = new ConcurrentHashMap<>();

    private final Socket socket;
    private final CountDownLatch pauseLatch = new CountDownLatch(1);
    private BufferedReader in;
    private BufferedWriter out;
    private String username;
    private int userId;
    private boolean authenticated = false;
    private boolean playing = false;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        logIn();
        if (!authenticated) {
            return;
        }
        try {
            String line, response = "";
            while ((line = in.readLine()) != null && !response.matches("^(GAME|JOIN).*")) {
                if (line.equals("EXIT")) {
                    break;
                } else if (line.equals("CREATE_GAME")) {
                    response = processCreateGame();
                } else if (line.startsWith("JOIN:")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        try {
                            int gameId = Integer.parseInt(parts[1]);
                            response = processJoinGame(gameId);
                        } catch (NumberFormatException e) {
                            response = "ERROR:Zły format ID gry";
                        }
                    } else {
                        response = "ERROR:Zły format. Użyj JOIN:id_gry";
                    }

                } else {
                    response = "ERROR:Nieznane polecenie";
                }

                out.write(response);
                out.newLine();
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String line;
            while (!playing && (line = in.readLine()) != null) {
                if (line.equals("EXIT")) {
                    logOut();
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            pauseLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logOut();
    }

    public void sendMessage(String message) {
        try {
            out.write(message);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedReader getReader() {
        return in;
    }

    public BufferedWriter getWriter() {
        return out;
    }

    public void resume() {
        pauseLatch.countDown();
    }

    private void logIn() {
        try {
            String line;
            while (!authenticated && (line = in.readLine()) != null) {
                String response;
                if (line.startsWith("LOGIN:")) {
                    String[] parts = line.split(":", 3);
                    if (parts.length == 3) {
                        response = processLogin(parts[1], parts[2]);
                        if ("LOGIN_SUCCESS".equals(response)) {
                            authenticated = true;
                            username = parts[1];
                            // dodaj do mapy zalogowanych
                            loggedIn.put(username, this);
                        }
                    } else {
                        response = "ERROR:Zły format. Użyj LOGIN:nazwa:haslo";
                    }
                } else if (line.startsWith("REGISTER:")) {
                    String[] parts = line.split(":", 3);
                    if (parts.length == 3) {
                        response = processRegister(parts[1], parts[2]);
                    } else {
                        response = "ERROR:Zły format. Użyj REGISTER:nazwa:haslo";
                    }
                } else {
                    response = "ERROR:Nieznane polecenie";
                }

                out.write(response);
                out.newLine();
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logOut() {
        // przy wyjściu usuń z mapy
        if (authenticated && username != null) {
            loggedIn.remove(username);
        }
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private void playGame() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
//                String response;
//                if (line.equals("EXIT")) {
//                    break;
//                } else if (line.startsWith("WHITE:")) {
//                    String[] parts = line.split(":", 2);
//                    if (parts.length == 2) {
//                        try {
//                            int gameId = Integer.parseInt(parts[1]);
//                            response = processJoinGame(gameId);
//                        } catch (NumberFormatException e) {
//                            response = "ERROR:Zły format ID gry";
//                        }
//                    } else {
//                        response = "ERROR:Zły format. Użyj JOIN:id_gry";
//                    }
//
//                } else {
//                    response = "ERROR:Nieznane polecenie";
//                }
//
//                out.write(response);
//                out.newLine();
//                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String processLogin(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "SELECT password FROM players WHERE nickname = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String stored = rs.getString("password");
                    if (stored.equals(password)) {
                        return "LOGIN_SUCCESS";
                    } else {
                        return "LOGIN_FAILED";
                    }
                } else {
                    return "LOGIN_FAILED:Użytkownik nie istnieje. Aby się zarejestrować wyślij REGISTER:" + username + ":haslo";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR:Problem z bazą danych";
        }
    }

    private String processRegister(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String check = "SELECT 1 FROM players WHERE nickname = ?";
            try (PreparedStatement ps = conn.prepareStatement(check)) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return "ERROR:Użytkownik już istnieje";
                }
            }
            String insert = "INSERT INTO players(nickname, password) VALUES(?, ?)";
            try (PreparedStatement ps2 = conn.prepareStatement(insert)) {
                ps2.setString(1, username);
                ps2.setString(2, password);
                ps2.executeUpdate();
                return "REGISTER_SUCCESS";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR:Problem z bazą danych";
        }
    }

    private String processCreateGame() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String insert = "INSERT INTO games (player_white_id, player_black_id, start_time, status) VALUES (?, NULL, ?, 'IN_PROGRESS')";
            try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                ps.setString(2, now);
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    int gameId = keys.getInt(1);
                    gameOffers.put(gameId, this);
                    return "GAME_CREATED:" + gameId;
                }
                return "ERROR:Nie można utworzyć gry";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR:Problem z bazą danych";
        }
    }

    private String processJoinGame(int gameId) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Sprawdź istnienie i wolne miejsce
            String query = "SELECT player_black_id FROM games WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, gameId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    return "ERROR:Gra o ID " + gameId + " nie istnieje";
                }
                int blackId = rs.getInt("player_black_id");
                if (!rs.wasNull()) {
                    return "ERROR:Gra jest już pełna";
                }
            }
            // Dołącz jako czarny
            String update = "UPDATE games SET player_black_id = ? WHERE id = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(update)) {
                ps2.setInt(1, userId);
                ps2.setInt(2, gameId);
                ps2.executeUpdate();
            }
            // Powiadom obu graczy
            ClientHandler creator = gameOffers.get(gameId);
            if (creator == null) {
                return "ERROR:Nie można dołączyć do gry";
            }
            creator.out.write("GAME_JOIN:" + gameId);
            creator.out.newLine();
            creator.out.flush();
            creator.playing = true;
            this.out.write("GAME_START:" + gameId);
            this.out.newLine();
            this.out.flush();
            this.playing = true;

            // Gra pozostaje w gameOffers aż do zakończenia lub pauzy
            Game game = new Game(gameId, this, loggedIn.get(creator.username));
            game.start();
            return "JOIN_SUCCESS:" + gameId;
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return "ERROR:Nie można dołączyć do gry";
        }
    }
}

//package pl.edu.agh.fis.checkers.server;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.Socket;
//import java.util.UUID;
//
//class ClientHandler implements Runnable {
//    private final Socket clientSocket;
//    private final String clientId;
//    private PrintWriter out;
//    private BufferedReader in;
//
//    public ClientHandler(Socket socket) {
//        this.clientSocket = socket;
//        this.clientId = UUID.randomUUID().toString().substring(0, 8);
//    }
//
//    public String getClientId() {
//        return clientId;
//    }
//
//    public void sendMessage(String message) {
//        out.println(message);
//    }
//
//    @Override
//    public void run() {
//        try {
//            out = new PrintWriter(clientSocket.getOutputStream(), true);
//            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//
//            // Register this client with the server
//            Server.registerClient(this);
//
//            String inputLine;
//            while ((inputLine = in.readLine()) != null) {
//                if (inputLine.equalsIgnoreCase("quit")) {
//                    break;
//                }
//
//                // Check if this is a pairing request
//                if (inputLine.startsWith("PAIR_WITH:")) {
//                    String targetId = inputLine.substring("PAIR_WITH:".length());
//                    Server.handlePairRequest(clientId, targetId);
//                }
//                // Otherwise forward as regular message
//                else {
//                    System.out.println("Message from " + clientId + ": " + inputLine);
//                    Server.forwardMessage(clientId, inputLine);
//                }
//            }
//        } catch (IOException ex) {
//            System.out.println("Exception in ClientHandler: " + ex.getMessage());
//        } finally {
//            try {
//                Server.removeClient(clientId);
//                in.close();
//                out.close();
//                clientSocket.close();
//            } catch (IOException ex) {
//                System.out.println("Error closing connection: " + ex.getMessage());
//            }
//        }
//    }
//}
