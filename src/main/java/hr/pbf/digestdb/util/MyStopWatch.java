package hr.pbf.digestdb.util;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

public class MyStopWatch {

    private String name;

    StopWatch stopWatch;

    public MyStopWatch() {
        stopWatch = new StopWatch();
        stopWatch.start();
    }

    public void printDuration(String name) {
        stopWatch.split();
        System.out.println(name + " " + DurationFormatUtils.formatDurationHMS(stopWatch.getSplitTime()));
    }

    public StopWatch getStopWatch() {
        return stopWatch;
    }
}
