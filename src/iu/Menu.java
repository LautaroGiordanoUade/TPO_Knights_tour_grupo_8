package ui;

import core.KnightBacktracking;
import core.KnightWarnsdorff;
import core.KnightDPPathDP;

import io.TourRepository;
import model.DPResult;
import model.Tour;

import iu.fx.FxBoardWindow;
import iu.fx.FxBootstrap;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Menú gráfico principal de la aplicación del Caballo de Ajedrez.
 *
 * Reemplaza la interfaz de consola por una ventana JavaFX moderna con:
 * - Botones intuitivos para cada funcionalidad.
 * - Diálogos modales para entrada de parámetros.
 * - Indicador de progreso (spinner) durante operaciones costosas.
 * - Mensajes de estado y error.
 *
 * Arquitectura:
 * - Todos los métodos pesados (resolución de tours) se ejecutan en segundo plano.
 * - La UI se actualiza exclusivamente en el hilo de JavaFX.
 * - Reutiliza toda la lógica existente (KnightBacktracking, KnightDPPathDP, etc.).
 *
 * Complejidad general:
 * - Mostrar ventana: O(1)
 * - Operaciones de E/S (guardar/cargar): O(n²)
 * - Resolución de tours: depende del algoritmo (ver métodos específicos).
 */
public class Menu {

    private Stage primaryStage;
    private BorderPane mainLayout;
    private Label statusLabel;
    private ProgressIndicator spinner;

    /**
     * Inicializa y muestra la ventana principal del menú en el hilo de JavaFX.
     *
     * Complejidad temporal: O(1)
     * Complejidad espacial: O(1)
     *
     * Nota: Este método debe llamarse solo una vez.
     */
    public void show() {
        FxBootstrap.ensureJavaFxToolkitStarted();
        Platform.runLater(this::createAndShowWindow);
    }

    /**
     * Crea y muestra la ventana principal con todos sus componentes.
     *
     * Complejidad temporal: O(1) (creación de controles simples)
     * Complejidad espacial: O(1)
     */
    private void createAndShowWindow() {
        primaryStage = new Stage();
        primaryStage.setTitle("TP Knight's tour — Menú Principal");
        primaryStage.setOnCloseRequest(e -> System.exit(0));

        mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        // Título principal
        Label titleLabel = new Label("TP Knight's tour");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setAlignment(Pos.CENTER);
        mainLayout.setTop(titleLabel);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);

        // Botones de acción
        VBox buttonBox = new VBox(12);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20));

        Button btnBacktracking = createStyledButton("1) Generar tour (Backtracking) y guardar");
        Button btnWarnsdorff = createStyledButton("2) Generar tour (Warnsdorff) y guardar");
        Button btnShowClassic = createStyledButton("3) Mostrar tour clásico guardado");
        Button btnDPGenerate = createStyledButton("4) Generar caso DP (puntajes + k movs) y guardar");
        Button btnDPShow = createStyledButton("5) Mostrar caso DP guardado");
        Button btnExit = createStyledButton("0) Salir");

        // Asociar acciones a los botones
        btnBacktracking.setOnAction(e -> generateAndSaveClassicTour("backtracking"));
        btnWarnsdorff.setOnAction(e -> generateAndSaveClassicTour("warnsdorff"));
        btnShowClassic.setOnAction(e -> pickAndShowClassicTourFromDisk());
        btnDPGenerate.setOnAction(e -> generateAndSaveDPCase());
        btnDPShow.setOnAction(e -> pickAndShowDPCaseFromDisk());
        btnExit.setOnAction(e -> System.exit(0));

        buttonBox.getChildren().addAll(
                btnBacktracking, btnWarnsdorff, btnShowClassic,
                btnDPGenerate, btnDPShow, btnExit
        );
        mainLayout.setCenter(buttonBox);

        // Barra de estado con spinner de carga
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER);
        statusLabel = new Label("Listo");
        spinner = new ProgressIndicator();
        spinner.setVisible(false);
        spinner.setPrefSize(20, 20);
        statusBox.getChildren().addAll(spinner, statusLabel);
        mainLayout.setBottom(statusBox);
        BorderPane.setAlignment(statusBox, Pos.CENTER);

        Scene scene = new Scene(mainLayout, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    // ========================================================================
    // ===================== MÉTODOS: TOUR CLÁSICO ============================
    // ========================================================================

    /**
     * Genera y guarda un tour clásico usando el algoritmo especificado.
     *
     * Flujo:
     * 1. Solicita parámetros mediante diálogos modales.
     * 2. Ejecuta el solver en segundo plano (muestra spinner).
     * 3. Guarda el resultado en disco.
     * 4. Muestra mensaje de éxito o error.
     *
     * Complejidad temporal:
     *   - Backtracking: O(8^(n²)) en el peor caso.
     *   - Warnsdorff: O(n²) en la práctica.
     *
     * Complejidad espacial: O(n²)
     *
     * @param algorithmName Nombre del algoritmo ("backtracking" o "warnsdorff")
     */
    private void generateAndSaveClassicTour(String algorithmName) {
        // Entrada de parámetros (O(1) por diálogo)
        int boardSize = askForBoardSize();
        if (boardSize == -1) return;

        int startRow = askForStartRow(boardSize);
        if (startRow == -1) return;

        int startCol = askForStartCol(boardSize);
        if (startCol == -1) return;

        // Ejecución en segundo plano
        executeInBackground(() -> {
            Tour tour = switch (algorithmName) {
                case "backtracking" -> KnightBacktracking.solveUsingBacktracking(boardSize, startRow, startCol);
                case "warnsdorff" -> KnightWarnsdorff.solveUsingWarnsdorff(boardSize, startRow, startCol);
                default -> null;
            };

            if (tour == null) {
                Platform.runLater(() ->
                        showError("No se encontró tour con " + algorithmName + " para esos parámetros."));
                return;
            }

            String filename = TourRepository.filenameForClassicTour(boardSize, startRow, startCol, algorithmName);
            TourRepository.saveClassicTour(filename, tour);
            Platform.runLater(() ->
                    showInfo("Tour guardado en: " + filename));
        });
    }

    /**
     * Permite al usuario elegir un tour clásico guardado y mostrarlo en una ventana animada.
     *
     * Flujo:
     * 1. Lista archivos guardados.
     * 2. Muestra diálogo de selección.
     * 3. Carga el tour.
     * 4. Solicita preferencias de visualización.
     * 5. Abre ventana de animación.
     *
     * Complejidad temporal:
     *   - Listar archivos: O(m log m), m = cantidad de archivos.
     *   - Cargar tour: O(n²)
     *   - Mostrar ventana: O(1)
     *
     * Complejidad espacial: O(n²)
     */
    private void pickAndShowClassicTourFromDisk() {
        List<File> files = listFilesByPrefix("tour_");
        if (files.isEmpty()) {
            showInfo("No hay tours guardados. Generá con opción 1 o 2.");
            return;
        }

        ChoiceDialog<File> dialog = new ChoiceDialog<>(files.get(0), files);
        dialog.setTitle("Elegí un tour guardado");
        dialog.setHeaderText("Seleccioná un archivo:");
        Optional<File> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        File chosen = result.get();
        Tour tour = TourRepository.loadClassicTour(chosen.getName());
        if (tour == null) {
            showError("No se pudo cargar el archivo.");
            return;
        }

        boolean useWhite = showYesNoDialog("¿Caballo blanco?", true);
        int delay = askForDelay();

        FxBoardWindow.showTourInNewWindow(tour, delay, useWhite);
    }

    // ========================================================================
    // ======================== MÉTODOS: FLUJO DP =============================
    // ========================================================================

    /**
     * Genera un caso de Programación Dinámica, lo resuelve y guarda en disco.
     *
     * Flujo:
     * 1. Solicita parámetros (n, sr, sc, k, semilla).
     * 2. Genera matriz de puntajes (1..100).
     * 3. Resuelve con KnightDPPathDP en segundo plano.
     * 4. Guarda resultado y muestra resumen.
     *
     * Complejidad temporal:
     *   - Generación de puntajes: O(n²)
     *   - Resolución DP: O(8 * n² * 2^(k+1)) con BitSet (viable para k ≤ 15-20)
     *   - Guardado: O(n²)
     *
     * Complejidad espacial: O(n² + 2^(k+1)) para la búsqueda
     */
    private void generateAndSaveDPCase() {
        int boardSize = askForBoardSize();
        if (boardSize == -1) return;

        int startRow = askForStartRow(boardSize);
        if (startRow == -1) return;

        int startCol = askForStartCol(boardSize);
        if (startCol == -1) return;

        int maxMoves = askForMaxMoves();
        if (maxMoves == -1) return;

        long seed = askForSeed();
        if (seed == -1) return;

        executeInBackground(() -> {
            int[][] points = generateRandomPointsMatrix_1to100(boardSize, seed);
            DPResult result = KnightDPPathDP.solveMaxScorePathUsingDP(points, maxMoves, startRow, startCol);

            String filename = TourRepository.filenameForDPCase(boardSize, startRow, startCol, maxMoves, seed);
            TourRepository.saveDPCase(filename, result, seed);

            int actualMoves = computeMaxStepNumber(result.visitOrderMatrix) - 1;
            Platform.runLater(() -> {
                showInfo(String.format(
                        "Guardado DP en: %s\nScore: %d\nMovimientos: %d/%d",
                        filename, result.bestAccumulatedScore, actualMoves, maxMoves
                ));
            });
        });
    }

    /**
     * Carga un caso DP guardado y lo muestra en una ventana animada con puntajes.
     *
     * Flujo:
     * 1. Lista archivos DP.
     * 2. Diálogo de selección.
     * 3. Carga el caso.
     * 4. Solicita preferencias.
     * 5. Abre ventana con overlay de puntajes y HUD.
     *
     * Complejidad temporal: O(n²) (carga y visualización)
     * Complejidad espacial: O(n²)
     */
    private void pickAndShowDPCaseFromDisk() {
        List<File> files = listFilesByPrefix("dp_");
        if (files.isEmpty()) {
            showInfo("No hay casos DP guardados. Generá con opción 4.");
            return;
        }

        ChoiceDialog<File> dialog = new ChoiceDialog<>(files.get(0), files);
        dialog.setTitle("Elegí un caso DP guardado");
        dialog.setHeaderText("Seleccioná un archivo:");
        Optional<File> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        File chosen = result.get();
        DPResult dpResult = TourRepository.loadDPCase(chosen.getName());
        if (dpResult == null) {
            showError("No se pudo cargar el archivo DP.");
            return;
        }

        boolean useWhite = showYesNoDialog("¿Caballo blanco?", true);
        int delay = askForDelay();

        FxBoardWindow.showTourInNewWindow(
                dpResult.toTour(), delay, useWhite, dpResult.pointsMatrix
        );
    }

    // ========================================================================
    // ========================= MÉTODOS AUXILIARES ===========================
    // ========================================================================

    /**
     * Crea un botón con estilo consistente para el menú principal.
     *
     * Complejidad: O(1)
     */
    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setMinWidth(350);
        btn.setMinHeight(40);
        btn.setFont(Font.font("System", 14));
        return btn;
    }

    /**
     * Ejecuta una tarea en segundo plano, mostrando un spinner durante la ejecución.
     *
     * Complejidad: O(1) + costo de la tarea
     *
     * @param task Tarea a ejecutar en segundo plano
     */
    private void executeInBackground(Runnable task) {
        Platform.runLater(() -> {
            spinner.setVisible(true);
            statusLabel.setText("Resolviendo...");
        });

        new Thread(() -> {
            try {
                task.run();
            } finally {
                Platform.runLater(() -> {
                    spinner.setVisible(false);
                    statusLabel.setText("Listo");
                });
            }
        }).start();
    }

    /**
     * Muestra un diálogo de error al usuario.
     *
     * Complejidad: O(1)
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Muestra un diálogo de información al usuario.
     *
     * Complejidad: O(1)
     */
    private void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Información");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Muestra un diálogo de confirmación Sí/No.
     *
     * Complejidad: O(1)
     */
    private boolean showYesNoDialog(String message, boolean defaultYes) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        return result.orElse(ButtonType.NO) == ButtonType.YES;
    }

    /**
     * Pide al usuario el delay de animación.
     *
     * Complejidad: O(1)
     */
    private int askForDelay() {
        TextInputDialog dialog = new TextInputDialog("150");
        dialog.setTitle("Delay de animación");
        dialog.setHeaderText("Delay entre pasos (ms):");
        dialog.setContentText("Delay:");
        Optional<String> result = dialog.showAndWait();
        try {
            return Integer.parseInt(result.orElse("150"));
        } catch (NumberFormatException e) {
            return 150;
        }
    }

    // --- Métodos para entrada de parámetros ---
    /**
     * Solicita el tamaño del tablero (n).
     *
     * Complejidad: O(1)
     *
     * @return n ≥ 1, o -1 si se cancela/error
     */
    private int askForBoardSize() {
        TextInputDialog dialog = new TextInputDialog("6");
        dialog.setTitle("Tamaño del tablero");
        dialog.setHeaderText("Ingresá el tamaño n del tablero:");
        dialog.setContentText("n:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return -1;
        try {
            int n = Integer.parseInt(result.get());
            if (n < 1) throw new NumberFormatException();
            return n;
        } catch (NumberFormatException e) {
            showError("Tamaño inválido. Debe ser un entero ≥ 1.");
            return -1;
        }
    }

    /**
     * Solicita la fila inicial (0 ≤ sr < n).
     *
     * Complejidad: O(1)
     */
    private int askForStartRow(int n) {
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Fila inicial");
        dialog.setHeaderText("Fila inicial [0.." + (n - 1) + "]:");
        dialog.setContentText("Fila:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return -1;
        try {
            int r = Integer.parseInt(result.get());
            if (r < 0 || r >= n) throw new IllegalArgumentException();
            return r;
        } catch (Exception e) {
            showError("Fila inválida. Debe estar en [0, " + (n - 1) + "].");
            return -1;
        }
    }

    /**
     * Solicita la columna inicial (0 ≤ sc < n).
     *
     * Complejidad: O(1)
     */
    private int askForStartCol(int n) {
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Columna inicial");
        dialog.setHeaderText("Columna inicial [0.." + (n - 1) + "]:");
        dialog.setContentText("Columna:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return -1;
        try {
            int c = Integer.parseInt(result.get());
            if (c < 0 || c >= n) throw new IllegalArgumentException();
            return c;
        } catch (Exception e) {
            showError("Columna inválida. Debe estar en [0, " + (n - 1) + "].");
            return -1;
        }
    }

    /**
     * Solicita la cantidad máxima de movimientos (k ≥ 0).
     *
     * Complejidad: O(1)
     */
    private int askForMaxMoves() {
        TextInputDialog dialog = new TextInputDialog("10");
        dialog.setTitle("Cantidad de movimientos");
        dialog.setHeaderText("Cantidad de movimientos k:");
        dialog.setContentText("k:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return -1;
        try {
            int k = Integer.parseInt(result.get());
            if (k < 0) throw new NumberFormatException();
            return k;
        } catch (NumberFormatException e) {
            showError("k inválido. Debe ser un entero ≥ 0.");
            return -1;
        }
    }

    /**
     * Solicita la semilla para generar puntajes.
     *
     * Complejidad: O(1)
     */
    private long askForSeed() {
        TextInputDialog dialog = new TextInputDialog("123");
        dialog.setTitle("Semilla para puntajes");
        dialog.setHeaderText("Semilla para generar puntajes (1..100):");
        dialog.setContentText("Semilla:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return -1;
        try {
            return Long.parseLong(result.get());
        } catch (NumberFormatException e) {
            showError("Semilla inválida.");
            return -1;
        }
    }

    // --- Métodos reutilizados del menú de consola ---
    /**
     * Lista archivos .csv que comienzan con un prefijo.
     *
     * Complejidad temporal: O(m log m), m = cantidad de archivos
     * Complejidad espacial: O(m)
     */
    private List<File> listFilesByPrefix(String prefix) {
        File dir = new File(".");
        File[] files = dir.listFiles((d, name) ->
                name.toLowerCase().startsWith(prefix) && name.toLowerCase().endsWith(".csv"));
        if (files == null) return List.of();
        return Arrays.stream(files)
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());
    }

    /**
     * Genera una matriz de puntajes aleatorios (1..100).
     *
     * Complejidad temporal: O(n²)
     * Complejidad espacial: O(n²)
     */
    private int[][] generateRandomPointsMatrix_1to100(int n, long seed) {
        java.util.Random rnd = new java.util.Random(seed);
        int[][] p = new int[n][n];
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                p[r][c] = 1 + rnd.nextInt(100);
        return p;
    }

    /**
     * Calcula el número máximo de paso en una matriz de orden.
     *
     * Complejidad temporal: O(n²)
     *
     * @return Paso máximo (≥1)
     */
    private int computeMaxStepNumber(int[][] order) {
        int max = 0;
        for (int[] row : order)
            for (int v : row)
                if (v > max) max = v;
        return Math.max(1, max);
    }
}