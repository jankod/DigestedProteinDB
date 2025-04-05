package hr.pbf.digestdb.workflow.core;

import lombok.Data;

@Data
public final class JobResult<R> {

    private R result;
    private long startTime;
    private long endTime;

}
