package pl.edu.agh.wfiis.checkers.game;

import java.util.List;
import java.util.ArrayList;

public class Board {
    private static Board instance;
    private List<Pawn> whitePawns;
    private List<Pawn> blackPawns;
    private final List<Square> squares = new ArrayList<>(32);

    public final class Square {
        private final Position position;
        private Pawn pawn;

        public Square(Pawn pawn, Position position) {
            if (pawn != null)
                pawn.setSquare(this);
            this.pawn = pawn;
            this.position = position;
        }

        public Pawn getPawn() {
            return pawn;
        }

        public void setPawn(Pawn pawn) {
            this.pawn = pawn;
        }

        public Position getPosition() {
            return position;
        }

        public boolean isEmpty() {
            return pawn == null;
        }

        @Override
        public String toString() {
            return "\u001B[40m" + (pawn == null ? "   " : pawn) + "\u001B[0m";
        }
    }

    private Board() {
        initialize();
    }

    private void initialize() {
        whitePawns = new ArrayList<>(12);
        blackPawns = new ArrayList<>(12);
        for (int i = 0; i < 32; i++) {
            Pawn pawn = null;
            if (i < 12) {
                pawn = new Pawn(Color.WHITE);
                whitePawns.add(pawn);
            } else if (i >= 20) {
                pawn = new Pawn(Color.BLACK);
                blackPawns.add(pawn);
            }
            squares.add(new Square(pawn, new Position(i)));
        }
    }

    public static Board getInstance() {
        if (instance == null) {
            instance = new Board();
        }
        return instance;
    }

    public void reset() {
        initialize();
    }

    public Square getSquare(Position position) {
        return squares.get(position.get32());
    }

    public int[] getScore() {
        return new int[]{12 - blackPawns.size(), 12 - whitePawns.size()};
    }

    public void makeMove(Move move) {
        move.getMovingPawn().setSquare(this.getSquare(move.getDestination()));
        this.getSquare(move.getDestination()).setPawn(move.getMovingPawn());
        this.getSquare(move.getOrigin()).setPawn(null);

        List<Pawn> captures = move.getCaptured();
        if (!captures.isEmpty()) {
            Pawn captured = captures.getFirst();
            captured.getSquare().setPawn(null);
            captured.setSquare(null);
            if (captured.getColor() == Color.WHITE)
                whitePawns.remove(captured);
            else
                blackPawns.remove(captured);
        }

        if (move.getNextMove() != null) {
            this.makeMove(move.getNextMove());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 7; i >= 0; i--) {
            sb.append(" ");
            sb.append(i + 1);
            sb.append(" ");
            for (int j = 0; j < 8; j++) {
                if ((i + j) % 2 == 0) {
                    sb.append(squares.get(new Position(j + 1, i + 1).get32()));
                } else {
                    sb.append("\u001B[47m   \u001B[0m");
                }
            }
            sb.append("\n");
        }
        sb.append("    A  B  C  D  E  F  G  H ");
        return sb.toString();
    }
}
