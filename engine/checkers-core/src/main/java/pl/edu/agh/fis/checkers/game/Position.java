package pl.edu.agh.fis.checkers.game;

//import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Position {
    private final int x;
    private final int y;

    private int Normalize(int value) {
        return ((value - 1) % 8 + 8) % 8 + 1;
    }

    private int CalculateColumn(int index) {
        return 2 * (index % 4) + (index / 4) % 2 + 1;
    }

    private int CalculateRow(int index) {
        return index / 4 + 1;
    }

    public Position(int x, int y) {
        if ((x + y) % 2 != 0) {
            throw new ForbiddenPositionException("Position (" + x + ", " + y + ") is not allowed");
        }
        this.x = Normalize(x);
        this.y = Normalize(y);
    }

    public Position(int index) {
        if (index < 0 || 31 < index) {
            throw new ForbiddenPositionException("Index (" + index + ") is out of bounds");
        }
        x = Normalize(CalculateColumn(index));
        y = Normalize(CalculateRow(index));
    }

    public Position(String string) {
        if (string.length() != 2) {
            throw new ForbiddenPositionException("String \"" + string + "\" is not a valid position");
        }
        x = Normalize(string.charAt(0) - 64);
        y = Normalize(string.charAt(1) - 48);
        if ((x + y) % 2 != 0) {
            throw new ForbiddenPositionException("Position " + string + " is not allowed");
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int get64() {
        return (y - 1) * 8 + x - 1;
    }

    public int get32() {
        return this.get64() / 2;
    }

    @Override
    public String toString() {
        return "" + (char) ('A' + getX() - 1) + getY();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
