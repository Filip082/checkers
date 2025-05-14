package pl.edu.agh.wfiis.checkers.game;

public class Game {
    public static void main(String[] args) {
        Board plansza = new Board();
        Pawn pawn = plansza.getSquare(new Position(7, 3)).getPawn();
        if (pawn != null) {
            pawn.move(plansza.getSquare(new Position(6, 4)));
        }
        System.out.println(plansza);
//        System.out.println(new Position(5, 7).get32());
    }
}
