package pl.edu.agh.wfiis.checkers.game;

public class Position {
    private int x;
    private int y;
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public int getX() {return x;}
    public int setX(int x) { this.x = x % 7 + 1; return this.x; }
    public int getY() {return y;}
    public int setY(int y) { this.y = y % 7 + 1; return this.y; }
}
