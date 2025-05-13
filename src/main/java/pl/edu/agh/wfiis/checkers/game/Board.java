package pl.edu.agh.wfiis.checkers.game;

import java.util.List;
import java.util.ArrayList;

public class Board {
    List<Pawn> whitePawns;
    List<Pawn> blackPawns;

    class Square {
        Pawn pawn;

        boolean isEmpty() {
            return pawn == null;
        }

        Square() {
            pawn = null;
        }

        Square(Pawn pawn) {
            this.pawn = pawn;
        }

        @Override
        public String toString() {
            return "\u001B[40m" + (pawn == null ? "   " : pawn) + "\u001B[0m";
        }
    }

    List<Square> squares = new ArrayList<>(32);

    public Board() {
        whitePawns = new ArrayList<>(12);
        blackPawns = new ArrayList<>(12);
        for (int i = 0; i < 32; i++) {
            Pawn pawn = null;
            if (i < 12) {
                pawn = new Pawn(Color.WHITE, new Position(0, 0));
                whitePawns.add(pawn);
            } else if (i >= 20) {
                pawn = new Pawn(Color.BLACK, new Position(0, 0));
                blackPawns.add(pawn);
            }
            squares.add(new Square(pawn));
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
                    sb.append(squares.get((8 * i + j) / 2));
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
