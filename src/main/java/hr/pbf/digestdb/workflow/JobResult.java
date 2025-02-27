package hr.pbf.digestdb.workflow;

import lombok.Data;
import org.apache.commons.lang3.time.DurationFormatUtils;

@Data
public final class JobResult<R> {
    private R result;
    private long startTime;
    private long endTime;

    public String duration() {
        return DurationFormatUtils.formatDuration(endTime - startTime, "HH:mm:ss:SSS");
    }
}
