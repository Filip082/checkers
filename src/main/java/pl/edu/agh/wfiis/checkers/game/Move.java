package pl.edu.agh.wfiis.checkers.game;

import java.lang.Math;

public class Move {
    private Position origin;
    private Position destination;
    private Pawn captured = null;

    public Move(Position origin, Position destination) throws InvalidMoveException {
        Board board = Board.getInstance();
        if (board.getSquare(origin).getPawn() == null || board.getSquare(destination).getPawn() != null) {
            throw new InvalidMoveException();
        }
        int moveX = destination.getX() - origin.getX();
        int moveY = destination.getY() - origin.getY();
        if (Math.abs(moveX) != Math.abs(moveY)) {
            throw new InvalidMoveException();
        }
        if (Math.abs(moveY) == 2) {
            Position midPosition = new Position(origin.getX() + moveX,
                    origin.getY() + moveY);
            Board.Square mid = board.getSquare(midPosition);
            if (mid.getPawn() == null) {
                throw new InvalidMoveException();
            }
        } else if (moveY != 1 || Math.abs(moveX) != 1) {
            throw new InvalidMoveException();
        }

        this.origin = origin;
        this.destination = destination;
    }
}
