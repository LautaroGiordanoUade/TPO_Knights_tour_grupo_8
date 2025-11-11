package model;

/**
 * Representa un recorrido del caballo.
 * Mantiene compatibilidad con código viejo:
 *  - campo público "orden" (alias de la matriz interna)
 *  - método size() además de getBoardSize()
 *
 * Nota de seguridad: exponer "orden" permite escritura externa.
 * Para el TP está ok; si quisieras inmutabilidad estricta, usá sólo getters.
 */
public class Tour {

    /** Matriz interna con el orden de visita: order[row][col] = paso (1..N) o 0 si no visitado */
    private final int[][] knightMoveOrderMatrix;

    // ========= Alias de compatibilidad (para código existente) =========
    /** Alias público para compatibilidad con código que usa tour.orden[r][c] */
    public final int[][] orden;

    public Tour(int[][] knightMoveOrderMatrix) {
        this.knightMoveOrderMatrix = knightMoveOrderMatrix;
        // mismo arreglo, alias para compatibilidad:
        this.orden = knightMoveOrderMatrix;
    }

    /** Tamaño del tablero (N x N) */
    public int getBoardSize() {
        return knightMoveOrderMatrix.length;
    }

    /** Alias de compatibilidad con código que llama tour.size() */
    public int size() {
        return getBoardSize();
    }

    /** Devuelve la matriz de orden de visita (getter "formal") */
    public int[][] getKnightMoveOrderMatrix() {
        return knightMoveOrderMatrix;
    }

    /** Alias adicional por si querés un nombre más corto */
    public int[][] getOrderMatrix() {
        return knightMoveOrderMatrix;
    }
}
