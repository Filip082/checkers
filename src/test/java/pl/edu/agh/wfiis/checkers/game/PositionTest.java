package pl.edu.agh.wfiis.checkers.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {
    @Test
    void PositionFromXY() {
        Position position = new Position(1, 1);
        assertEquals(1, position.getX());
        assertEquals(1, position.getY());
    }

    @Test
    void PositionFromString() {
        Position position = new Position("A1");
        assertEquals(1, position.getX());
        assertEquals(1, position.getY());
    }

    @Test
    void Equality() {
        Position position = new Position("A1");
        Position position1 = new Position("A1");
        assertEquals(position, position1);
    }
}
