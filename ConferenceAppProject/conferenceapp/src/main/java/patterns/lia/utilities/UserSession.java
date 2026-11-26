package patterns.lia.utilities;

import patterns.lia.exceptions.AccessDeniedException;
import patterns.lia.model.User;
import patterns.lia.model.enums.Role;

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

    //returns the current user, or throws if nobody is logged in or they have a different role
    public static User requireRole(Role role) {
        if (currentUser == null || currentUser.getRole() != role) {
            throw new AccessDeniedException();
        }
        return currentUser;
    }
}
