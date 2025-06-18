package pl.edu.agh.fis.checkers.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Game extends Thread implements Runnable {
    private static final String DB_URL = "jdbc:sqlite:src/main/resources/checkers.db";

    private final int gameId;
    private final ClientHandler white;
    private final ClientHandler black;

    private final List<String> moves = new ArrayList<>();
    private boolean whiteTurn = true;
    private final LocalDateTime startTime;

    public Game(int gameId, ClientHandler white, ClientHandler black) {
        this.gameId = gameId;
        this.white = white;
        this.black = black;
        this.startTime = LocalDateTime.now();
        updateStartTime();
    }

    @Override
    public void run() {
        white.sendMessage("YOUR_TURN:WHITE");
        black.sendMessage("WAIT_TURN");

        try (
                BufferedReader whiteIn = white.getReader();
                BufferedWriter whiteOut = white.getWriter();
                BufferedReader blackIn = black.getReader();
                BufferedWriter blackOut = black.getWriter()
        ) {
            while (true) {
                ClientHandler current = whiteTurn ? white : black;
                ClientHandler opponent = whiteTurn ? black : white;
                BufferedReader in = whiteTurn ? whiteIn : blackIn;
                BufferedWriter out = whiteTurn ? whiteOut : blackOut;

//                in.skip(1);
//                in.mark(0);
//                while (in.ready()) {
//                    in.mark(0);
//                    in.skip(1);
//                }
//                in.reset();
                out.write("MOVE_OR_COMMAND");
                out.newLine();
                out.flush();

                String line = in.readLine();
                System.out.println("Received: " + line);
                if (line == null) {
                    abortGame();
                    break;
                }
                line = line.trim();

                if ("EXIT".equalsIgnoreCase(line)) {
                    finishGame(null);
                    break;
                }
                if ("PAUSE".equalsIgnoreCase(line)) {
                    pauseGame();
                    break;
                }

                // treat as generic move string
                moves.add((whiteTurn ? "W:" : "B:") + line);
                broadcastMove(line);
                whiteTurn = !whiteTurn;
            }
        } catch (IOException e) {
            e.printStackTrace();
            abortGame();
        }
        white.resume();
        black.resume();
    }

    private void broadcastMove(String move) throws IOException {
        white.sendMessage("OPPONENT_MOVE:" + move);
        black.sendMessage("OPPONENT_MOVE:" + move);
    }

    private void pauseGame() {
        persistGame("PAUSED");
        white.sendMessage("GAME_PAUSED");
        black.sendMessage("GAME_PAUSED");
    }

    private void finishGame(Integer winnerId) {
        persistGame("FINISHED", winnerId);
        white.sendMessage("GAME_FINISHED");
        black.sendMessage("GAME_FINISHED");
    }

    private void abortGame() {
        persistGame("ABORTED");
        white.sendMessage("GAME_ABORTED");
        black.sendMessage("GAME_ABORTED");
    }

    private void updateStartTime() {
        String sql = "UPDATE games SET start_time = ?, status = 'IN_PROGRESS' WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            ps.setInt(2, gameId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void persistGame(String status) {
        persistGame(status, null);
    }

    private void persistGame(String status, Integer winnerId) {
        LocalDateTime endTime = LocalDateTime.now();
        long duration = Duration.between(startTime, endTime).getSeconds();
        String movesStr = String.join(";", moves);

        String sql = "UPDATE games SET status = ?, end_time = ?, duration_seconds = ?, move_count = ?, moves = ?" +
                (winnerId != null ? ", winner_id = ?" : "") +
                " WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, status);
            ps.setString(idx++, endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            ps.setInt(idx++, (int) duration);
            ps.setInt(idx++, moves.size());
            ps.setString(idx++, movesStr);
            if (winnerId != null) {
                ps.setInt(idx++, winnerId);
            }
            ps.setInt(idx, gameId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
