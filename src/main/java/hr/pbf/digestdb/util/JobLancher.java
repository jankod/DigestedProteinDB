package hr.pbf.digestdb.util;

import hr.pbf.digestdb.workflow.ExecJob;
import hr.pbf.digestdb.workflow.Job;
import hr.pbf.digestdb.workflow.JobResult;

import java.util.ArrayList;
import java.util.List;

@
public class JobLancher {
    private List<ExecJob> jobs = new ArrayList<>();

    public <R> JobResult<R> run(Job<R> workflow) throws Exception {
        try {
            long time1 = System.currentTimeMillis();
            R result = workflow.start();
            long time2 = System.currentTimeMillis();
            JobResult<R> jobResult = new JobResult<>();
            jobResult.setResult(result);
            jobResult.setStartTime(time1);
            jobResult.setEndTime(time2);

            return jobResult;
        } catch (Exception e) {
            throw e;
        }
    }


    public void addJob(ExecJob cmd) {
        jobs.add(cmd);
    }
}
