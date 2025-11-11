package model;

/**
 * Representa el resultado de una ejecución del solver de Programación Dinámica.
 * Contiene:
 * - La matriz de orden de visita (1..t+1, t = movimientos hechos)
 * - La matriz original de puntajes
 * - El valor de k solicitado
 * - El mejor puntaje acumulado
 *
 * Permite conversión a Tour para visualización en JavaFX.
 */
public class DPResult {

    /** Matriz que indica en qué paso se visitó cada casilla (0 = no visitada) */
    public final int[][] visitOrderMatrix;

    /** Matriz original de puntajes del tablero */
    public final int[][] pointsMatrix;

    /** Número máximo de movimientos solicitado (k) */
    public final int requestedMaxMoves;

    /** Mejor puntaje acumulado en la ruta encontrada */
    public final int bestAccumulatedScore;

    /**
     * Constructor.
     *
     * @param visitOrderMatrix      Matriz de orden de visita
     * @param pointsMatrix          Matriz de puntajes
     * @param requestedMaxMoves     k solicitado
     * @param bestAccumulatedScore  Mejor score alcanzado
     */
    public DPResult(int[][] visitOrderMatrix,
                    int[][] pointsMatrix,
                    int requestedMaxMoves,
                    int bestAccumulatedScore) {
        this.visitOrderMatrix = visitOrderMatrix;
        this.pointsMatrix = pointsMatrix;
        this.requestedMaxMoves = requestedMaxMoves;
        this.bestAccumulatedScore = bestAccumulatedScore;
    }

    /**
     * Convierte este resultado en un objeto Tour para usar con JavaFX.
     * Complejidad: O(n²)
     */
    public Tour toTour() {
        return new Tour(visitOrderMatrix);
    }
}