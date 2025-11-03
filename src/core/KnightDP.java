package core;

/**
 * Variante DP (opcional para mostrar valor máximo de puntaje en k movimientos).
 * En el menú lo dejamos fuera de la animación clásica, pero te lo dejo por si lo querés usar.
 */
public class KnightDP {
    private static final int[] DR = {-2,-2,-1,-1, 1, 1, 2, 2};
    private static final int[] DC = {-1, 1,-2, 2,-2, 2,-1, 1};

    /**
     * Devuelve el máximo puntaje alcanzable en exactamente k movimientos.
     * No reconstruye camino (enfocado al valor).
     *
     * Complejidad temporal: O(k * n^2 * 8).
     */
    public static int maxScoreInK(int[][] puntos, int k, int sr, int sc) {
        int n = puntos.length;
        int[][][] dp = new int[k + 1][n][n];
        final int NEG = Integer.MIN_VALUE / 4;

        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                dp[0][r][c] = NEG;
        dp[0][sr][sc] = puntos[sr][sc];

        for (int step = 1; step <= k; step++) {
            for (int r = 0; r < n; r++)
                for (int c = 0; c < n; c++)
                    dp[step][r][c] = NEG;

            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) {
                    int prev = dp[step-1][r][c];
                    if (prev == NEG) continue;
                    for (int mv = 0; mv < 8; mv++) {
                        int nr = r + DR[mv], nc = c + DC[mv];
                        if (nr>=0 && nr<n && nc>=0 && nc<n) {
                            dp[step][nr][nc] = Math.max(dp[step][nr][nc], prev + puntos[nr][nc]);
                        }
                    }
                }
            }
        }

        int ans = NEG;
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                ans = Math.max(ans, dp[k][r][c]);
        return ans;
    }
}
