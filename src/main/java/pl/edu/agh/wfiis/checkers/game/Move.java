package pl.edu.agh.wfiis.checkers.game;

import java.lang.Math;
import java.util.LinkedList;
import java.util.List;

public class Move {
    private final Position origin;
    private final Position destination;
    private final Pawn movingPawn;
    private Pawn captured = null;
    private Move nextMove = null;

    private Move(Position origin, Position destination, Move prev) throws InvalidMoveException {
        if (origin == null || destination == null) {
            throw new InvalidMoveException();
        }
        Board board = Board.getInstance();
        movingPawn = (prev != null) ? prev.getMovingPawn() : board.getSquare(origin).getPawn();
        if (movingPawn == null || board.getSquare(destination).getPawn() != null) {
            throw new InvalidMoveException();
        }
        int positiveDirection = (movingPawn.getColor() == Color.WHITE) ? 1 : -1;
        int moveX = destination.getX() - origin.getX();
        int moveY = destination.getY() - origin.getY();
        if (Math.abs(moveX) != Math.abs(moveY)) {
            throw new InvalidMoveException();
        }
        if (movingPawn.isDame()) {

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

    private Move(List<Position> moveSequence, Move prev) throws InvalidMoveException {
        this(moveSequence.removeFirst(), (!moveSequence.isEmpty()) ? moveSequence.getFirst() : null, prev);
        if (moveSequence.size() > 1) {
            if (this.captured == null)
                throw new InvalidMoveException();
            nextMove = new Move(moveSequence, this);
            if (nextMove.getCaptured().isEmpty())
                throw new InvalidMoveException();
        }
    }

    public Move(List<Position> moveSequence) throws InvalidMoveException {
        this(moveSequence, null);
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

    public List<Pawn> getCaptured() {
        List<Pawn> capturedPawns = new LinkedList<>();
        if (captured != null) {
            capturedPawns.add(captured);
        }
        if (nextMove != null) {
            capturedPawns.addAll(nextMove.getCaptured());
        }
        return capturedPawns;
    }

    public Move getNextMove() {
        return nextMove;
    }
}
