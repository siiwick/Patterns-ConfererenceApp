package patterns.lia.service;

import patterns.lia.dao.CredentialsDao;
import patterns.lia.dao.UserDao;
import patterns.lia.db.DatabaseManager;
import patterns.lia.exceptions.AccessDeniedException;
import patterns.lia.exceptions.ValidationException;
import patterns.lia.model.User;
import patterns.lia.utilities.PasswordHasher;
import patterns.lia.utilities.UserSession;

/**
 * Profile for the logged-in user: updating their info and changing their
 * password.
 */
public class UserService {

    public static final int MIN_AGE = 16;
    public static final int MAX_AGE = 120;
    public static final int MIN_PASSWORD_LENGTH = 6;

    private static final String EMAIL_PATTERN = "[^@\\s]+@[^@\\s]+\\.[^@\\s]+";

    private final UserDao users;
    private final CredentialsDao credentials;

    public UserService(DatabaseManager db) {
        this.users = new UserDao(db);
        this.credentials = new CredentialsDao(db);
    }

    //saves the new name, email and age. then returns the updated user
    //throws ValidationException, DuplicateEmailException or DataAccessException
    public User updateProfile(String fullName, String email, Integer age) {
        User current = requireLoggedIn();
        String name = fullName == null ? "" : fullName.trim();
        String mail = email == null ? "" : email.trim();

        if (name.isEmpty() || mail.isEmpty()) {
            throw new ValidationException("validation.required");
        }
        if (age == null || age < MIN_AGE || age > MAX_AGE) {
            throw new ValidationException("validation.age.range", MIN_AGE, MAX_AGE);
        }
        if (!mail.matches(EMAIL_PATTERN)) {
            throw new ValidationException("validation.email.invalid");
        }

        User changed = new User(current.getId(), name, mail, age, current.getRole(), current.getCreatedAt());
        users.update(changed);

        UserSession.login(changed);
        return changed;
    }

    public void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        User current = requireLoggedIn();

        if (isEmpty(currentPassword) || isEmpty(newPassword) || isEmpty(confirmPassword)) {
            throw new ValidationException("validation.required");
        }
        if (!PasswordHasher.verify(currentPassword, credentials.findPasswordHash(current.getId()))) {
            throw new ValidationException("validation.password.current");
        }
        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("validation.password.short", MIN_PASSWORD_LENGTH);
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("validation.password.mismatch");
        }

        credentials.updatePasswordHash(current.getId(), PasswordHasher.hash(newPassword));
    }

    private static User requireLoggedIn() {
        User current = UserSession.getCurrentUser();
        if (current == null) {
            throw new AccessDeniedException();
        }
        return current;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
