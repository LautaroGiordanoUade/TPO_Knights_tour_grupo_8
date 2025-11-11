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

/**
 * Ventana JavaFX que muestra el tablero animado.
 *
 * Complejidad temporal por apertura: O(1) (crear escena/controles).
 * El costo por frame está en FxBoardCanvas (O(n^2)).
 */
public class FxBoardWindow {

    /**
     * Uso clásico (tours sin puntajes): mantiene comportamiento existente.
     * Complejidad temporal: O(1).
     */
    public static void showTourInNewWindow(Tour tourToShow,
                                           long animationDelayMillis,
                                           boolean useWhiteKnightPiece) {
        // delega al overload con matriz null (sin overlay de puntajes)
        showTourInNewWindow(tourToShow, animationDelayMillis, useWhiteKnightPiece, null);
    }

    /**
     * Overload para PD: si pointsMatrixOrNull != null, el canvas dibuja puntajes por celda
     * y HUD verde con score acumulado y movimientos hechos. Si es null, se comporta como clásico.
     * Complejidad temporal: O(1).
     */
    public static void showTourInNewWindow(Tour tourToShow,
                                           long animationDelayMillis,
                                           boolean useWhiteKnightPiece,
                                           int[][] pointsMatrixOrNull) {

        Runnable openWindowTask = () -> {
            Stage stage = new Stage();

            // Elegimos el constructor correcto del canvas según haya o no overlay de puntajes
            FxBoardCanvas canvas = (pointsMatrixOrNull == null)
                    ? new FxBoardCanvas(tourToShow, animationDelayMillis, useWhiteKnightPiece)
                    : new FxBoardCanvas(tourToShow, animationDelayMillis, useWhiteKnightPiece, pointsMatrixOrNull);

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

            // Un poquito más alto por el HUD verde (cuando hay PD)
            double extraHeight = (pointsMatrixOrNull == null) ? 44 : 60;

            Scene scene = new Scene(root, canvas.getWidth(), canvas.getHeight() + extraHeight);
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
