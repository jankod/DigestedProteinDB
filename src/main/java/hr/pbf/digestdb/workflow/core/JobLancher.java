package hr.pbf.digestdb.workflow.core;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

@Data
@Slf4j
public class JobLancher {

    private List<JobExecution<?>> jobs = new ArrayList<>();

    private ExecutorService executorService;

    public JobLancher() {
        executorService = Executors.newFixedThreadPool(8);
    }

    private <R> void run(JobExecution<R> jobExecution) {
        JobContext jobContext = new JobContext();

        JobResult<R> jobResult = new JobResult<>();
        try {
            jobExecution.status = JobExecution.Status.STARTED;
            long time1 = System.currentTimeMillis();
            R result = jobExecution.getJob().start(jobContext);
            jobExecution.status = JobExecution.Status.FINISHED;
            jobResult.setStartTime(time1);
            jobResult.setResult(result);
        } catch (Exception e) {
            jobExecution.status = JobExecution.Status.FAILED;
            jobExecution.exception = e;
        }
        long time2 = System.currentTimeMillis();
        jobExecution.setResult(jobResult);
        jobResult.setEndTime(time2);

    }

    public void runAll() {
        for (JobExecution<?> jobExecution : jobs) {
            Future<?> future = executorService.submit(jobExecution);
            jobExecution.setFuture(future);
        }
    }

    public void addJob(Job<?> job) {
        jobs.add(new JobExecution<>(new JobContext(), job));
    }

    public boolean waitForAll() throws InterruptedException {
        return executorService.awaitTermination(4, TimeUnit.SECONDS);
    }

    @Data
    static
    class JobExecution<R> implements Callable<R> {
        private JobContext jobContext;
        private Job<R> job;
        private JobResult<R> result;
        private Exception exception;
        private Future<R> future;

        public void setFuture(Future<?> future) {
            this.future = (Future<R>) future;
        }

        enum Status {
            NOT_STARTED, STARTED, FINISHED, FAILED
        }

        private Status status = Status.NOT_STARTED;

        public JobExecution(JobContext jobContext, Job<R> job) {
            this.jobContext = jobContext;
            this.job = job;
            Objects.requireNonNull(job);
            Objects.requireNonNull(jobContext);
        }

        @Override
        public R call() throws Exception {
            return job.start(jobContext);
        }


    }

}
