package io;

import model.DPResult;
import model.Tour;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Repositorio de tours (IO de archivos).
 *
 * Complejidad temporal (resumen):
 * - filenameForClassicTour / filenameForDPCase: O(1)  (formateo de strings)
 * - fileExists: O(1) promedio (consulta al FS; ignoramos latencia de disco)
 * - saveClassicTour: O(n^2)  (escribe toda la matriz n x n)
 * - loadClassicTour: O(n^2)  (lee toda la matriz n x n)
 * - saveDPCase: O(n^2)       (escribe puntos y orden óptimo)
 * - loadDPCase: O(n^2)       (lee puntos y orden óptimo)
 */
public class TourRepository {

    // Formato de timestamp que se agrega al final del nombre del archivo
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // ---------- Tours clásicos ----------
    public static String filenameForClassicTour(int boardSize, int startRowIndex, int startColumnIndex, String methodName) {
        String ts = LocalDateTime.now().format(FILE_TS);
        return String.format("tour_n%d_r%d_c%d_%s_%s.csv",
                boardSize, startRowIndex, startColumnIndex, methodName.toLowerCase(), ts);
    }

    public static boolean fileExists(String filename) {
        return new File(filename).exists();
    }

    public static void saveClassicTour(String filename, Tour tourToSave) {
        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(filename), StandardCharsets.UTF_8))) {

            int boardSize = tourToSave.getBoardSize();
            int[][] order = tourToSave.getKnightMoveOrderMatrix();

            printWriter.println(boardSize);
            for (int rowIndex = 0; rowIndex < boardSize; rowIndex++) {
                StringBuilder rowBuilder = new StringBuilder();
                for (int colIndex = 0; colIndex < boardSize; colIndex++) {
                    if (colIndex > 0) rowBuilder.append(",");
                    rowBuilder.append(order[rowIndex][colIndex]);
                }
                printWriter.println(rowBuilder);
            }
            System.out.println("Archivo guardado: " + filename);
        } catch (IOException exception) {
            System.out.println("Error guardando " + filename + ": " + exception.getMessage());
        }
    }

    public static Tour loadClassicTour(String filename) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filename), StandardCharsets.UTF_8))) {
            int boardSize = Integer.parseInt(bufferedReader.readLine().trim());
            int[][] orderMatrix = new int[boardSize][boardSize];
            for (int rowIndex = 0; rowIndex < boardSize; rowIndex++) {
                String[] parts = bufferedReader.readLine().split(",");
                for (int colIndex = 0; colIndex < boardSize; colIndex++) {
                    orderMatrix[rowIndex][colIndex] = Integer.parseInt(parts[colIndex].trim());
                }
            }
            return new Tour(orderMatrix);
        } catch (Exception exception) {
            System.out.println("Error leyendo " + filename + ": " + exception.getMessage());
            return null;
        }
    }

    // ---------- Casos DP (nueva implementación, compatible con DPResult actual) ----------

    /**
     * Genera un nombre de archivo para un caso DP.
     * Formato: dp_n{N}_k{K}_sr{R}_sc{C}_seed{S}.csv
     */
    public static String filenameForDPCase(int boardSize, int startRow, int startCol, int maxMoves, long seed) {
        return String.format("dp_n%d_k%d_sr%d_sc%d_seed%d.csv", boardSize, maxMoves, startRow, startCol, seed);
    }

    /**
     * Guarda un resultado DP en CSV.
     * Formato:
     *   n, k, seed
     *   puntos (n filas)
     *   orden (n filas)
     *
     * Complejidad temporal: O(n²)
     * Complejidad espacial: O(n²)
     */
    public static void saveDPCase(String filename, DPResult dpResult, long seedUsed) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            int n = dpResult.pointsMatrix.length;
            writer.printf("%d,%d,%d%n", n, dpResult.requestedMaxMoves, seedUsed);

            // Guardar matriz de puntajes
            for (int r = 0; r < n; r++) {
                writer.println(Arrays.stream(dpResult.pointsMatrix[r])
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining(",")));
            }

            // Guardar matriz de orden
            for (int r = 0; r < n; r++) {
                writer.println(Arrays.stream(dpResult.visitOrderMatrix[r])
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining(",")));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar DP: " + filename, e);
        }
    }

    /**
     * Carga un resultado DP desde CSV.
     *
     * Complejidad temporal: O(n²)
     * Complejidad espacial: O(n²)
     *
     * @param filename Nombre del archivo CSV
     * @return DPResult cargado, o null si hay error
     */
    public static DPResult loadDPCase(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String[] header = reader.readLine().split(",");
            int n = Integer.parseInt(header[0]);
            int k = Integer.parseInt(header[1]);
            long seed = Long.parseLong(header[2]); // opcional, no usado en carga

            int[][] points = new int[n][n];
            for (int r = 0; r < n; r++) {
                points[r] = Arrays.stream(reader.readLine().split(","))
                        .mapToInt(Integer::parseInt)
                        .toArray();
            }

            int[][] order = new int[n][n];
            for (int r = 0; r < n; r++) {
                order[r] = Arrays.stream(reader.readLine().split(","))
                        .mapToInt(Integer::parseInt)
                        .toArray();
            }

            // Calcular score acumulado (por seguridad, aunque se guarda en DPResult)
            int score = 0;
            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) {
                    if (order[r][c] > 0) score += points[r][c];
                }
            }

            return new DPResult(order, points, k, score);
        } catch (IOException e) {
            System.err.println("Error al cargar DP: " + filename);
            return null;
        }
    }

}