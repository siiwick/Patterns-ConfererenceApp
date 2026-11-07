
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import patterns.lia.dao.CredentialsDao;
import patterns.lia.dao.UserDao;
import patterns.lia.db.DatabaseManager;
import patterns.lia.model.User;
import patterns.lia.model.enums.Role;

class CredentialsDaoTest {

    private UserDao users;
    private CredentialsDao credentials;

    @BeforeEach
    void setUp() {
        DatabaseManager db = TestDb.create();
        users = new UserDao(db);
        credentials = new CredentialsDao(db);
    }

    private User userWithHash(String email, String hash) {
        return users.create(new User("Name", email, 30, Role.PARTICIPANT), hash);
    }

    //finding
    @Test
    void testFindHash() {
        User u = userWithHash("a@x.com", "hash-A");
        assertEquals("hash-A", credentials.findPasswordHash(u.getId()));
    }

    @Test
    void testUniqueHash() {
        User a = userWithHash("a@x.com", "hash-A");
        User b = userWithHash("b@x.com", "hash-B");

        assertEquals("hash-A", credentials.findPasswordHash(a.getId()));
        assertEquals("hash-B", credentials.findPasswordHash(b.getId()));
    }

    @Test
    void testUnkownUser() {
        assertNull(credentials.findPasswordHash(9999));
    }

    //updating
    @Test
    void testUpdateHash() {
        User u = userWithHash("a@x.com", "old-hash");

        assertTrue(credentials.updatePasswordHash(u.getId(), "new-hash"));

        assertEquals("new-hash", credentials.findPasswordHash(u.getId()));
    }

    @Test
    void testUpdateHashUnkown() {
        assertFalse(credentials.updatePasswordHash(9999, "whatever"));
    }
}
