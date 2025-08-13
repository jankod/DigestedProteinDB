package hr.pbf.digestdb.job;

import java.util.concurrent.atomic.AtomicLong;

public class JobIDUtil {
    private static final AtomicLong jobIdCounter = new AtomicLong(1);
    private static final AtomicLong taskIdCounter = new AtomicLong(1);

    public static long generateJobId(Class<?> jobClass) {
        return jobIdCounter.getAndIncrement();
    }

    public static long generateTaskId() {
        return taskIdCounter.getAndIncrement();
    }
}
