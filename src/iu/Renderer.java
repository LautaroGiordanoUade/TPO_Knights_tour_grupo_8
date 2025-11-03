package iu;

import model.Tour;

public class Renderer {

    // ANSI (si tu consola no soporta colores, igual se ve correctamente sin estilos)
    private static final String RESET = "\u001B[0m";
    private static final String GREY  = "\u001B[90m";
    private static final String WHITE = "\u001B[97m";
    private static final String BLACK = "\u001B[30;1m"; // negro brillante (gris oscuro)
    private static final String CLEAR = "\u001B[H\u001B[2J";

    public enum KnightColor { BLANCO, NEGRO }

    /**
     * Dibuja el tablero completo y anima el movimiento del caballo según el orden del Tour.
     * - Los números del camino completo se muestran en gris claro.
     * - El caballo se pinta en blanco o negro y se mueve paso a paso.
     *
     * Complejidad temporal: O(n^2) para preparar el dibujo base + O(n^2) para animar = O(n^2).
     */
    public static void animateTour(Tour tour, long delayMs, KnightColor color) {
        int n = tour.size();

        // Precomputo posiciones (r,c) por paso para animar rápido.
        int maxStep = n * n;
        int[][] pos = new int[maxStep + 1][2];
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++) {
                int s = tour.orden[r][c];
                if (s >= 1 && s <= maxStep) {
                    pos[s][0] = r; pos[s][1] = c;
                }
            }

        for (int step = 1; step <= maxStep; step++) {
            // Limpiar consola
            System.out.print(CLEAR);
            System.out.flush();

            // Título
            System.out.println("Knight's Tour — paso " + step + "/" + maxStep);

            // Render
            renderFrame(tour, step, color);

            ui.SleepUtils.ms(delayMs);
        }
        System.out.println("\nListo.");
    }

    private static void renderFrame(Tour tour, int currentStep, KnightColor color) {
        int n = tour.size();
        int width = String.valueOf(n*n).length();

        // Pos actual del caballo:
        int kr = -1, kc = -1;
        int target = currentStep;
        outer:
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                if (tour.orden[r][c] == target) { kr = r; kc = c; break outer; }

        String piece = (color == KnightColor.BLANCO) ? WHITE + "♘" + RESET
                : BLACK + "♞" + RESET;

        for (int r = 0; r < n; r++) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < n; c++) {
                int s = tour.orden[r][c];

                if (r == kr && c == kc) {
                    // Caballo en la celda actual
                    sb.append(" ").append(center(piece, width)).append(" ");
                } else {
                    String cell;
                    if (s > 0) {
                        // Camino completo numerado en gris clarito
                        cell = GREY + String.format("%" + width + "d", s) + RESET;
                    } else {
                        cell = " ".repeat(width);
                    }
                    sb.append(" ").append(cell).append(" ");
                }
                if (c < n - 1) sb.append("|");
            }
            System.out.println(sb);
            if (r < n - 1) System.out.println("-".repeat((width + 2) * n + (n - 1)));
        }
    }

    private static String center(String s, int width) {
        if (s.length() >= width) return s;
        int pad = width - s.length();
        int left = pad/2, right = pad - left;
        return " ".repeat(left) + s + " ".repeat(right);
    }
}
