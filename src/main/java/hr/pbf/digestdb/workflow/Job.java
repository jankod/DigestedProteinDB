package hr.pbf.digestdb.workflow;

import java.io.File;

/**
 * Job interface.
 *
 * @param <R> result type
 */
public interface Job<R> {

    R start() throws Exception;

}
