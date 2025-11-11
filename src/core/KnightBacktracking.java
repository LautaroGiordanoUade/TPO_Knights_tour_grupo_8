package core;

import model.Tour;

import java.util.Arrays;

/**
 * Knight’s Tour – Parte 1 (Backtracking).
 * Intenta cubrir un tablero n x n con un solo recorrido del caballo, colocando los pasos 1..n^2.
 * Devuelve un Tour con la matriz de orden o null si no hay solución.
 *
 * Notas de diseño:
 * - Mantiene el backtracking “puro” (explora caminos), pero con una optimización local:
 *   en cada paso ordena los 8 movimientos por “grado” (menos salidas primero).
 *   Esto reduce dramáticamente el branching en la práctica, sin dejar de ser backtracking.
 */
public class KnightBacktracking {

    // Offsets (8 movimientos posibles del caballo)
    private static final int[] KNIGHT_DELTA_ROW = {-2,-2,-1,-1, 1, 1, 2, 2};
    private static final int[] KNIGHT_DELTA_COL = {-1, 1,-2, 2,-2, 2,-1, 1};

    /**
     * Resuelve Knight's Tour por backtracking desde (startRowIndex, startColumnIndex).
     *
     * Validaciones:
     * - boardSize >= 1
     * - 0 <= startRowIndex,startColumnIndex < boardSize
     *
     * Complejidad temporal (peor caso): O(8^(n^2)).
     *   - El árbol de búsqueda puede explotar; el ordenamiento por grado sólo mejora el caso práctico.
     * Complejidad espacial: O(n^2) por la matriz + O(n^2) de stack recursivo.
     */
    public static Tour solveUsingBacktracking(int boardSize,
                                              int startRowIndex,
                                              int startColumnIndex) {
        // ---- chequeos de entrada (para “elegir n*n de entrada” y no reventar) ----
        if (boardSize <= 0) {
            System.out.println("El tamaño del tablero debe ser >= 1.");
            return null;
        }
        if (!isInsideBoard(boardSize, startRowIndex, startColumnIndex)) {
            System.out.println("La posición inicial está fuera del tablero.");
            return null;
        }

        // Matriz de orden de visita: 0 = no visitado; 1..n^2 = paso
        int[][] knightMoveOrderMatrix = new int[boardSize][boardSize];
        for (int[] matrixRow : knightMoveOrderMatrix) Arrays.fill(matrixRow, 0);

        // Colocar el primer paso en la celda inicial
        knightMoveOrderMatrix[startRowIndex][startColumnIndex] = 1;

        // Backtracking
        boolean tourWasFound = backtrackPlaceNextStep(
                boardSize,
                startRowIndex,
                startColumnIndex,
                /* next step */ 2,
                knightMoveOrderMatrix
        );

        // Si hay solución, devolvemos el Tour (el menú lo guardará a archivo)
        return tourWasFound ? new Tour(knightMoveOrderMatrix) : null;
    }

    /**
     * Coloca recursivamente el siguiente paso.
     *
     * Complejidad temporal (peor caso): O(8^(n^2)).
     * Complejidad espacial: O(n^2) de stack (profundidad = n^2).
     */
    private static boolean backtrackPlaceNextStep(int boardSize,
                                                  int currentRowIndex,
                                                  int currentColumnIndex,
                                                  int nextStepNumberToPlace,
                                                  int[][] knightMoveOrderMatrix) {
        // Caso base: ya coloqué todos los pasos 1..n^2
        if (nextStepNumberToPlace > boardSize * boardSize) return true;

        // Ordenar los próximos movimientos por “grado” (Warnsdorff-lite)
        int[] orderedMoveIndices = computeMoveOrderByDegree(boardSize, currentRowIndex, currentColumnIndex, knightMoveOrderMatrix);

        for (int moveIdx = 0; moveIdx < 8; moveIdx++) {
            int movementIndex = orderedMoveIndices[moveIdx];
            if (movementIndex == -1) break; // no quedan candidatos

            int candidateRowIndex = currentRowIndex + KNIGHT_DELTA_ROW[movementIndex];
            int candidateColIndex = currentColumnIndex + KNIGHT_DELTA_COL[movementIndex];

            if (isInsideBoard(boardSize, candidateRowIndex, candidateColIndex)
                    && knightMoveOrderMatrix[candidateRowIndex][candidateColIndex] == 0) {

                knightMoveOrderMatrix[candidateRowIndex][candidateColIndex] = nextStepNumberToPlace;

                if (backtrackPlaceNextStep(boardSize,
                        candidateRowIndex,
                        candidateColIndex,
                        nextStepNumberToPlace + 1,
                        knightMoveOrderMatrix)) {
                    return true; // camino exitoso
                }

                // backtrack: deshacer y probar el siguiente movimiento
                knightMoveOrderMatrix[candidateRowIndex][candidateColIndex] = 0;
            }
        }
        return false; // no hubo forma de colocar este paso
    }

    /**
     * Calcula un orden de prueba de los 8 movimientos desde (r,c), priorizando
     * primero los que dejan menos salidas (grado más chico). Si no hay candidato, devuelve -1.
     *
     * Complejidad temporal: O(8 * 8) ~ O(1) por paso (constante).
     * Espacial: O(1).
     */
    private static int[] computeMoveOrderByDegree(int boardSize,
                                                  int r,
                                                  int c,
                                                  int[][] order) {
        int[] moveIndex = new int[8];
        int[] degree    = new int[8];
        int candidateCount = 0;

        // Armo la lista de candidatos y su “grado” (cantidad de casillas libres a las que podrían ir luego)
        for (int k = 0; k < 8; k++) {
            int nr = r + KNIGHT_DELTA_ROW[k];
            int nc = c + KNIGHT_DELTA_COL[k];
            if (isInsideBoard(boardSize, nr, nc) && order[nr][nc] == 0) {
                moveIndex[candidateCount] = k;
                degree[candidateCount] = countDegree(boardSize, nr, nc, order);
                candidateCount++;
            }
        }

        // Completo el resto con -1 para cortar el loop rápido
        for (int i = candidateCount; i < 8; i++) moveIndex[i] = -1;

        // Ordeno por grado ascendente (selection sort chiquito: 8 elementos máx)
        for (int i = 0; i < candidateCount; i++) {
            int best = i;
            for (int j = i + 1; j < candidateCount; j++) {
                if (degree[j] < degree[best]) best = j;
            }
            if (best != i) {
                int tmpIdx = moveIndex[i]; moveIndex[i] = moveIndex[best]; moveIndex[best] = tmpIdx;
                int tmpDeg = degree[i];    degree[i]    = degree[best];    degree[best]    = tmpDeg;
            }
        }
        return moveIndex;
    }

    /** Cuenta cuántas casillas libres quedarían disponibles desde (r,c). */
    private static int countDegree(int boardSize, int r, int c, int[][] order) {
        int free = 0;
        for (int k = 0; k < 8; k++) {
            int nr = r + KNIGHT_DELTA_ROW[k];
            int nc = c + KNIGHT_DELTA_COL[k];
            if (isInsideBoard(boardSize, nr, nc) && order[nr][nc] == 0) free++;
        }
        return free;
    }

    /** Chequea si (rowIndex, colIndex) está dentro del tablero n x n. */
    private static boolean isInsideBoard(int boardSize, int rowIndex, int colIndex) {
        return rowIndex >= 0 && rowIndex < boardSize && colIndex >= 0 && colIndex < boardSize;
    }

    // ---------------------- Helpers opcionales de salida por consola ----------------------

    /**
     * Imprime la matriz de orden (formato simple) si querés “ver” la solución en consola.
     * O(N^2) temporal, O(1) extra.
     */
    public static void printSolutionIfAny(Tour tour) {
        if (tour == null) {
            System.out.println("No se encontró solución.");
            return;
        }
        int n = tour.getBoardSize();
        int[][] m = tour.getKnightMoveOrderMatrix();
        int width = String.valueOf(n*n).length();
        for (int r = 0; r < n; r++) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < n; c++) {
                if (c > 0) sb.append(" ");
                sb.append(String.format("%" + width + "d", m[r][c]));
            }
            System.out.println(sb);
        }
    }
}
