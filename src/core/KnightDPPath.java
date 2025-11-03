package core;

import model.DPResult;

/**
 * Parte 3 – Programación Dinámica (tablas) con reconstrucción de camino.
 * Dado un tablero de puntajes y una posición inicial, obtiene un camino de exactamente k movimientos
 * maximizando la suma.
 *
 * Complejidad temporal: O(k * boardSize^2 * 8).
 * Complejidad espacial: O(k * boardSize^2) para DP + padres.
 */
public class KnightDPPath {

    private static final int[] knightDeltaRow = {-2,-2,-1,-1, 1, 1, 2, 2};
    private static final int[] knightDeltaCol = {-1, 1,-2, 2,-2, 2,-1, 1};

    public static DPResult solveMaxScorePathUsingDP(int[][] pointsMatrix,
                                                    int moveCountK,
                                                    int startRowIndex,
                                                    int startColumnIndex) {
        int boardSize = pointsMatrix.length;
        final int NEG_INF = Integer.MIN_VALUE / 4;

        int[][][] dpMaxScoreAtStepAndCell = new int[moveCountK + 1][boardSize][boardSize];
        int[][][] parentRowAtStepAndCell  = new int[moveCountK + 1][boardSize][boardSize];
        int[][][] parentColAtStepAndCell  = new int[moveCountK + 1][boardSize][boardSize];

        // init
        for (int rowIndex = 0; rowIndex < boardSize; rowIndex++)
            for (int colIndex = 0; colIndex < boardSize; colIndex++) {
                dpMaxScoreAtStepAndCell[0][rowIndex][colIndex] = NEG_INF;
                parentRowAtStepAndCell[0][rowIndex][colIndex] = -1;
                parentColAtStepAndCell[0][rowIndex][colIndex] = -1;
            }
        dpMaxScoreAtStepAndCell[0][startRowIndex][startColumnIndex] = pointsMatrix[startRowIndex][startColumnIndex];

        // relax
        for (int stepIndex = 1; stepIndex <= moveCountK; stepIndex++) {
            for (int rowIndex = 0; rowIndex < boardSize; rowIndex++)
                for (int colIndex = 0; colIndex < boardSize; colIndex++) {
                    dpMaxScoreAtStepAndCell[stepIndex][rowIndex][colIndex] = NEG_INF;
                    parentRowAtStepAndCell[stepIndex][rowIndex][colIndex] = -1;
                    parentColAtStepAndCell[stepIndex][rowIndex][colIndex] = -1;
                }

            for (int prevRowIndex = 0; prevRowIndex < boardSize; prevRowIndex++)
                for (int prevColIndex = 0; prevColIndex < boardSize; prevColIndex++) {
                    int prevScore = dpMaxScoreAtStepAndCell[stepIndex - 1][prevRowIndex][prevColIndex];
                    if (prevScore == NEG_INF) continue;

                    for (int moveDeltaIndex = 0; moveDeltaIndex < 8; moveDeltaIndex++) {
                        int nextRowIndex = prevRowIndex + knightDeltaRow[moveDeltaIndex];
                        int nextColIndex = prevColIndex + knightDeltaCol[moveDeltaIndex];
                        if (nextRowIndex >= 0 && nextRowIndex < boardSize && nextColIndex >= 0 && nextColIndex < boardSize) {
                            int candidateScore = prevScore + pointsMatrix[nextRowIndex][nextColIndex];
                            if (candidateScore > dpMaxScoreAtStepAndCell[stepIndex][nextRowIndex][nextColIndex]) {
                                dpMaxScoreAtStepAndCell[stepIndex][nextRowIndex][nextColIndex] = candidateScore;
                                parentRowAtStepAndCell[stepIndex][nextRowIndex][nextColIndex] = prevRowIndex;
                                parentColAtStepAndCell[stepIndex][nextRowIndex][nextColIndex] = prevColIndex;
                            }
                        }
                    }
                }
        }

        // best end
        int bestAccumulatedScore = NEG_INF, bestEndRow = -1, bestEndCol = -1;
        for (int rowIndex = 0; rowIndex < boardSize; rowIndex++)
            for (int colIndex = 0; colIndex < boardSize; colIndex++) {
                if (dpMaxScoreAtStepAndCell[moveCountK][rowIndex][colIndex] > bestAccumulatedScore) {
                    bestAccumulatedScore = dpMaxScoreAtStepAndCell[moveCountK][rowIndex][colIndex];
                    bestEndRow = rowIndex; bestEndCol = colIndex;
                }
            }

        int[][] orderMatrixWithOptimalPath = new int[boardSize][boardSize];
        if (bestEndRow == -1) return new DPResult(orderMatrixWithOptimalPath, pointsMatrix, moveCountK, bestAccumulatedScore);

        // reconstruct
        int currentStepToWrite = moveCountK;
        int cursorRow = bestEndRow, cursorCol = bestEndCol;
        for (int stepNumber = moveCountK + 1; stepNumber >= 2; stepNumber--) {
            orderMatrixWithOptimalPath[cursorRow][cursorCol] = stepNumber;
            int prevRow = parentRowAtStepAndCell[currentStepToWrite][cursorRow][cursorCol];
            int prevCol = parentColAtStepAndCell[currentStepToWrite][cursorRow][cursorCol];
            cursorRow = prevRow; cursorCol = prevCol; currentStepToWrite--;
        }
        orderMatrixWithOptimalPath[cursorRow][cursorCol] = 1;

        return new DPResult(orderMatrixWithOptimalPath, pointsMatrix, moveCountK, bestAccumulatedScore);
    }
}
