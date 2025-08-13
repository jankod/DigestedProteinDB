package hr.pbf.digestdb.job;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneId;


@Data
class TaskExecution {
    private final long taskId;
    private final String taskName;
    private final String description;
    private final int order;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private TaskStatus status;
    private String errorMessage;
    private Class<?> resultType;
    private String resultSummary;

    public TaskExecution(long taskId, String taskName, String description, int order) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.description = description;
        this.order = order;
        this.startTime = LocalDateTime.now();
        this.status = TaskStatus.PENDING;
    }

    public long getDurationMs() {
        if (endTime == null && status == TaskStatus.RUNNING) {
            return System.currentTimeMillis() -
                     ZoneId.systemDefault().getRules()
                           .getOffset(startTime.atZone(ZoneId.systemDefault()).toInstant())
                           .getTotalSeconds() * 1000L;
        }
        if (endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        return 0;
    }
}
