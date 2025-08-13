package hr.pbf.digestdb.util;

import java.util.ArrayDeque;
import java.util.Deque;

public class ConsoleProgress {
    private static long startTime = 0;
    private static long lastUpdateTime = 0;
    private static int lastCurrent = 0;

    // Koristi klizni prozor za preciznije procjene
    private static final Deque<SpeedSample> speedSamples = new ArrayDeque<>();
    private static final int MAX_SAMPLES = 10; // Broj uzoraka za prosjek
    private static final long MIN_UPDATE_INTERVAL = 100; // Minimalno 100ms između ažuriranja

    // Klasa za čuvanje uzoraka brzine
    private static class SpeedSample {
        final long timestamp;
        final int progress;
        final double speed;

        SpeedSample(long timestamp, int progress, double speed) {
            this.timestamp = timestamp;
            this.progress = progress;
            this.speed = speed;
        }
    }

    public static void setProgress(int current, int total, String message) {
        if (total <= 0) {
            throw new IllegalArgumentException("Total must be greater than zero.");
        }
        if (current < 0 || current > total) {
            throw new IllegalArgumentException("Current must be between 0 and total.");
        }

        long currentTime = System.currentTimeMillis();

        // Inicijaliziraj pri prvom pozivu
        if (startTime == 0) {
            startTime = currentTime;
            lastUpdateTime = currentTime;
            lastCurrent = current;
            speedSamples.clear();
        }

        int percent = (int) ((double) current / total * 100);
        String progressBar = "[" + "=".repeat(percent / 2) + " ".repeat(50 - percent / 2) + "]";

        String eta = "";
        if (current > 0 && current < total) {
            // Ažuriraj brzinu samo ako je prošlo dovoljno vremena
            long timeDiff = currentTime - lastUpdateTime;
            int itemsDiff = current - lastCurrent;

            if (timeDiff >= MIN_UPDATE_INTERVAL && itemsDiff > 0) {
                double currentSpeed = (double) itemsDiff / (timeDiff / 1000.0);

                // Dodaj novi uzorak
                speedSamples.addLast(new SpeedSample(currentTime, current, currentSpeed));

                // Ukloni stare uzorke (starije od 30 sekundi ili više od MAX_SAMPLES)
                long cutoffTime = currentTime - 30000;
                while (!speedSamples.isEmpty() &&
                       (speedSamples.peekFirst().timestamp < cutoffTime ||
                        speedSamples.size() > MAX_SAMPLES)) {
                    speedSamples.removeFirst();
                }

                lastUpdateTime = currentTime;
                lastCurrent = current;
            }

            // Izračunaj ETA koristeći različite metode ovisno o dostupnim podacima
            eta = calculateETA(current, total, currentTime);
        }

        System.out.printf("\r%s %d%% (%d/%d)%s - %s",
              progressBar, percent, current, total, eta, message);

        if (current == total) {
            long totalTime = currentTime - startTime;
            double finalSpeed = (double) total / (totalTime / 1000.0);
            System.out.printf(" - Završeno za: %s (%.1f/s)\n",
                  formatDuration(totalTime), finalSpeed);

            // Reset za sljedeći progress
            reset();
        }
    }

    private static String calculateETA(int current, int total, long currentTime) {
        if (speedSamples.isEmpty()) {
            return "";
        }

        double estimatedSpeed;
        String method;

        // Ako imamo dovoljno uzoraka, koristi klizni prosjek
        if (speedSamples.size() >= 3) {
            // Težišni prosjek - noviji uzorci imaju veću težinu
            double weightedSum = 0.0;
            double totalWeight = 0.0;

            int i = 0;
            for (SpeedSample sample : speedSamples) {
                double weight = Math.pow(1.5, i); // Eksponencijalno rastući težinski faktor
                weightedSum += sample.speed * weight;
                totalWeight += weight;
                i++;
            }

            estimatedSpeed = weightedSum / totalWeight;
            method = "klizni";

        } else {
            // Za manje uzoraka, koristi linearnu regresiju na cijelom tijeku
            long totalElapsed = currentTime - startTime;
            if (totalElapsed > 1000) { // Minimalno 1 sekunda
                estimatedSpeed = (double) current / (totalElapsed / 1000.0);
                method = "ukupni";
            } else {
                return "";
            }
        }

        if (estimatedSpeed > 0) {
            double remainingItems = total - current;
            long remainingTime = (long) ((remainingItems / estimatedSpeed) * 1000);

            // Dodaj stabilizaciju za vrlo kratka preostala vremena
            if (remainingTime < 1000) {
                remainingTime = 1000;
            }

            return String.format(" - ETA: %s (%.1f/s, %s)",
                  formatDuration(remainingTime), estimatedSpeed, method);
        }

        return "";
    }

    private static void reset() {
        startTime = 0;
        speedSamples.clear();
        lastUpdateTime = 0;
        lastCurrent = 0;
    }

    private static String formatDuration(long milliseconds) {
        if (milliseconds < 0) return "00:00:00";

        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds = seconds % 60;
        minutes = minutes % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    public static void main(String[] args) {
        System.out.println("Test 1: Konstantna brzina");
        for (int i = 0; i <= 50; i++) {
            setProgress(i, 50, "Konstantna brzina...");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("\n\nTest 2: Varijabilna brzina (sporo na početku, brže na kraju)");
        for (int i = 0; i <= 50; i++) {
            setProgress(i, 50, "Varijabilna brzina...");
            try {
                // Postupno ubrzavanje
                long delay = Math.max(50, 500 - i * 8);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("\n\nTest 3: Nepredvidljiva brzina");
        for (int i = 0; i <= 50; i++) {
            setProgress(i, 50, "Nepredvidljiva brzina...");
            try {
                // Nasumična brzina
                long delay = 100 + (int) (Math.random() * 400);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
