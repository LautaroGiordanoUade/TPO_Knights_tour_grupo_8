package iu.fx;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.Tour;

public class FxBoardWindow {

    public static void showTourInNewWindow(Tour tourToShow,
                                           long animationDelayMillis,
                                           boolean useWhiteKnightPiece) {

        Runnable openWindowTask = () -> {
            Stage stage = new Stage();
            iu.fx.FxBoardCanvas canvas = new iu.fx.FxBoardCanvas(tourToShow, animationDelayMillis, useWhiteKnightPiece);

            // Centro: Canvas dentro de StackPane
            StackPane centerPane = new StackPane(canvas);

            // Abajo: barra con botón "Repetir"
            Button repeatButton = new Button("Repetir");
            repeatButton.setOnAction(e -> canvas.restartAnimation());

            HBox bottomBar = new HBox(repeatButton);
            bottomBar.setSpacing(10);
            bottomBar.setPadding(new Insets(8));

            // Layout general
            BorderPane root = new BorderPane();
            root.setCenter(centerPane);
            root.setBottom(bottomBar);

            Scene scene = new Scene(root, canvas.getWidth(), canvas.getHeight() + 44);
            stage.setTitle("Knight's Tour (JavaFX)");
            stage.setScene(scene);
            stage.show();

            canvas.startAnimation();
        };

        if (Platform.isFxApplicationThread()) {
            openWindowTask.run();
        } else {
            Platform.runLater(openWindowTask);
        }
    }
}
