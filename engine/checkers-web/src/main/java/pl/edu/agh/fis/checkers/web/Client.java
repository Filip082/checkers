package pl.edu.agh.fis.checkers.web;

import org.teavm.jso.*;
import pl.edu.agh.fis.checkers.game.*;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSString;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@JSClass(name = "Client")
public class Client {
    private final Board board;
    private List<JSPawn> capturedPawns;
    private Move nextMove;

    public Client() {
        board = new Board();
        nextMove = null;
        capturedPawns = new LinkedList<>();
    }

    @JSExport
    public static Client createNewGame() {
        return new Client();
    }

    @JSExport
    public static Client restoreGame(JSArray<String> moves) {
        Client client = new Client();

        for (int i = 0; i < moves.getLength() - 1; ++i) {
            Move move = new Move(client.board, new LinkedList<>(
                    Arrays.stream(String.valueOf(moves.get(i)).split(" "))
                            .map(Position::new)
                            .toList()
            ));
            client.board.makeMove(move);
        }

        Move move = new Move(client.board, new LinkedList<>(
                Arrays.stream(String.valueOf(moves.get(moves.getLength() - 1)).split(" "))
                        .map(Position::new)
                        .toList()
        ));

        for (Pawn pawn : move.getCaptured())
            client.capturedPawns.add(constructPawn(pawn));

        client.board.makeMove(move);

        return client;
    }

    public interface JSPawn extends JSObject {
        @JSProperty("position")
        void setPosition(JSString position);

        @JSProperty("color")
        void setColor(JSString color);

        @JSProperty("dame")
        void setDame(boolean dame);

        @JSProperty("captured")
        void setCaptured(boolean captured);
    }

    @JSBody(script = "return {};")
    private static native JSPawn createEmptyPawn();

    private static JSPawn constructPawn(Pawn pawn) {
        JSPawn temp = createEmptyPawn();
        temp.setPosition(JSString.valueOf(pawn.getSquare().getPosition().toString()));
        temp.setColor(JSString.valueOf(pawn.getColor() == Color.WHITE ? "Biały" : "Czarny"));
        temp.setDame(pawn.isDame());
        temp.setCaptured(false);

        return temp;
    }

    @JSExport
    public JSArray<JSPawn> getBoardState() {
        JSArray<JSPawn> pawns = new JSArray<>();

        for (JSPawn pawn : capturedPawns) {
            pawn.setCaptured(true);
            pawns.push(pawn);
        }

        for (Pawn pawn : board.getPawns(Color.WHITE))
            pawns.push(constructPawn(pawn));
        for (Pawn pawn : board.getPawns(Color.BLACK))
            pawns.push(constructPawn(pawn));

        return pawns;
    }

    @JSExport
    public JSString verifyMove(String input) {
        System.out.println("Java received move: " + input);
        Move move = new Move(board, new LinkedList<>(
                Arrays.stream(input.split(" "))
                        .map(Position::new)
                        .toList()
        ));
        System.out.println("Move created.");

        nextMove = move;

        return JSString.valueOf(move.getMovingPawn().getColor() == Color.WHITE ? "Biały" : "Czarny");
    }

    @JSExport
    public void applyMove() {
        capturedPawns = new LinkedList<>();
        for (Pawn pawn : nextMove.getCaptured())
            capturedPawns.add(constructPawn(pawn));
        board.makeMove(nextMove);
        nextMove = null;
    }

    // Opcjonalnie: metoda main, jeśli TeaVM jej wymaga do startu (zależy od configu)
    public static void main(String[] args) {
    }
}
