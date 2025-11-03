package iu.fx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import model.Tour;

public class FxBoardCanvas extends Canvas {

    private final Tour tourToAnimate;
    private final long animationDelayMillis;
    private final boolean useWhiteKnightPiece;

    private final int boardSize;
    private final int maxStepNumberInTour;

    private Timeline animationTimeline;
    private int currentAnimationStep;

    private static final int DEFAULT_CELL_PIXELS = 70;

    public FxBoardCanvas(Tour tourToAnimate,
                         long animationDelayMillis,
                         boolean useWhiteKnightPiece) {
        this.tourToAnimate = tourToAnimate;
        this.animationDelayMillis = animationDelayMillis;
        this.useWhiteKnightPiece = useWhiteKnightPiece;

        this.boardSize = tourToAnimate.getBoardSize();
        this.maxStepNumberInTour = computeMaxStepNumber(tourToAnimate);

        double preferredSize = Math.min(900, Math.max(360, boardSize * DEFAULT_CELL_PIXELS));
        setWidth(preferredSize);
        setHeight(preferredSize);

        this.animationTimeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(Duration.millis(Math.max(10, animationDelayMillis)), e -> {
            this.currentAnimationStep = Math.min(this.currentAnimationStep + 1, this.maxStepNumberInTour);
            drawCurrentFrame();
            if (this.currentAnimationStep >= this.maxStepNumberInTour) {
                this.animationTimeline.stop();
            }
        });
        this.animationTimeline.getKeyFrames().add(keyFrame);
        this.animationTimeline.setCycleCount(Timeline.INDEFINITE);

        this.currentAnimationStep = 0;
        drawCurrentFrame();
    }

    public double getPreferredWidth()  { return getWidth(); }
    public double getPreferredHeight() { return getHeight(); }

    public void startAnimation() { animationTimeline.playFromStart(); }

    /** Reinicia la animación desde el paso 0 y vuelve a reproducir. */
    public void restartAnimation() {
        this.currentAnimationStep = 0;
        drawCurrentFrame();
        animationTimeline.stop();
        animationTimeline.playFromStart();
    }

    private void drawCurrentFrame() {
        GraphicsContext g = getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        int cellSizePixels = (int) Math.floor(Math.min(getWidth(), getHeight()) / boardSize);
        int leftPaddingPixels = (int) ((getWidth()  - cellSizePixels * boardSize) / 2);
        int topPaddingPixels  = (int) ((getHeight() - cellSizePixels * boardSize) / 2);

        int[][] orderMatrix = tourToAnimate.getKnightMoveOrderMatrix();

        // Números del camino (gris claro)
        g.setFill(Color.GRAY);
        g.setFont(Font.font(Math.max(12, cellSizePixels * 0.28)));
        for (int rowIndex = 0; rowIndex < boardSize; rowIndex++) {
            for (int colIndex = 0; colIndex < boardSize; colIndex++) {
                int stepNumber = orderMatrix[rowIndex][colIndex];
                if (stepNumber > 0) {
                    String text = String.valueOf(stepNumber);
                    double textX = leftPaddingPixels + colIndex * cellSizePixels + cellSizePixels * 0.35;
                    double textY = topPaddingPixels  + rowIndex * cellSizePixels + cellSizePixels * 0.65;
                    g.fillText(text, textX, textY);
                }
            }
        }

        // Grid
        g.setStroke(Color.color(0.8, 0.8, 0.8));
        for (int i = 0; i <= boardSize; i++) {
            double x = leftPaddingPixels + i * cellSizePixels;
            double y = topPaddingPixels  + i * cellSizePixels;
            g.strokeLine(leftPaddingPixels, y, leftPaddingPixels + boardSize * cellSizePixels, y);
            g.strokeLine(x, topPaddingPixels, x, topPaddingPixels + boardSize * cellSizePixels);
        }

        // Caballo
        int[] currentKnightCell = findCellForStep(orderMatrix, Math.max(1, currentAnimationStep));
        if (currentKnightCell != null) {
            String knightGlyph = useWhiteKnightPiece ? "♘" : "♞";
            g.setFill(Color.BLACK);
            g.setFont(Font.font(Math.max(18, cellSizePixels * 0.6)));
            double glyphX = leftPaddingPixels + currentKnightCell[1] * cellSizePixels + cellSizePixels * 0.28;
            double glyphY = topPaddingPixels  + currentKnightCell[0] * cellSizePixels + cellSizePixels * 0.72;
            g.fillText(knightGlyph, glyphX, glyphY);
        }
    }

    private static int[] findCellForStep(int[][] orderMatrix, int stepNumber) {
        int n = orderMatrix.length;
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                if (orderMatrix[r][c] == stepNumber) return new int[]{r, c};
        return null;
    }

    private static int computeMaxStepNumber(Tour tour) {
        int[][] order = tour.getKnightMoveOrderMatrix();
        int max = 0;
        for (int[] row : order)
            for (int v : row)
                if (v > max) max = v;
        return Math.max(1, max);
    }
}
