package pl.edu.agh.wfiis.checkers.game;

public class Pawn {
    private final Color color;
    private Board.Square square;
    private boolean isDame = false;

    public Pawn(Color color) {
        this.color = color;
    }

    Color getColor() {
        return color;
    }

    void setSquare(Board.Square square) {
        this.square = square;
    }

    Board.Square getSquare() {
        return square;
    }

    boolean isDame() {
        return isDame;
    }

    @Override
    public String toString() {
        return (color == Color.BLACK ? "\u001B[31m" : "") + " ● ";
    }
}
