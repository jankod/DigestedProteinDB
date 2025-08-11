package hr.pbf.digestdb.util;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

public class MyStopWatch {

    private long lastTime;

    public MyStopWatch() {
        lastTime = System.currentTimeMillis();
    }

    public String getCurrentDuration() {
        long currentTime = System.currentTimeMillis();
        long duration = currentTime - lastTime;
        lastTime = currentTime; // Ažuriraj za sljedeći poziv
        return DurationFormatUtils.formatDurationHMS(duration);
    }
}
