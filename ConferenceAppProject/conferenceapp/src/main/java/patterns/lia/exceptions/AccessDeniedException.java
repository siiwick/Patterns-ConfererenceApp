package patterns.lia.exceptions;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException() {
        super("error.access.denied");
    }
}
