package pl.edu.agh.fis.checkers.server;

import java.util.List;

class Game {
    private final String id;
    private final ClientHandler player1;
    private final ClientHandler player2;
    private List<String> moves;

    Game(String id, ClientHandler player1, ClientHandler player2) {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
    }

    ClientHandler getPlayer1() {
        return player1;
    }

    ClientHandler getPlayer2() {
        return player2;
    }
}
