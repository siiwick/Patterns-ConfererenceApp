package patterns.lia.exceptions;

public class ValidationException extends RuntimeException {

    private final Object[] args;

    public ValidationException(String messageKey, Object... args) {
        super(messageKey);
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }
}
