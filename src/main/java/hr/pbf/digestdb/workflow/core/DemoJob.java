package hr.pbf.digestdb.workflow.core;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DemoJob {

    public static void main(String[] args) throws Exception {

        SimpleJob1 simpleJob1 = new SimpleJob1();
        simpleJob1.setParam1("param1");
        simpleJob1.setParam2(2);

        SimpleJob2 simpleJob2 = new SimpleJob2();
        simpleJob2.setParam22("param22");

        JobLancher jobLancher = new JobLancher();
        jobLancher.addJob(simpleJob1);
        jobLancher.addJob(simpleJob2);

        jobLancher.runAll();
        boolean res = jobLancher.waitForAll();
        log.debug("All jobs finished: {}", res);
    }

    @Slf4j
    @Data
    static class SimpleJob1 implements Job<Void> {

        String param1;

        int param2;

        public final String PARAM3 = "param3";

        @Override
        public Void start(JobContext jobContext) throws Exception {
            log.info("Starting job SimpleJob1");
            jobContext.setParam(PARAM3, param1);
            for (int i = 0; i < 10; i++) {
                Thread.sleep(2000);
                log.debug("Job SimpleJob1, iteration: {}", i);
            }

            return null;
        }
    }

    @Data
    @Slf4j
    static class SimpleJob2 implements Job<Void> {

        String param22;

        @Override
        public Void start(JobContext jobContext) throws Exception {
            for (int i = 0; i < 10; i++) {
                Thread.sleep(2000);
                log.debug("Job SimpleJob2, iteration: {}", i);
            }
            return null;
        }
    }
}
