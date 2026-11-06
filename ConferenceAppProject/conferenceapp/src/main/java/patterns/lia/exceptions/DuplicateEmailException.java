package patterns.lia.exceptions;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {
        super("validation.email.duplicate");
    }
}
