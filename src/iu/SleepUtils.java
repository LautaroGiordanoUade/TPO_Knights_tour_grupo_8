package ui;

public class SleepUtils {
    public static void sleepMillis(long milliseconds) {
        try { Thread.sleep(milliseconds); } catch (InterruptedException ignored) {}
    }
    // Alias, por compatibilidad con código viejo:
    public static void ms(long milliseconds) { sleepMillis(milliseconds); }
}
