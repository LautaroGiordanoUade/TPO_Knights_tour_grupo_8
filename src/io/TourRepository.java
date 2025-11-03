package io;

import model.DPResult;
import model.Tour;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class TourRepository {

    // ---------- Tours clásicos ----------
    public static String filenameForClassicTour(int boardSize, int startRowIndex, int startColumnIndex, String methodName) {
        return String.format("tour_n%d_r%d_c%d_%s.csv", boardSize, startRowIndex, startColumnIndex, methodName.toLowerCase());
    }

    public static boolean fileExists(String filename) {
        return new File(filename).exists();
    }

    public static void saveClassicTour(String filename, Tour tourToSave) {
        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(filename), StandardCharsets.UTF_8))) {

            int boardSize = tourToSave.getBoardSize();           // <-- antes: boardSize()
            int[][] order = tourToSave.getKnightMoveOrderMatrix(); // <-- antes: campo privado

            printWriter.println(boardSize);
            for (int rowIndex = 0; rowIndex < boardSize; rowIndex++) {
                StringBuilder rowBuilder = new StringBuilder();
                for (int colIndex = 0; colIndex < boardSize; colIndex++) {
                    if (colIndex > 0) rowBuilder.append(",");
                    rowBuilder.append(order[rowIndex][colIndex]); // <-- antes accedías al campo privado
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

    // ---------- Casos DP ----------
    public static String filenameForDPCase(int boardSize, int startRowIndex, int startColumnIndex, int moveCountK) {
        return String.format("dp_n%d_r%d_c%d_k%d.csv", boardSize, startRowIndex, startColumnIndex, moveCountK);
    }

    public static void saveDPCase(String filename, DPResult dpResult) {
        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(filename), StandardCharsets.UTF_8))) {

            printWriter.println(dpResult.boardSize + "," + dpResult.moveCountK);

            for (int rowIndex = 0; rowIndex < dpResult.boardSize; rowIndex++) {
                StringBuilder sb = new StringBuilder();
                for (int colIndex = 0; colIndex < dpResult.boardSize; colIndex++) {
                    if (colIndex > 0) sb.append(",");
                    sb.append(dpResult.pointsMatrix[rowIndex][colIndex]);
                }
                printWriter.println(sb);
            }

            printWriter.println();

            for (int rowIndex = 0; rowIndex < dpResult.boardSize; rowIndex++) {
                StringBuilder sb = new StringBuilder();
                for (int colIndex = 0; colIndex < dpResult.boardSize; colIndex++) {
                    if (colIndex > 0) sb.append(",");
                    sb.append(dpResult.orderMatrixWithOptimalPath[rowIndex][colIndex]);
                }
                printWriter.println(sb);
            }

            System.out.println("Archivo DP guardado: " + filename + " (score=" + dpResult.bestAccumulatedScore + ")");
        } catch (IOException exception) {
            System.out.println("Error guardando DP " + filename + ": " + exception.getMessage());
        }
    }

    public static DPResult loadDPCase(String filename) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filename), StandardCharsets.UTF_8))) {
            String[] headerParts = bufferedReader.readLine().trim().split(",");
            int boardSize = Integer.parseInt(headerParts[0]);
            int moveCountK = Integer.parseInt(headerParts[1]);

            int[][] pointsMatrix = new int[boardSize][boardSize];
            for (int rowIndex = 0; rowIndex < boardSize; rowIndex++) {
                String[] parts = bufferedReader.readLine().split(",");
                for (int colIndex = 0; colIndex < boardSize; colIndex++) {
                    pointsMatrix[rowIndex][colIndex] = Integer.parseInt(parts[colIndex].trim());
                }
            }
            bufferedReader.readLine(); // blank

            int[][] orderMatrixWithOptimalPath = new int[boardSize][boardSize];
            for (int rowIndex = 0; rowIndex < boardSize; rowIndex++) {
                String[] parts = bufferedReader.readLine().split(",");
                for (int colIndex = 0; colIndex < boardSize; colIndex++) {
                    orderMatrixWithOptimalPath[rowIndex][colIndex] = Integer.parseInt(parts[colIndex].trim());
                }
            }
            return new DPResult(orderMatrixWithOptimalPath, pointsMatrix, moveCountK, 0);
        } catch (Exception exception) {
            System.out.println("Error leyendo DP " + filename + ": " + exception.getMessage());
            return null;
        }
    }
}
