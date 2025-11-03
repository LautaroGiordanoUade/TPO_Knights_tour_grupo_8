package core;

import model.Tour;

import java.util.Arrays;

public class KnightWarnsdorff {

    private static final int[] knightDeltaRow = {-2,-2,-1,-1, 1, 1, 2, 2};
    private static final int[] knightDeltaCol = {-1, 1,-2, 2,-2, 2,-1, 1};

    /**
     * Heurística de Warnsdorff: en cada paso elijo la siguiente casilla con menor “grado”.
     * Devuelve un tour completo si logra cubrir todas las casillas; si se traba, retorna null.
     *
     * Complejidad temporal: O(boardSize^2) — en cada paso miro hasta 8 vecinos y su grado (hasta 8).
     * Complejidad espacial: O(boardSize^2) por la matriz de orden.
     */
    public static Tour solveUsingWarnsdorff(int boardSize,
                                            int startRowIndex,
                                            int startColumnIndex) {
        int[][] knightMoveOrderMatrix = new int[boardSize][boardSize];
        for (int[] matrixRow : knightMoveOrderMatrix) Arrays.fill(matrixRow, 0);

        int currentRowIndex = startRowIndex;
        int currentColumnIndex = startColumnIndex;
        knightMoveOrderMatrix[currentRowIndex][currentColumnIndex] = 1;

        for (int stepNumber = 2; stepNumber <= boardSize * boardSize; stepNumber++) {
            int chosenNextRowIndex = -1, chosenNextColIndex = -1;
            int bestNextDegreeCount = Integer.MAX_VALUE;

            for (int movementIndex = 0; movementIndex < 8; movementIndex++) {
                int candidateRowIndex = currentRowIndex + knightDeltaRow[movementIndex];
                int candidateColIndex = currentColumnIndex + knightDeltaCol[movementIndex];

                if (isInsideBoard(boardSize, candidateRowIndex, candidateColIndex)
                        && knightMoveOrderMatrix[candidateRowIndex][candidateColIndex] == 0) {
                    int candidateDegree = countDegree(boardSize, candidateRowIndex, candidateColIndex, knightMoveOrderMatrix);
                    if (candidateDegree < bestNextDegreeCount) {
                        bestNextDegreeCount = candidateDegree;
                        chosenNextRowIndex = candidateRowIndex;
                        chosenNextColIndex = candidateColIndex;
                    }
                }
            }

            if (chosenNextRowIndex == -1) return null; // me quedé sin jugadas

            currentRowIndex = chosenNextRowIndex;
            currentColumnIndex = chosenNextColIndex;
            knightMoveOrderMatrix[currentRowIndex][currentColumnIndex] = stepNumber;
        }
        return new Tour(knightMoveOrderMatrix);
    }

    private static int countDegree(int boardSize, int rowIndex, int colIndex, int[][] knightMoveOrderMatrix) {
        int availableMoveCount = 0;
        for (int movementIndex = 0; movementIndex < 8; movementIndex++) {
            int neighborRowIndex = rowIndex + knightDeltaRow[movementIndex];
            int neighborColIndex = colIndex + knightDeltaCol[movementIndex];
            if (isInsideBoard(boardSize, neighborRowIndex, neighborColIndex)
                    && knightMoveOrderMatrix[neighborRowIndex][neighborColIndex] == 0) {
                availableMoveCount++;
            }
        }
        return availableMoveCount;
    }

    private static boolean isInsideBoard(int boardSize, int rowIndex, int colIndex) {
        return rowIndex >= 0 && rowIndex < boardSize && colIndex >= 0 && colIndex < boardSize;
    }
}
