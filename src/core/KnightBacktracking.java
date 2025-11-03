package core;

import model.Tour;

import java.util.Arrays;

public class KnightBacktracking {

    // Desplazamientos del caballo (8 posibilidades)
    private static final int[] knightDeltaRow  = {-2,-2,-1,-1, 1, 1, 2, 2};
    private static final int[] knightDeltaCol  = {-1, 1,-2, 2,-2, 2,-1, 1};

    /**
     * Knight's Tour por backtracking desde (startRowIndex,startColumnIndex) en un tablero boardSize x boardSize.
     * Devuelve una matriz con el orden de visita o null si no hay solución.
     *
     * Complejidad temporal (peor caso): O(8^(boardSize^2)) — árbol exponencial.
     * Complejidad espacial: O(boardSize^2) por la matriz + O(boardSize^2) de stack recursivo.
     */
    public static Tour solveUsingBacktracking(int boardSize,
                                              int startRowIndex,
                                              int startColumnIndex) {
        int[][] knightMoveOrderMatrix = new int[boardSize][boardSize];
        for (int[] matrixRow : knightMoveOrderMatrix) Arrays.fill(matrixRow, 0);

        knightMoveOrderMatrix[startRowIndex][startColumnIndex] = 1;
        boolean tourWasFound = backtrackPlaceNextStep(boardSize,
                startRowIndex,
                startColumnIndex,
                2,
                knightMoveOrderMatrix);
        return tourWasFound ? new Tour(knightMoveOrderMatrix) : null;
    }

    private static boolean backtrackPlaceNextStep(int boardSize,
                                                  int currentRowIndex,
                                                  int currentColumnIndex,
                                                  int nextStepNumberToPlace,
                                                  int[][] knightMoveOrderMatrix) {
        if (nextStepNumberToPlace > boardSize * boardSize) return true;

        for (int movementIndex = 0; movementIndex < 8; movementIndex++) {
            int candidateRowIndex = currentRowIndex + knightDeltaRow[movementIndex];
            int candidateColIndex = currentColumnIndex + knightDeltaCol[movementIndex];

            if (isInsideBoard(boardSize, candidateRowIndex, candidateColIndex)
                    && knightMoveOrderMatrix[candidateRowIndex][candidateColIndex] == 0) {
                knightMoveOrderMatrix[candidateRowIndex][candidateColIndex] = nextStepNumberToPlace;

                if (backtrackPlaceNextStep(boardSize,
                        candidateRowIndex,
                        candidateColIndex,
                        nextStepNumberToPlace + 1,
                        knightMoveOrderMatrix)) return true;

                knightMoveOrderMatrix[candidateRowIndex][candidateColIndex] = 0; // backtrack
            }
        }
        return false;
    }

    private static boolean isInsideBoard(int boardSize, int rowIndex, int colIndex) {
        return rowIndex >= 0 && rowIndex < boardSize && colIndex >= 0 && colIndex < boardSize;
    }
}
