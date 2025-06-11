package pl.edu.agh.fis.checkers.game;

import java.io.Console;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Game {
    private Color currentPlayerColor = Color.WHITE;

    private static String Prompt(Color color) {
        return "\033[0m"
                + (color == Color.WHITE ? "\033[37m" : "\033[31m")
                + "\uE0B6"
                + (color == Color.WHITE ? "\033[47m\033[30m" : "\033[41m\033[37m")
                + "\033[1mPlayer "
                + (color == Color.WHITE ? "1" : "2")
                + "\033[0m"
                + (color == Color.WHITE ? "\033[37m" : "\033[31m")
                + "\uE0B4\033[0m\033[32m ";
    }

    //Works best with NerdFont installed
    //https://www.nerdfonts.com/font-downloads
    //Does NOT work properly in IntelliJ's run environment
    public static void main(String[] args) {
        Game game = new Game();
        Console console = System.console();
        String message = "Example move: A3 B4";
        int[] score = Board.getInstance().getScore();

        System.out.print("\033[2J\033[H\033[s");
        while (score[0] < 12 && score[1] < 12) {
            System.out.print("\033[u");
            System.out.println(Board.getInstance());
            System.out.println("\033[K" + message);
            message = " ● \033[33m" + score[0] + " : " + score[1] + "\033[31m ●\033[0m";
            System.out.print("\nType \033[32mq\033[0m to quit\r\033[A");

            System.out.print("\033[K");
            String input = console.readLine(Prompt(game.currentPlayerColor)).toUpperCase();
            if (input.contains("S")) {
                message = "● \033[33m" + score[0] + " : " + score[1] + "\033[31m ●\033[0m";
                continue;
            } else if (input.contains("EXIT") || input.contains("Q")) {
                break;
            }

            Move move;
            try {
                move = new Move(new LinkedList<>(
                        Arrays.stream(input.split(" "))
                                .map(Position::new)
                                .toList()
                ));
                if (move.getMovingPawn().getColor() != game.currentPlayerColor) {
                    throw new InvalidMoveException("Trying to move opponent's pawn");
                }
                if (!move.getCaptured().isEmpty()) {
                    StringBuilder captured = new StringBuilder();
                    captured.append("Pawn")
                            .append(move.getCaptured().size() == 1 ? "" : "s")
                            .append(" captured at ");
                    List<Pawn> captures = move.getCaptured();
                    Pawn lastCaptured = captures.removeLast();
                    for (Pawn pawn : captures) {
                        captured.append(pawn.getSquare().getPosition()).append(", ");
                    }
                    captured.append(lastCaptured.getSquare().getPosition());
                    message = captured.toString();
                }
            } catch (InvalidMoveException e) {
                message = "Invalid move. Try again. " + e.getMessage();
                continue;
            } catch (ForbiddenPositionException e) {
                message = e.getMessage();
                continue;
            }
            Board.getInstance().makeMove(move);
            score = Board.getInstance().getScore();
            game.currentPlayerColor = game.currentPlayerColor == Color.WHITE ? Color.BLACK : Color.WHITE;
        }

        System.out.print("\033[u");
        System.out.println(Board.getInstance());
        System.out.println("\033[K" + " ● \033[33m" + score[0] + " : " + score[1] + "\033[31m ●\033[0m\033[K");
        if (score[0] == score[1]) {
            System.out.println("\033[32m\uE0B6\033[30m\033[42mTie\033[0m\033[32m\uE0B4\033[0m\033[K");
        } else {
            System.out.println(Prompt(score[1] < score[0] ? Color.WHITE : Color.BLACK)
                    + "\033[2D"
                    + (score[1] < score[0] ? "\033[37m" : "\033[31m")
                    + "\033[43m\uE0B4\033[30m wins! \uF527\033[0m\033[33m\uE0B4\033[0m\033[K");
        }
    }
}
