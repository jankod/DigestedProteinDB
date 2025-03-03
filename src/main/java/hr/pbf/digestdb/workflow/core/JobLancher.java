package hr.pbf.digestdb.workflow.core;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Data
@Slf4j
public class JobLancher {

    private List<JobExecution<?>> jobs = new ArrayList<>();


    private ExecutorService executorService;

    public JobLancher() {
        executorService = Executors.newFixedThreadPool(8);
    }

    private <R> void run(JobExecution<R> job) {
        JobContext jobContext = new JobContext();

        JobResult<R> jobResult = new JobResult<>();
        try {
            job.status = JobExecution.Status.STARTED;
            long time1 = System.currentTimeMillis();
            R result = job.getJob().start(jobContext);
            job.status = JobExecution.Status.FINISHED;
            jobResult.setStartTime(time1);
            jobResult.setResult(result);
        } catch (Exception e) {
            job.status = JobExecution.Status.FAILED;
            job.exception = e;
        }
        long time2 = System.currentTimeMillis();
        job.setResult(jobResult);
        jobResult.setEndTime(time2);

    }

    public <R> void runAll() {
        for (JobExecution<?> job : jobs) {
           // Future<R> future = executorService.submit((Callable<R>) () -> run(job));
            run(job);
        }
    }

    public void addJob(Job<?> job) {
        jobs.add(new JobExecution(job));
    }

    @Data
    static
    class JobExecution<R> {
        Job<R> job;
        JobResult<R> result;
        Exception exception;

        enum Status {
            NOT_STARTED, STARTED, FINISHED, FAILED
        }

        Status status = Status.NOT_STARTED;

        JobExecution(Job<R> job) {
            Objects.requireNonNull(job);
            this.job = job;
        }
    }

}
