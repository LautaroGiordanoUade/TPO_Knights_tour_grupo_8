package iu.fx;

import javafx.application.Platform;

/**
 * Inicializa el toolkit de JavaFX una sola vez desde una app “de consola”.
 */
public class FxBootstrap {
    private static volatile boolean toolkitStarted = false;

    public static synchronized void ensureJavaFxToolkitStarted() {
        if (toolkitStarted) return;
        // Inicia JavaFX sin Stage visible aún.
        Platform.startup(() -> {});
        toolkitStarted = true;
    }
}
