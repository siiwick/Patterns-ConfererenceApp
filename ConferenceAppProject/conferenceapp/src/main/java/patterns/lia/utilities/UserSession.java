package patterns.lia.utilities;

import patterns.lia.model.User;

/**
 * Holds the user who is currently logged in.
 */
public final class UserSession {

    private static User currentUser;

    private UserSession() {
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
