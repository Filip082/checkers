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
    private Move nextMove;

    public Client() {
        board = new Board();
        nextMove = null;
    }

    @JSExport
    public static Client createNewGame() {
        return new Client();
    }

    @JSExport
    public static Client restoreGame(JSArray<String> moves) {
        Client client = new Client();

        for (int i = 0; i < moves.getLength(); ++i) {
            Move move = new Move(client.board, new LinkedList<>(
                    Arrays.stream(String.valueOf(moves.get(i)).split(" "))
                            .map(Position::new)
                            .toList()
            ));
            client.board.makeMove(move);
        }

        return client;
    }

    public interface BoardState extends JSObject {
        @JSProperty("biale")
        void setWhitePawns(JSArray<JSString> biale);

        @JSProperty("czarne")
        void setBlackPawns(JSArray<JSString> czarne);
    }

    @JSBody(script = "return {};")
    private static native BoardState createEmptyBoardState();

    @JSExport
    public BoardState getBoardState() {
        JSArray<JSString> whitePawnsPositions = new JSArray<>();
        JSArray<JSString> blackPawnsPositions = new JSArray<>();

        for (Pawn pawn : board.getPawns(Color.WHITE))
            whitePawnsPositions.push(JSString.valueOf(pawn.getSquare().getPosition().toString()));
        for (Pawn pawn : board.getPawns(Color.BLACK))
            blackPawnsPositions.push(JSString.valueOf(pawn.getSquare().getPosition().toString()));

        BoardState state = createEmptyBoardState();
        state.setWhitePawns(whitePawnsPositions);
        state.setBlackPawns(blackPawnsPositions);
        return state;
    }

    public interface MoveResult extends JSObject {
        @JSProperty("bicia")
        void setScore(int bicia);

        @JSProperty("zbite_pionki")
        void setCaptured(JSArray<JSString> zbite_pionki);
    }

    @JSBody(script = "return {};")
    private static native MoveResult createEmptyResult();

    @JSExport
    public MoveResult verifyMove(String input) {
        System.out.println("Java received move: " + input);
        Move move = new Move(board, new LinkedList<>(
                Arrays.stream(input.split(" "))
                        .map(Position::new)
                        .toList()
        ));
        System.out.println("Move created.");
        int bicia = move.getCaptured().size();
        List<Pawn> capturedPawns = move.getCaptured();

        MoveResult result = createEmptyResult();
        result.setScore(bicia);

        JSArray<JSString> jsCapturedArray = new JSArray<>();
        if (capturedPawns != null) {
            for (Pawn pawn : capturedPawns) {
                String position = pawn.getSquare().getPosition().toString();
                jsCapturedArray.push(JSString.valueOf(position));
            }
        }
        result.setCaptured(jsCapturedArray);

        nextMove = move;

        return result;
    }

    @JSExport
    public void applyMove() {
        board.makeMove(nextMove);
        nextMove = null;
    }

    // Opcjonalnie: metoda main, jeśli TeaVM jej wymaga do startu (zależy od configu)
    public static void main(String[] args) {
    }
}
