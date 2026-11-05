package patterns.lia.exceptions;

public class DataAccessException extends RuntimeException {

    public DataAccessException(Throwable cause) {
        super("error.database", cause);
    }
}
