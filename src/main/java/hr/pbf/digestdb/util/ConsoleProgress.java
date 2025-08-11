package hr.pbf.digestdb.util;

public class ConsoleProgress {
    private static long startTime = 0;
    private static long lastUpdateTime = 0;
    private static int lastCurrent = 0;
    private static double averageSpeed = 0.0;
    private static int speedSamples = 0;

    public static void setProgress(int current, int total, String message) {
        if (total <= 0) {
            throw new IllegalArgumentException("Total must be greater than zero.");
        }
        if (current < 0 || current > total) {
            throw new IllegalArgumentException("Current must be between 0 and total.");
        }

        long currentTime = System.currentTimeMillis();

        // Inicijaliziraj startTime pri prvom pozivu
        if (startTime == 0) {
            startTime = currentTime;
            lastUpdateTime = currentTime;
            lastCurrent = current;
        }

        int percent = (int) ((double) current / total * 100);
        String progressBar = "[" + "=".repeat(percent / 2) + " ".repeat(50 - percent / 2) + "]";

        String eta = "";
        if (current > 0 && current < total) {
            // Izračunaj trenutnu brzinu
            long timeDiff = currentTime - lastUpdateTime;
            int itemsDiff = current - lastCurrent;

            if (timeDiff > 0 && itemsDiff > 0) {
                double currentSpeed = (double) itemsDiff / (timeDiff / 1000.0);

                // Ažuriraj prosječnu brzinu (eksponencijalni prosjek)
                if (speedSamples == 0) {
                    averageSpeed = currentSpeed;
                } else {
                    averageSpeed = (averageSpeed * 0.8) + (currentSpeed * 0.2);
                }
                speedSamples++;
            }

            // Koristi prosjećnu brzinu za ETA
            if (averageSpeed > 0) {
                double remainingItems = total - current;
                long remainingTime = (long) ((remainingItems / averageSpeed) * 1000);
                eta = String.format(" - ETA: %s (%.1f/s)",
                      formatDuration(remainingTime), averageSpeed);
            }
        }

        System.out.printf("\r%s %d%% (%d/%d)%s - %s",
              progressBar, percent, current, total, eta, message);

        if (current == total) {
            long totalTime = currentTime - startTime;
            double finalSpeed = (double) total / (totalTime / 1000.0);
            System.out.printf(" - Završeno za: %s (%.1f/s)\n",
                  formatDuration(totalTime), finalSpeed);

            // Reset za sljedeći progress
            startTime = 0;
            averageSpeed = 0.0;
            speedSamples = 0;
        }

        lastUpdateTime = currentTime;
        lastCurrent = current;
    }

    private static String formatDuration(long milliseconds) {
        if (milliseconds < 0) return "00:00:00";

        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds = seconds % 60;
        minutes = minutes % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static void main(String[] args) {
        // Sporiji test za realističnu ETA
        for (int i = 0; i <= 100; i++) {
            setProgress(i, 100, "Procesiranje...");
            try {
                // Varijabilna brzina za testiranje ETA
                Thread.sleep(100 + (i % 10) * 50); // 100-600ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
