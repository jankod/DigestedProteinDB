package hr.pbf.digestdb.job;

import java.util.List;
import java.util.Optional;

// Job tracking service
interface JobTrackingService {
    void saveJobExecution(JobExecution jobExecution);
    void updateJobExecution(JobExecution jobExecution);
    void saveTaskExecution(long jobId, TaskExecution taskExecution);
    void updateTaskExecution(long jobId, TaskExecution taskExecution);
    Optional<JobExecution> getJobExecution(long jobId);
    List<JobExecution> getAllJobs();
    List<JobExecution> getJobsByStatus(JobStatus status);
    List<TaskExecution> getTasksForJob(long jobId);
}
