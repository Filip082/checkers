package pl.edu.agh.fis.checkers.game;

public class ForbiddenPositionException extends RuntimeException {
    public ForbiddenPositionException(String message) {
        super(message);
    }
}
