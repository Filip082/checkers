package pl.edu.agh.wfiis.checkers.game;//import java.Math

enum Player {WHITE, BLACK}

public class DeprecatedBoard {
    private int whitePawns = (int) Math.pow(2,12) - 1;
    private int blackPawns = ((int) Math.pow(2,12) - 1) << 20;
    private int whiteDames = 0;
    private int blackDames = 0;

    @Override
    public String toString() {
        return "pl.edu.agh.wfiis.checkers.game.Board{" +
                "\n\twhitePawns=" + String.format("%32s", Integer.toBinaryString(whitePawns)).replace(' ', '0') +
                ",\n\tblackPawns=" + String.format("%32s", Integer.toBinaryString(blackPawns)).replace(' ', '0') +
                ",\n\twhiteDames=" + String.format("%32s", Integer.toBinaryString(whiteDames)).replace(' ', '0') +
                ",\n\tblackDames=" + String.format("%32s", Integer.toBinaryString(blackDames)).replace(' ', '0') +
                "\n}";
    }
}
