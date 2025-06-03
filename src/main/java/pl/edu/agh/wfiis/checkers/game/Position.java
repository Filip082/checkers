package pl.edu.agh.wfiis.checkers.game;

import java.util.Objects;

public class Position {
    private int x;
    private int y;

    public Position(int x, int y) {
        if ((x + y) % 2 != 0) {
            return;
        }
        this.setX(x);
        this.setY(y);
    }

    public Position(int index) {
        this.set32(index);
    }

    public Position(String string) {
        if (string.length() != 2) {
            return;
        }
        this.setX(string.charAt(0) - 64);
        this.setY(string.charAt(1) - 48);
    }

    public int getX() {
        return x;
    }

    public int setX(int x) {
        this.x = ((x - 1) % 8 + 8) % 8 + 1;
        return this.x;
    }

    public int getY() {
        return y;
    }

    public int setY(int y) {
        this.y = ((y - 1) % 8 + 8) % 8 + 1;
        return this.y;
    }

    public int get64() {
        return (y - 1) * 8 + x - 1;
    }

    public int get32() {
        return this.get64() / 2;
    }

    public Position set32(int index) {
        index %= 32;
        setX(2 * (index % 4) + (index / 4) % 2 + 1);
        setY(index / 4 + 1);
        return this;
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
