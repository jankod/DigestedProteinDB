package hr.pbf.digestdb.workflow;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    private Object data;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Object data) {
        super(message + " Data: " + data);
    }
}
