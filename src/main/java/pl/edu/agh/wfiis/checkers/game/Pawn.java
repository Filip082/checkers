package pl.edu.agh.wfiis.checkers.game;

public class Pawn {
    private final Color color;
    private Position position;
    private boolean isDame = false;

    public Pawn(Color color, Position position) {
        this.color = color;
        this.position = position;
    }

    public Color getColor() {
        return color;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean isDame() {
        return isDame;
    }

    @Override
    public String toString() {
        return (color == Color.BLACK ? "\u001B[31m" : "") + " ● ";
    }
}
