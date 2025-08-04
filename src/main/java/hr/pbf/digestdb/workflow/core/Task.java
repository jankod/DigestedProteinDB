package hr.pbf.digestdb.workflow.core;

/**
 * Task interface for defining a unit of work in a workflow.
 * @param <I> the type of input to the task
 * @param <O> the type of output produced by the task
 */
public interface Task<I,O> {

    O start(JobContext context, I input) throws Exception;

}
