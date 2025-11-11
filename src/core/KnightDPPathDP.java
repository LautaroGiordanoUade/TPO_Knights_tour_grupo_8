package core;

import model.DPResult;

import java.util.*;

/**
 * KnightDPPathDP — Backtracking con memoización usando BitSet para rutas SIN REPETICIÓN.
 *
 * Resuelve la variación del Knight's Tour:
 * - Dado un tablero n×n con puntajes positivos (1..100),
 * - Encuentra la ruta del caballo que visita casillas SIN REPETIR,
 * - Con hasta k movimientos (k+1 casillas),
 * - Que maximice la suma de puntajes.
 *
 * Prioridad:
 * 1. Mayor cantidad de movimientos (t ≤ k).
 * 2. Entre rutas de igual longitud, mayor puntaje.
 *
 * Características:
 * - Usa BitSet para representar el conjunto de casillas visitadas (soporta n > 8).
 * - Memoización con clave basada en (BitSet, posición).
 * - Reconstrucción de la ruta óptima.
 *
 * Complejidad temporal: O(8 * C * S)
 *   - C = número de combinaciones alcanzables ≈ O( (n²)^(k+1) ) en el peor caso (sin poda)
 *   - S = costo de clonar y comparar BitSet ≈ O(n² / 64)
 *   - En la práctica, la poda reduce drásticamente el número de estados.
 *
 * Complejidad espacial: O(C * n²) para almacenar los estados y padres.
 *
 * Nota: Viable para k ≤ 15-20 y n ≤ 10 en la mayoría de las máquinas.
 */
public class KnightDPPathDP {

    private static final int KNIGHT_MOVES = 8;
    private static final int[] DELTA_ROW = {-2, -2, -1, -1, 1, 1, 2, 2};
    private static final int[] DELTA_COL = {-1, 1, -2, 2, -2, 2, -1, 1};

    /**
     * Clase auxiliar para almacenar el estado de un nodo en la búsqueda.
     * Guarda el mejor puntaje y longitud alcanzables desde este estado.
     */
    private static class SearchState {
        long bestScore;
        int bestLength;
        SearchState(long score, int length) {
            this.bestScore = score;
            this.bestLength = length;
        }
    }

    /**
     * Contenedor mutable para la mejor solución global encontrada durante la búsqueda.
     */
    private static class BestSolution {
        long score = Long.MIN_VALUE;
        int length = -1;
        BitSet visitedMask = null;
        int endRow = -1;
        int endCol = -1;
    }

    /**
     * Resuelve el problema de maximización de puntaje con la restricción de no repetir casillas.
     *
     * @param pointsMatrix Matriz n×n con puntajes (asumidos positivos, ej. 1..100)
     * @param maxMoves     Número máximo de movimientos k solicitado
     * @param startRow     Fila inicial (0-based)
     * @param startCol     Columna inicial (0-based)
     * @return             DPResult con la mejor ruta encontrada
     */
    public static DPResult solveMaxScorePathUsingDP(int[][] pointsMatrix, int maxMoves, int startRow, int startCol) {
        // --- Validaciones iniciales ---
        if (pointsMatrix == null || pointsMatrix.length == 0 || pointsMatrix[0] == null) {
            return createEmptyResult(0, maxMoves, pointsMatrix);
        }

        int boardSize = pointsMatrix.length;
        for (int row = 0; row < boardSize; row++) {
            if (pointsMatrix[row] == null || pointsMatrix[row].length != boardSize) {
                return createEmptyResult(boardSize, maxMoves, pointsMatrix);
            }
        }

        if (maxMoves < 0 || startRow < 0 || startRow >= boardSize || startCol < 0 || startCol >= boardSize) {
            return createEmptyResult(boardSize, maxMoves, pointsMatrix);
        }

        // Limitar maxMoves al máximo posible (no se pueden visitar más casillas que las que hay)
        int maxPossibleMoves = boardSize * boardSize - 1;
        if (maxMoves > maxPossibleMoves) {
            maxMoves = maxPossibleMoves;
        }

        // --- Estructuras para búsqueda y reconstrucción ---
        Map<String, SearchState> memo = new HashMap<>();
        Map<String, BitSet> parentMask = new HashMap<>();
        Map<String, Integer> parentRow = new HashMap<>();
        Map<String, Integer> parentCol = new HashMap<>();
        BestSolution bestSolution = new BestSolution();

        // Inicializar el estado inicial
        BitSet initialVisited = new BitSet(boardSize * boardSize);
        int startIndex = startRow * boardSize + startCol;
        initialVisited.set(startIndex);

        bestSolution.score = pointsMatrix[startRow][startCol];
        bestSolution.length = 0;
        bestSolution.visitedMask = (BitSet) initialVisited.clone();
        bestSolution.endRow = startRow;
        bestSolution.endCol = startCol;

        // Ejecutar la búsqueda DFS con memoización
        depthFirstSearch(
                initialVisited, startRow, startCol, 0, maxMoves,
                pointsMatrix, boardSize, memo,
                parentMask, parentRow, parentCol, bestSolution
        );

        // --- Reconstruir la ruta óptima ---
        int[][] visitOrderMatrix = new int[boardSize][boardSize];
        BitSet currentMask = bestSolution.visitedMask;
        int currentRow = bestSolution.endRow;
        int currentCol = bestSolution.endCol;
        int totalVisited = bestSolution.length + 1; // número de casillas en la ruta

        // Reconstrucción hacia atrás usando los mapas de padres
        for (int step = totalVisited; step >= 1; step--) {
            visitOrderMatrix[currentRow][currentCol] = step;
            if (step > 1) {
                String key = generateStateKey(currentMask, currentRow, currentCol);
                currentMask = parentMask.get(key);
                currentRow = parentRow.get(key);
                currentCol = parentCol.get(key);
            }
        }

        return new DPResult(visitOrderMatrix, pointsMatrix, maxMoves, (int) bestSolution.score);
    }

    /**
     * Genera una clave única (String) para un estado (BitSet, fila, columna).
     * Se usa para memoización y reconstrucción.
     *
     * Complejidad: O(n² / 64) debido a la conversión de BitSet a String.
     *
     * @param visited Conjunto de casillas visitadas
     * @param row     Fila actual
     * @param col     Columna actual
     * @return        Clave única como String
     */
    private static String generateStateKey(BitSet visited, int row, int col) {
        return visited.toString() + "@" + row + "," + col;
    }

    /**
     * Búsqueda en profundidad (DFS) con memoización y poda.
     *
     * Explora recursivamente todos los caminos posibles sin repetir casillas,
     * actualizando la mejor solución global y almacenando información para reconstrucción.
     *
     * Complejidad por llamada: O(1) + costo de generar clave (O(n²/64))
     *
     * @param currentVisited  BitSet con casillas visitadas hasta ahora
     * @param currentRow      Fila actual del caballo
     * @param currentCol      Columna actual del caballo
     * @param movesDone       Número de movimientos realizados
     * @param maxMoves        Límite máximo de movimientos
     * @param points          Matriz de puntajes
     * @param boardSize       Tamaño del tablero
     * @param memo            Mapa de memoización (clave -> mejor estado)
     * @param parentMask      Mapa para reconstrucción: clave -> máscara padre
     * @param parentRow       Mapa para reconstrucción: clave -> fila padre
     * @param parentCol       Mapa para reconstrucción: clave -> columna padre
     * @param best            Contenedor de la mejor solución global
     */
    private static void depthFirstSearch(
            BitSet currentVisited, int currentRow, int currentCol, int movesDone, int maxMoves,
            int[][] points, int boardSize,
            Map<String, SearchState> memo,
            Map<String, BitSet> parentMask,
            Map<String, Integer> parentRow,
            Map<String, Integer> parentCol,
            BestSolution best) {

        // Calcular el puntaje actual sumando todas las casillas visitadas
        long currentScore = 0;
        for (int index = currentVisited.nextSetBit(0); index >= 0; index = currentVisited.nextSetBit(index + 1)) {
            int r = index / boardSize;
            int c = index % boardSize;
            currentScore += points[r][c];
        }

        // Actualizar la mejor solución global (prioridad: longitud, luego puntaje)
        if (movesDone > best.length ||
                (movesDone == best.length && currentScore > best.score)) {
            best.score = currentScore;
            best.length = movesDone;
            best.visitedMask = (BitSet) currentVisited.clone();
            best.endRow = currentRow;
            best.endCol = currentCol;
        }

        // Si ya alcanzamos el límite de movimientos, detener la exploración
        if (movesDone >= maxMoves) {
            return;
        }

        // Explorar los 8 movimientos posibles del caballo
        for (int move = 0; move < KNIGHT_MOVES; move++) {
            int newRow = currentRow + DELTA_ROW[move];
            int newCol = currentCol + DELTA_COL[move];

            // Verificar que el movimiento esté dentro del tablero
            if (newRow < 0 || newRow >= boardSize || newCol < 0 || newCol >= boardSize) {
                continue;
            }

            int newIndex = newRow * boardSize + newCol;
            // Verificar que la casilla no haya sido visitada
            if (currentVisited.get(newIndex)) {
                continue;
            }

            // Crear nuevo estado con la casilla añadida
            BitSet newVisited = (BitSet) currentVisited.clone();
            newVisited.set(newIndex);

            String newKey = generateStateKey(newVisited, newRow, newCol);

            // Poda: si ya vimos este estado con un resultado mejor o igual, saltar
            if (memo.containsKey(newKey)) {
                SearchState existing = memo.get(newKey);
                long newScore = currentScore + points[newRow][newCol];
                if (existing.bestLength > movesDone + 1 ||
                        (existing.bestLength == movesDone + 1 && existing.bestScore >= newScore)) {
                    continue;
                }
            }

            // Actualizar memoización y mapas de padres para reconstrucción
            memo.put(newKey, new SearchState(currentScore + points[newRow][newCol], movesDone + 1));
            parentMask.put(newKey, (BitSet) currentVisited.clone());
            parentRow.put(newKey, currentRow);
            parentCol.put(newKey, currentCol);

            // Recursión
            depthFirstSearch(
                    newVisited, newRow, newCol, movesDone + 1, maxMoves,
                    points, boardSize, memo,
                    parentMask, parentRow, parentCol, best
            );
        }
    }

    /**
     * Crea un resultado vacío para casos inválidos.
     *
     * Complejidad: O(n²) para inicializar la matriz vacía.
     */
    private static DPResult createEmptyResult(int boardSize, int maxMoves, int[][] pointsMatrix) {
        int[][] emptyOrder = (boardSize > 0) ? new int[boardSize][boardSize] : new int[0][0];
        return new DPResult(emptyOrder, pointsMatrix, maxMoves, Integer.MIN_VALUE);
    }
}