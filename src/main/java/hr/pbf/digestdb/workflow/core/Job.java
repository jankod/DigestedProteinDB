package hr.pbf.digestdb.workflow.core;

/**
 * Job interface.
 *
 * @param <R> result type
 */
public interface Job<R> {

    R start(JobWorkflowContext context) throws Exception;

}
