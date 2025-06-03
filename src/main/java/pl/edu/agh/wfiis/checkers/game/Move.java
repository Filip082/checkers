package pl.edu.agh.wfiis.checkers.game;

import java.lang.Math;

public class Move {
    private Position origin;
    private Position destination;
    private Pawn movingPawn;
    private Pawn captured = null;

    public Move(Position origin, Position destination) throws InvalidMoveException {
        Board board = Board.getInstance();
        movingPawn = board.getSquare(origin).getPawn();
        if (movingPawn == null || board.getSquare(destination).getPawn() != null) {
            throw new InvalidMoveException();
        }
        int positiveDirection = (movingPawn.getColor() == Color.WHITE) ? 1 : -1;
        int moveX = destination.getX() - origin.getX();
        int moveY = destination.getY() - origin.getY();
        if (Math.abs(moveX) != Math.abs(moveY)) {
            throw new InvalidMoveException();
        }
        if (Math.abs(moveY) == 2) {
            Position midPosition = new Position(origin.getX() + moveX / 2,
                    origin.getY() + moveY / 2);
            captured = board.getSquare(midPosition).getPawn();
            if (captured == null || captured.getColor() == movingPawn.getColor()) {
                throw new InvalidMoveException();
            }
        } else if (moveY != positiveDirection || Math.abs(moveX) != 1) {
            throw new InvalidMoveException();
        }

        this.origin = origin;
        this.destination = destination;
    }

    public Position getOrigin() {
        return origin;
    }

    public Position getDestination() {
        return destination;
    }

    public Pawn getMovingPawn() {
        return movingPawn;
    }

    public Pawn getCaptured() {
        return captured;
    }
}
