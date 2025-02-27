package hr.pbf.digestdb.util;

import hr.pbf.digestdb.workflow.Job;
import hr.pbf.digestdb.workflow.JobResult;

public class JobLancher {

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


}
