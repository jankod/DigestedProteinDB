package hr.pbf.digestdb.job;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class DemoJob {

    public static void main(String[] args) throws Exception {
        // Create job parameters
        SimpleJobParam jobParams = new SimpleJobParam("value1", "value2");

        // Create tracking service
        JobTrackingService trackingService = new InMemoryJobTrackingService();

        // Execute job
        JobExecutor jobExecutor = new JobExecutor(trackingService);
        long jobId = jobExecutor.executeJob(SimpleJob1.class, jobParams);

        // Demonstrate tracking capabilities
        demonstrateTracking(trackingService, jobId);
    }

    private static void demonstrateTracking(JobTrackingService trackingService, long jobId) {
        System.out.println("\n=== JOB TRACKING DEMONSTRATION ===");

        // Get job details
        Optional<JobExecution> jobOpt = trackingService.getJobExecution(jobId);
        if (jobOpt.isPresent()) {
            JobExecution job = jobOpt.get();
            System.out.printf("Job ID: %s%n", job.getJobId());
            System.out.printf("Job Name: %s%n", job.getJobName());
            System.out.printf("Status: %s%n", job.getStatus());
            System.out.printf("Duration: %d ms%n", job.getDurationMs());
            System.out.printf("Start Time: %s%n", job.getStartTime());
            System.out.printf("End Time: %s%n", job.getEndTime());

            // Get task details
            System.out.println("\nTasks:");
            List<TaskExecution> tasks = trackingService.getTasksForJob(jobId);
            tasks.stream()
                  .sorted(Comparator.comparingInt(TaskExecution::getOrder))
                  .forEach(task -> {
                      System.out.printf("  - %s (%s): %s [%d ms]%n",
                            task.getTaskName(),
                            task.getStatus(),
                            task.getDescription(),
                            task.getDurationMs());
                  });
        }

        // Show all jobs
        System.out.println("\nAll jobs:");
        trackingService.getAllJobs().forEach(job ->
              System.out.printf("  %s: %s (%s)%n",
                    job.getJobId(),
                    job.getJobName(),
                    job.getStatus()));
    }

    @Slf4j
    @Data
    @RequiredArgsConstructor
    static class SimpleJob1 {

        @JobParam
        private final SimpleJobParam param;

        @Task(order = 1, name = "task1", description = "This is a simple task that does something.")
        public TaskResult1 task1() {
            log.info("Executing task1 with parameters: {}", param);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new TaskResult1("Result from task1");
        }

        @Task(order = 2, name = "task2", description = "This task depends on the result of task1.")
        public TaskResult2 task2(TaskResult1 taskResult1) {
            log.info("Executing task2 with result from task1: {}", taskResult1);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new TaskResult2("Result from task2 based on " + taskResult1.result());
        }

        @Task(order = 3, name = "task3", description = "Final task that uses results from both previous tasks.")
        public TaskResult3 task3(TaskResult1 result1, TaskResult2 result2) {
            log.info("Executing task3 with results: {} and {}", result1, result2);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new TaskResult3("Final result combining: " + result1.result() + " and " + result2.result());
        }
    }
}


// Data classes
record SimpleJobParam(String param1, String param2) {
}

record TaskResult1(String result) {
}

record TaskResult2(String result) {
}

record TaskResult3(String result) {
}
