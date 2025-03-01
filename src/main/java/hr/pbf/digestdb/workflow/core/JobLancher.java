package hr.pbf.digestdb.workflow.core;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Data
@Slf4j
public class JobLancher {

    private List<JobExecution<?>> jobs = new ArrayList<>();


    private ScheduledExecutorService executorService;

    public JobLancher() {
        executorService = Executors.newScheduledThreadPool(5);
    }

    private <R> JobResult<R> run(JobExecution<R> job) throws Exception {
        JobContext jobContext = new JobContext();

        Exception jobException = null;
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

        return jobResult;
    }

    public void runAll() throws Exception {

        for (JobExecution<?> job : jobs) {
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
