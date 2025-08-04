package hr.pbf.digestdb.job;

import hr.pbf.digestdb.workflow.core.Job;
import hr.pbf.digestdb.workflow.core.JobContext;
import hr.pbf.digestdb.workflow.core.JobExecution;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DemoJob {

    public static void main(String[] args) throws Exception {
        SimpleJobParameters jobParameters = new SimpleJobParameters();

        SimpleJob1 simpleJob1 = new SimpleJob1(jobParameters);

    }

    @Slf4j
    @Data
    @RequiredArgsConstructor
    static class SimpleJob1 implements Job {

        private final SimpleJobParameters parameters;

        @Override
        public void run(JobContext context) throws Exception {
            log.info("Starting job SimpleJob1");

        }
    }

    @Data
    static class SimpleJobParameters {
        String param1;
        int param2;
    }


}
