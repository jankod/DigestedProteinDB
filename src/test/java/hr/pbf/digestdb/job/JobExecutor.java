package hr.pbf.digestdb.job;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class JobExecutor {


    private final JobTrackingService trackingService;

    public JobExecutor(JobTrackingService trackingService) {
        this.trackingService = trackingService;
    }

    public JobExecutor() {
        this(new InMemoryJobTrackingService());
    }

    private static class TaskMethod {
        final Method method;
        final Task annotation;
        final Class<?>[] parameterTypes;

        TaskMethod(Method method, Task annotation) {
            this.method = method;
            this.annotation = annotation;
            this.parameterTypes = method.getParameterTypes();
        }
    }

    public <T> long executeJob(Class<T> jobClass, Object jobParams) throws Exception {
        long jobId = JobIDUtil.generateJobId(jobClass);

        JobExecution jobExecution = new JobExecution(jobId, jobClass.getSimpleName(), jobParams);
        trackingService.saveJobExecution(jobExecution);

        try {
            log.info("Starting job execution for class: {} with ID: {}", jobClass.getSimpleName(), jobId);

            jobExecution.setStatus(JobStatus.RUNNING);
            trackingService.updateJobExecution(jobExecution);

            // Create job instance
            T jobInstance = createJobInstance(jobClass, jobParams);

            // Scan and sort tasks
            List<TaskMethod> tasks = scanJobClass(jobClass);

            // Execute tasks in order
            Map<Class<?>, Object> taskResults = new HashMap<>();

            for (TaskMethod taskMethod : tasks) {
                TaskExecution taskExecution = executeTaskWithTracking(
                        jobId, jobInstance, taskMethod, taskResults);

                if (taskExecution.getStatus() == TaskStatus.FAILED) {
                    jobExecution.setStatus(JobStatus.FAILED);
                    jobExecution.setErrorMessage("Task " + taskExecution.getTaskName() + " failed: " + taskExecution.getErrorMessage());
                    break;
                }
            }

            if (jobExecution.getStatus() == JobStatus.RUNNING) {
                jobExecution.setStatus(JobStatus.COMPLETED);
            }

            jobExecution.setEndTime(LocalDateTime.now());
            trackingService.updateJobExecution(jobExecution);

            log.info("Job execution completed successfully! Job ID: {}, Duration: {} ms",
                    jobId, jobExecution.getDurationMs());

            return jobId;

        } catch (Exception e) {
            jobExecution.setStatus(JobStatus.FAILED);
            jobExecution.setErrorMessage(e.getMessage());
            jobExecution.setEndTime(LocalDateTime.now());
            trackingService.updateJobExecution(jobExecution);

            log.error("Job execution failed for ID: {}", jobId, e);
            throw e;
        }
    }

    private TaskExecution executeTaskWithTracking(long jobId, Object jobInstance,
                                                 TaskMethod taskMethod, Map<Class<?>, Object> availableResults) {

        long taskId = JobIDUtil.generateTaskId();
        TaskExecution taskExecution = new TaskExecution(
                taskId,
                taskMethod.annotation.name(),
                taskMethod.annotation.description(),
                taskMethod.annotation.order()
        );

        trackingService.saveTaskExecution(jobId, taskExecution);

        try {
            log.info("Executing task: {} (order: {}, ID: {})",
                    taskMethod.annotation.name(),
                    taskMethod.annotation.order(),
                    taskId);

            taskExecution.setStatus(TaskStatus.RUNNING);
            trackingService.updateTaskExecution(jobId, taskExecution);

            Object result = executeTask(jobInstance, taskMethod, availableResults);

            if (result != null) {
                availableResults.put(result.getClass(), result);
                taskExecution.setResultType(result.getClass());
                taskExecution.setResultSummary(result.toString().substring(0,
                        Math.min(100, result.toString().length())));
            }

            taskExecution.setStatus(TaskStatus.COMPLETED);
            taskExecution.setEndTime(LocalDateTime.now());
            trackingService.updateTaskExecution(jobId, taskExecution);

            log.info("Task {} completed successfully in {} ms",
                    taskMethod.annotation.name(),
                    taskExecution.getDurationMs());

            return taskExecution;

        } catch (Exception e) {
            taskExecution.setStatus(TaskStatus.FAILED);
            taskExecution.setErrorMessage(e.getMessage());
            taskExecution.setEndTime(LocalDateTime.now());
            trackingService.updateTaskExecution(jobId, taskExecution);

            log.error("Task {} failed: {}", taskMethod.annotation.name(), e.getMessage(), e);
            return taskExecution;
        }
    }

    private <T> T createJobInstance(Class<T> jobClass, Object jobParams) throws Exception {
        Constructor<?>[] constructors = jobClass.getDeclaredConstructors();

        for (Constructor<?> constructor : constructors) {
            Parameter[] parameters = constructor.getParameters();
            if (parameters.length == 1 && parameters[0].getType().isAssignableFrom(jobParams.getClass())) {
                constructor.setAccessible(true);
                return (T) constructor.newInstance(jobParams);
            }
        }

        throw new IllegalArgumentException("No suitable constructor found for job class: " + jobClass.getName());
    }

    private <T> List<TaskMethod> scanJobClass(Class<T> jobClass) {
        List<TaskMethod> tasks = new ArrayList<>();

        Method[] methods = jobClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Task.class)) {
                Task taskAnnotation = method.getAnnotation(Task.class);
                method.setAccessible(true);
                tasks.add(new TaskMethod(method, taskAnnotation));
            }
        }

        tasks.sort(Comparator.comparingInt(t -> t.annotation.order()));

        log.info("Found {} tasks in job class {}", tasks.size(), jobClass.getSimpleName());
        return tasks;
    }

    private Object executeTask(Object jobInstance, TaskMethod taskMethod, Map<Class<?>, Object> availableResults)
            throws Exception {

        Object[] args = new Object[taskMethod.parameterTypes.length];

        for (int i = 0; i < taskMethod.parameterTypes.length; i++) {
            Class<?> paramType = taskMethod.parameterTypes[i];
            Object result = availableResults.get(paramType);

            if (result == null) {
                throw new IllegalStateException(
                    String.format("Required parameter of type %s not available for task %s",
                            paramType.getName(), taskMethod.annotation.name()));
            }

            args[i] = result;
        }

        return taskMethod.method.invoke(jobInstance, args);
    }

    // Public API methods for job monitoring
    public List<JobExecution> getRunningJobs() {
        return trackingService.getJobsByStatus(JobStatus.RUNNING);
    }

    public Optional<JobExecution> getJobStatus(long jobId) {
        return trackingService.getJobExecution(jobId);
    }

    public List<TaskExecution> getJobTasks(long jobId) {
        return trackingService.getTasksForJob(jobId);
    }
}
