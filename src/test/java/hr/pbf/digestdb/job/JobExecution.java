package hr.pbf.digestdb.job;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobExecution {
    private final long jobId;
    private final String jobName;
    private final Object jobParams;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private JobStatus status;
    private String errorMessage;
    
    public JobExecution(long jobId, String jobName, Object jobParams) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.jobParams = jobParams;
        this.startTime = LocalDateTime.now();
        this.status = JobStatus.PENDING;
    }
    
    public long getDurationMs() {
        if (endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        return java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();
    }
}
