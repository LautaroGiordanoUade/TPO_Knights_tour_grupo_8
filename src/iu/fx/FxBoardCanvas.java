package iu.fx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import model.Tour;

/**
 * Canvas del tablero con animación del caballo.
 * - Tours clásicos: ruta numerada + caballo.
 * - Caso DP (pasando pointsMatrix): además pinta los puntajes por celda y
 *   muestra arriba, en una franja dedicada, el score acumulado y los movimientos hechos.
 *
 * Complejidad por frame: O(N^2).
 */
public class FxBoardCanvas extends Canvas {

    private final Tour tourToAnimate;
    private final long animationDelayMillis;
    private final boolean useWhiteKnightPiece;
    private final int[][] pointsMatrixOrNull;   // solo DP (si null => clásico)

    private final int n;
    private final int maxStep; // = casillas visitadas en esta ruta (t+1)

    private Timeline timeline;
    private int step; // step visible 1..maxStep

    private static final int CELL_PX = 70;
    private static final int TOP_HUD_HEIGHT = 50; // espacio dedicado arriba para el HUD

    // Tours clásicos
    public FxBoardCanvas(Tour tour, long delayMs, boolean white) {
        this(tour, delayMs, white, null);
    }

    // DP (con overlay de puntajes)
    public FxBoardCanvas(Tour tour, long delayMs, boolean white, int[][] pointsMatrix) {
        this.tourToAnimate = tour;
        this.animationDelayMillis = delayMs;
        this.useWhiteKnightPiece = white;
        this.pointsMatrixOrNull = pointsMatrix;

        this.n = tour.getBoardSize();
        this.maxStep = computeMaxStepNumber(tour);

        // Si es modo DP, añadimos espacio arriba para el HUD
        double baseSize = Math.min(900, Math.max(360, n * CELL_PX));
        double totalHeight = baseSize + (pointsMatrixOrNull != null ? TOP_HUD_HEIGHT : 0);
        setWidth(baseSize);
        setHeight(totalHeight);

        timeline = new Timeline();
        KeyFrame kf = new KeyFrame(Duration.millis(Math.max(10, animationDelayMillis)), e -> {
            if (step < maxStep) step++;
            draw();
            if (step >= maxStep) timeline.stop();
        });
        timeline.getKeyFrames().add(kf);
        timeline.setCycleCount(Timeline.INDEFINITE);

        this.step = 1;      // <<< arranca en 1 para que se vea el caballo desde el inicio
        draw();
    }

    public double getPreferredWidth()  { return getWidth(); }
    public double getPreferredHeight() { return getHeight(); }
    public void startAnimation() { timeline.playFromStart(); }
    public void restartAnimation() { step = 1; draw(); timeline.stop(); timeline.playFromStart(); }

    // ---- dibujo por frame ----
    private void draw() {
        GraphicsContext g = getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Calcular posición del tablero (más abajo si hay HUD)
        int cell = (int) Math.floor(Math.min(getWidth(), getWidth()) / n);
        int left = (int) ((getWidth() - cell * n) / 2);
        int top = (pointsMatrixOrNull != null) ? TOP_HUD_HEIGHT :
                (int) ((getHeight() - cell * n) / 2);

        int[][] order = tourToAnimate.getKnightMoveOrderMatrix();

        // --- HUD superior (solo en modo DP) ---
        if (pointsMatrixOrNull != null) {
            long scoreSoFar = accumulatedScore(order, pointsMatrixOrNull, step);
            String hudText = "Score acumulado: " + scoreSoFar
                    + "   |   Movs: " + Math.max(0, step - 1) + "/" + (maxStep - 1);

            // Fondo sutil para el HUD
            g.setFill(Color.rgb(46, 204, 113, 0.12)); // verde muy transparente
            g.fillRect(0, 0, getWidth(), TOP_HUD_HEIGHT);

            g.setFill(Color.web("#27ae60")); // verde más oscuro que #2ecc71
            g.setFont(Font.font(Math.max(16, cell * 0.30)));

            // Centrado preciso
            double textWidth = g.getFont().getSize() * hudText.length() * 0.6;
            double textX = Math.max(10, (getWidth() - textWidth) / 2);
            g.fillText(hudText, textX, TOP_HUD_HEIGHT - 15);
        }

        // (DP) puntajes por celda (overlay suave)
        if (pointsMatrixOrNull != null) {
            g.setFill(Color.rgb(0, 0, 0, 0.22));
            g.setFont(Font.font(Math.max(11, cell * 0.22)));
            for (int r = 0; r < n; r++)
                for (int c = 0; c < n; c++) {
                    String txt = Integer.toString(pointsMatrixOrNull[r][c]);
                    double x = left + c * cell + cell * 0.12;
                    double y = top  + r * cell + cell * 0.28;
                    g.fillText(txt, x, y);
                }
        }

        // Números de la ruta (gris)
        g.setFill(Color.GRAY);
        g.setFont(Font.font(Math.max(12, cell * 0.28)));
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++) {
                int s = order[r][c];
                if (s > 0) {
                    String txt = String.valueOf(s);
                    double x = left + c * cell + cell * 0.35;
                    double y = top  + r * cell + cell * 0.65;
                    g.fillText(txt, x, y);
                }
            }

        // Grid
        g.setStroke(Color.color(0.8, 0.8, 0.8));
        for (int i = 0; i <= n; i++) {
            double x = left + i * cell;
            double y = top  + i * cell;
            g.strokeLine(left, y, left + n * cell, y);
            g.strokeLine(x, top, x, top + n * cell);
        }

        // Caballo en la celda del step actual
        int[] pos = findCellForStep(order, step);
        if (pos != null) {
            String glyph = useWhiteKnightPiece ? "♘" : "♞";
            g.setFill(Color.BLACK);
            g.setFont(Font.font(Math.max(18, cell * 0.6)));
            double gx = left + pos[1] * cell + cell * 0.28;
            double gy = top  + pos[0] * cell + cell * 0.72;
            g.fillText(glyph, gx, gy);
        }
    }

    // ---- helpers ----
    private static int[] findCellForStep(int[][] order, int s) {
        int n = order.length;
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                if (order[r][c] == s) return new int[]{r, c};
        return null;
    }

    private static int computeMaxStepNumber(Tour tour) {
        int[][] o = tour.getKnightMoveOrderMatrix();
        int max = 0;
        for (int[] row : o) for (int v : row) if (v > max) max = v;
        return Math.max(1, max);
    }

    private static long accumulatedScore(int[][] order, int[][] points, int lastStep) {
        if (points == null || lastStep <= 0) return 0L;
        int n = order.length; long total = 0L;
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++) {
                int s = order[r][c];
                if (s >= 1 && s <= lastStep) total += points[r][c];
            }
        return total;
    }
}