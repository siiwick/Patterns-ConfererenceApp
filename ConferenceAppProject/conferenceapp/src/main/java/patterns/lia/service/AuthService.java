package patterns.lia.service;

import patterns.lia.dao.CredentialsDao;
import patterns.lia.dao.UserDao;
import patterns.lia.db.DatabaseManager;
import patterns.lia.model.User;
import patterns.lia.utilities.PasswordHasher;

/**
 * Checks login credentials against the database.
 */
public class AuthService {

    private final UserDao users;
    private final CredentialsDao credentials;

    public AuthService(DatabaseManager db) {
        this(new UserDao(db), new CredentialsDao(db));
    }

    public AuthService(UserDao users, CredentialsDao credentials) {
        this.users = users;
        this.credentials = credentials;
    }

    //returns the user if the email and password match, otherwise null
    public User login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            return null;
        }
        User user = users.findByEmail(email.trim());
        if (user == null) {
            return null;
        }
        String storedHash = credentials.findPasswordHash(user.getId());
        return PasswordHasher.verify(password, storedHash) ? user : null;
    }
}
