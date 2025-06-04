module pl.edu.agh.fis.checkers {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    opens pl.edu.agh.fis.checkers to javafx.fxml;
    exports pl.edu.agh.fis.checkers;
    exports pl.edu.agh.fis.checkers.game;
    opens pl.edu.agh.fis.checkers.game to javafx.fxml;
}
