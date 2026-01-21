package pl.edu.agh.fis.checkers.game;

public class Pawn {
    private final Color color;
    private Board.Square square;
    private boolean isDame = false;

    public Pawn(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    void setSquare(Board.Square square) {
        if (this.square != null) {
            this.square.setPawn(null);
        }
        this.square = square;
        if (this.square != null) {
            this.square.setPawn(this);
        }
    }

    public Board.Square getSquare() {
        return square;
    }

    void promote() {
        isDame = true;
    }

    public boolean isDame() {
        return isDame;
    }

    @Override
    public String toString() {
        return (color == Color.BLACK ? "\u001B[31m" : "") + (isDame() ? " ◉ " : " ● ");
    }
}
