package hr.pbf.digestdb.workflow.core;

/**
 * Job interface.
 *
 */
public interface Job {

    void run(JobContext context) throws Exception;



}
