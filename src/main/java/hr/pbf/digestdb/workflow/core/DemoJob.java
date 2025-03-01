package hr.pbf.digestdb.workflow.core;

import hr.pbf.digestdb.workflow.ExeCommand;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

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

            return null;
        }
    }

    @Data
    @Slf4j
    static class SimpleJob2 implements Job<Void> {

        String param22;

        @Override
        public Void start(JobContext jobContext) throws Exception {
            log.info("Starting job SimpleJob2");
            return null;
        }
    }
}
