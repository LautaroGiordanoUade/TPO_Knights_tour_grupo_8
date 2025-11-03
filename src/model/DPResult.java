package model;

public class DPResult {
    /** orderMatrixWithOptimalPath[row][col] = 1..(k+1) solo en la ruta óptima; 0 si no visitado */
    public final int[][] orderMatrixWithOptimalPath;
    /** pointsMatrix[row][col] = puntaje de la casilla */
    public final int[][] pointsMatrix;
    public final int boardSize;
    public final int moveCountK;
    public final int bestAccumulatedScore;

    public DPResult(int[][] orderMatrixWithOptimalPath,
                    int[][] pointsMatrix,
                    int moveCountK,
                    int bestAccumulatedScore) {
        this.orderMatrixWithOptimalPath = orderMatrixWithOptimalPath;
        this.pointsMatrix = pointsMatrix;
        this.boardSize = orderMatrixWithOptimalPath.length;
        this.moveCountK = moveCountK;
        this.bestAccumulatedScore = bestAccumulatedScore;
    }

    public Tour toTour() {
        return new Tour(orderMatrixWithOptimalPath);
    }
}
