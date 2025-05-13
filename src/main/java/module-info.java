module pl.edu.agh.wfiis.checkers {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    opens pl.edu.agh.wfiis.checkers to javafx.fxml;
    exports pl.edu.agh.wfiis.checkers;
    exports pl.edu.agh.wfiis.checkers.game;
    opens pl.edu.agh.wfiis.checkers.game to javafx.fxml;
}
