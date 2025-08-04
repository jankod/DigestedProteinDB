package hr.pbf.digestdb.workflow.core;

import lombok.Data;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

@Data
public class JobExecution<R> implements Callable<R> {
    private JobContext jobContext;
    private Job job;
    private JobResult<R> result;
    private Exception exception;
    private Future<R> future;
    private Status status = Status.NOT_STARTED;

    long timeStart;
    long timeEnd;

    enum Status {
        NOT_STARTED, STARTED, FINISHED, FAILED
    }

    public JobExecution(JobContext context, Job job) {
        Objects.requireNonNull(job);
        Objects.requireNonNull(context);
        this.jobContext = context;
        this.job = job;
    }

    @Override
    public R call() {
        try {
            status = JobExecution.Status.STARTED;
            timeStart = System.currentTimeMillis();
//            JobResult<R> r = getJob().run(jobContext);
//            status = JobExecution.Status.FINISHED;
//            jobResult.setStartTime(timeStart);
//            jobResult.setResult(result);
        } catch (Exception e) {
            status = JobExecution.Status.FAILED;
            exception = e;
        }
//        setResult(jobResult);
        return result.getResult();
    }

    public void setFuture(Future<?> future) {
        this.future = (Future<R>) future;
    }
}
