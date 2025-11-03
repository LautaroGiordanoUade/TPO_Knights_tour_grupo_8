package ui;

import core.KnightBacktracking;
import core.KnightWarnsdorff;
import core.KnightDPPath;
import io.TourRepository;
import model.DPResult;
import model.Tour;
import iu.fx.FxBoardWindow;
import iu.fx.FxBootstrap;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Menu {

    private final Scanner consoleInputScanner = new Scanner(System.in);

    public void run() {
        FxBootstrap.ensureJavaFxToolkitStarted();

        while (true) {
            System.out.println("\n=== TP Juego del Caballo (Menu) ===");
            System.out.println("1) Generar tour (Backtracking) y guardar");
            System.out.println("2) Generar tour (Warnsdorff) y guardar");
            System.out.println("3) Mostrar tour guardado (lista → animación JavaFX)");
            System.out.println("4) Generar caso DP (puntajes + k movs) y guardar");
            System.out.println("5) Mostrar caso DP guardado (lista → animación JavaFX)");
            System.out.println("0) Salir");
            System.out.print("Opción: ");
            String selectedOption = consoleInputScanner.nextLine().trim();

            switch (selectedOption) {
                case "1" -> generateAndSaveClassicTour("backtracking");
                case "2" -> generateAndSaveClassicTour("warnsdorff");
                case "3" -> pickAndShowClassicTourFromDisk();
                case "4" -> generateAndSaveDPCase();
                case "5" -> pickAndShowDPCaseFromDisk();
                case "0" -> { System.out.println("Chau!"); return; }
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    // ======= Generar =======

    private void generateAndSaveClassicTour(String methodName) {
        int boardSize = readInt("Tamaño del tablero n (p. ej., 6 u 8): ");
        int startRowIndex = readInt("Fila inicial [0.." + (boardSize-1) + "]: ");
        int startColumnIndex = readInt("Columna inicial [0.." + (boardSize-1) + "]: ");

        Tour computedTour = switch (methodName) {
            case "backtracking" -> KnightBacktracking.solveUsingBacktracking(boardSize, startRowIndex, startColumnIndex);
            case "warnsdorff"   -> KnightWarnsdorff.solveUsingWarnsdorff(boardSize, startRowIndex, startColumnIndex);
            default -> null;
        };

        if (computedTour == null) {
            System.out.println("No se encontró tour con " + methodName + " para esos parámetros.");
            return;
        }

        String outputFilename = TourRepository.filenameForClassicTour(boardSize, startRowIndex, startColumnIndex, methodName);
        TourRepository.saveClassicTour(outputFilename, computedTour);
    }

    private void generateAndSaveDPCase() {
        int boardSize = readInt("Tamaño del tablero n (p. ej. 6): ");
        int startRowIndex = readInt("Fila inicial [0.." + (boardSize-1) + "]: ");
        int startColumnIndex = readInt("Columna inicial [0.." + (boardSize-1) + "]: ");
        int moveCountK = readInt("Cantidad de movimientos k (p. ej. 10): ");

        long randomSeed = readLong("Semilla para puntajes (p. ej. 123): ");
        int[][] pointsMatrix = generateRandomPointsMatrix(boardSize, randomSeed);

        DPResult dpResult = KnightDPPath.solveMaxScorePathUsingDP(pointsMatrix, moveCountK, startRowIndex, startColumnIndex);
        String outputFilename = TourRepository.filenameForDPCase(boardSize, startRowIndex, startColumnIndex, moveCountK);
        TourRepository.saveDPCase(outputFilename, dpResult);
    }

    // ======= Mostrar desde lista del disco =======

    private void pickAndShowClassicTourFromDisk() {
        List<File> files = listFilesByPrefix("tour_");
        if (files.isEmpty()) {
            System.out.println("(No hay tours guardados. Generá con opción 1 o 2.)");
            return;
        }
        File chosen = promptPickFile(files, "Elegí un tour guardado");
        if (chosen == null) return;

        Tour tourToShow = TourRepository.loadClassicTour(chosen.getName());
        if (tourToShow == null) {
            System.out.println("No se pudo cargar el archivo.");
            return;
        }

        boolean useWhiteKnightPiece = readYesNo("¿Caballo blanco? (S/n) [default S]: ", true);
        long animationDelayMillis = readInt("Delay entre pasos (ms, p. ej. 120): ");
        FxBoardWindow.showTourInNewWindow(tourToShow, animationDelayMillis, useWhiteKnightPiece);
    }

    private void pickAndShowDPCaseFromDisk() {
        List<File> files = listFilesByPrefix("dp_");
        if (files.isEmpty()) {
            System.out.println("(No hay casos DP guardados. Generá con opción 4.)");
            return;
        }
        File chosen = promptPickFile(files, "Elegí un caso DP guardado");
        if (chosen == null) return;

        DPResult dpResult = TourRepository.loadDPCase(chosen.getName());
        if (dpResult == null) {
            System.out.println("No se pudo cargar el archivo DP.");
            return;
        }

        boolean useWhiteKnightPiece = readYesNo("¿Caballo blanco? (S/n) [default S]: ", true);
        long animationDelayMillis = readInt("Delay entre pasos (ms, p. ej. 150): ");
        FxBoardWindow.showTourInNewWindow(dpResult.toTour(), animationDelayMillis, useWhiteKnightPiece);
    }

    // ======= Helpers =======

    private List<File> listFilesByPrefix(String prefix) {
        File wd = new File(".");
        File[] all = wd.listFiles((dir, name) -> name.toLowerCase().startsWith(prefix) && name.toLowerCase().endsWith(".csv"));
        if (all == null) return List.of();
        return Arrays.stream(all)
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());
    }

    private File promptPickFile(List<File> files, String title) {
        System.out.println("\n" + title + ":");
        for (int i = 0; i < files.size(); i++) {
            System.out.printf("  %d) %s%n", i + 1, files.get(i).getName());
        }
        int idx = readInt("Número de opción (0 para cancelar): ");
        if (idx <= 0 || idx > files.size()) {
            System.out.println("Cancelado.");
            return null;
        }
        return files.get(idx - 1);
    }

    private int[][] generateRandomPointsMatrix(int boardSize, long randomSeed) {
        java.util.Random rnd = new java.util.Random(randomSeed);
        int[][] p = new int[boardSize][boardSize];
        for (int r = 0; r < boardSize; r++)
            for (int c = 0; c < boardSize; c++)
                p[r][c] = 1 + rnd.nextInt(9);
        return p;
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = consoleInputScanner.nextLine().trim();
            try { return Integer.parseInt(s); }
            catch (NumberFormatException e) { System.out.println("Ingresá un entero válido."); }
        }
    }

    private long readLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = consoleInputScanner.nextLine().trim();
            try { return Long.parseLong(s); }
            catch (NumberFormatException e) { System.out.println("Ingresá un long válido."); }
        }
    }

    private boolean readYesNo(String prompt, boolean defaultYes) {
        System.out.print(prompt);
        String s = consoleInputScanner.nextLine().trim().toLowerCase();
        if (s.isEmpty()) return defaultYes;
        return s.startsWith("s") || s.equals("y") || s.equals("yes");
    }
}
