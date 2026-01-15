package pl.edu.agh.fis.checkers.game;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

public class MoveTest {
    @Test
    void FirtsMove() {
        Board board = new Board();
        Move move = new Move(board, new LinkedList<>(
                Arrays.stream("A3 B4".split(" "))
                        .map(Position::new)
                        .toList()
        ));
        assertInstanceOf(Move.class, move);
    }
}
