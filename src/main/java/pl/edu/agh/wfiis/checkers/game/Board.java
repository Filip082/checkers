package pl.edu.agh.wfiis.checkers.game;

import java.util.List;
import java.util.ArrayList;

public class Board {
    List<Pawn> whitePawns;
    List<Pawn> blackPawns;
    List<Square> squares = new ArrayList<>(32);

    public class Square {
        private Position position;
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

        public boolean isEmpty() {
            return pawn == null;
        }

        @Override
        public String toString() {
            return "\u001B[40m" + (pawn == null ? "   " : pawn) + "\u001B[0m";
        }
    }

    public Board() {
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

    public Square getSquare(Position position) {
        return squares.get(position.get32());
    }

    public Pawn getPawn(Color c, int index) {
        if (c == Color.WHITE)
            return whitePawns.get(index % 12);
        else return blackPawns.get(index % 12);
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
        sb.append("    A  B  C  D  E  F  G  H\n");
        return sb.toString();
    }
}
