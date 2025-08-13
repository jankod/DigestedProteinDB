package hr.pbf.digestdb.job;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// In-memory implementation (you can replace with JPA/database implementation)
class InMemoryJobTrackingService implements JobTrackingService {
    private final Map<Long, JobExecution> jobs = new ConcurrentHashMap<>();
    private final Map<Long, List<TaskExecution>> jobTasks = new ConcurrentHashMap<>();

    @Override
    public void saveJobExecution(JobExecution jobExecution) {
        jobs.put(jobExecution.getJobId(), jobExecution);
        jobTasks.put(jobExecution.getJobId(), new ArrayList<>());
    }

    @Override
    public void updateJobExecution(JobExecution jobExecution) {
        jobs.put(jobExecution.getJobId(), jobExecution);
    }

    @Override
    public void saveTaskExecution(long jobId, TaskExecution taskExecution) {
        jobTasks.computeIfAbsent(jobId, k -> new ArrayList<>()).add(taskExecution);
    }

    @Override
    public void updateTaskExecution(long jobId, TaskExecution taskExecution) {
        List<TaskExecution> tasks = jobTasks.get(jobId);
        if (tasks != null) {
            tasks.removeIf(t -> t.getTaskId() == taskExecution.getTaskId());
            tasks.add(taskExecution);
        }
    }

    @Override
    public Optional<JobExecution> getJobExecution(long jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    @Override
    public List<JobExecution> getAllJobs() {
        return new ArrayList<>(jobs.values());
    }

    @Override
    public List<JobExecution> getJobsByStatus(JobStatus status) {
        return jobs.values().stream()
                .filter(job -> job.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskExecution> getTasksForJob(long jobId) {
        return jobTasks.getOrDefault(jobId, new ArrayList<>());
    }
}
