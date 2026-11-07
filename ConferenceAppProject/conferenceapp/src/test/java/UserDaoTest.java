
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import patterns.lia.dao.CredentialsDao;
import patterns.lia.dao.UserDao;
import patterns.lia.db.DatabaseManager;
import patterns.lia.exceptions.DataAccessException;
import patterns.lia.exceptions.DuplicateEmailException;
import patterns.lia.model.User;
import patterns.lia.model.enums.Role;

class UserDaoTest {

    private DatabaseManager db;
    private UserDao users;
    private CredentialsDao credentials;

    @BeforeEach
    void setUp() {
        db = TestDb.create();
        users = new UserDao(db);
        credentials = new CredentialsDao(db);
    }

    private User newParticipant(String name, String email) {
        return users.create(new User(name, email, 25, Role.PARTICIPANT), "hash" + name);
    }

    private int count(String sql) throws SQLException {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    //creation
    @Test
    void testCreateUser() {
        User saved = users.create(new User("Alice Smith", "alice@x.com", 30, Role.PARTICIPANT), "h1");

        assertTrue(saved.getId() > 0);
        assertEquals("Alice Smith", saved.getFullName());
        assertEquals("alice@x.com", saved.getEmail());
        assertEquals(30, saved.getAge());
        assertEquals(Role.PARTICIPANT, saved.getRole());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void testCredentialsCreation() throws SQLException {
        User saved = users.create(new User("Alice", "alice@x.com", 30, Role.PARTICIPANT), "secret-hash");

        assertEquals("secret-hash", credentials.findPasswordHash(saved.getId()));
        assertEquals(1, count("SELECT COUNT(*) FROM credentials"));
    }

    @Test
    void testCreateAdminUser() {
        User admin = users.create(new User("Admin", "admin@x.com", 40, Role.ADMIN), "h");
        assertTrue(users.findById(admin.getId()).isAdmin());
    }

    @Test
    void testDuplicateEmail() throws SQLException {
        newParticipant("Alice", "alice@x.com");

        assertThrows(DuplicateEmailException.class, () -> newParticipant("Other", "alice@x.com"));
        assertEquals(1, count("SELECT COUNT(*) FROM users"));
        assertEquals(1, count("SELECT COUNT(*) FROM credentials"));
    }

    @Test
    void testDuplicateUppercaseEmail() throws SQLException {
        newParticipant("Alice", "alice@x.com");

        assertThrows(DuplicateEmailException.class, () -> newParticipant("Other", "ALICE@X.COM"));
        assertEquals(1, count("SELECT COUNT(*) FROM users"));
        assertEquals(1, count("SELECT COUNT(*) FROM credentials"));
    }

    @Test
    void testBelowMinAge() throws SQLException {
        assertThrows(DataAccessException.class, () -> users.create(new User("Kid", "kid@x.com", 10, Role.PARTICIPANT), "h"));

        assertNull(users.findByEmail("kid@x.com"));
        assertEquals(0, count("SELECT COUNT(*) FROM credentials"));
    }

    @Test
    void testBlankName() {
        assertThrows(DataAccessException.class, () -> users.create(new User("   ", "blank@x.com", 30, Role.PARTICIPANT), "h"));
    }

    //finding
    @Test
    void testFindById() {
        User saved = newParticipant("Alice", "alice@x.com");
        assertEquals("alice@x.com", users.findById(saved.getId()).getEmail());
    }

    @Test
    void testUnkownId() {
        assertNull(users.findById(9999));
    }

    @Test
    void testFindByEmail() {
        User saved = newParticipant("Alice", "alice@x.com");
        assertEquals(saved.getId(), users.findByEmail("alice@x.com").getId());
    }

    @Test
    void testFindByEmailUppercase() {
        User saved = newParticipant("Alice", "alice@x.com");
        assertEquals(saved.getId(), users.findByEmail("ALICE@x.com").getId());
    }

    @Test
    void testUnkownEmail() {
        assertNull(users.findByEmail("nobody@x.com"));
    }

    @Test
    void testFindAll() {
        newParticipant("Charlie", "c@x.com");
        newParticipant("Alice", "a@x.com");
        newParticipant("Bob", "b@x.com");

        List<String> names = users.findAll().stream().map(User::getFullName).toList();
        assertEquals(List.of("Alice", "Bob", "Charlie"), names);
    }

    @Test
    void testEmptyFindAll() {
        assertTrue(users.findAll().isEmpty());
    }

    @Test
    void testFindByRole() {
        users.create(new User("Admin", "admin@x.com", 40, Role.ADMIN), "h");
        newParticipant("Alice", "a@x.com");
        newParticipant("Bob", "b@x.com");

        assertEquals(1, users.findByRole(Role.ADMIN).size());
        assertEquals(2, users.findByRole(Role.PARTICIPANT).size());
    }

    //updating
    @Test
    void testUpdateProfile() {
        User saved = newParticipant("Alice", "alice@x.com");
        saved.setFullName("Alice Updated");
        saved.setEmail("new@x.com");
        saved.setAge(41);

        assertTrue(users.update(saved));

        User reloaded = users.findById(saved.getId());
        assertEquals("Alice Updated", reloaded.getFullName());
        assertEquals("new@x.com", reloaded.getEmail());
        assertEquals(41, reloaded.getAge());
    }

    @Test
    void testCantUpdateRole() {
        User saved = newParticipant("Alice", "alice@x.com");
        saved.setRole(Role.ADMIN);

        users.update(saved);

        assertEquals(Role.PARTICIPANT, users.findById(saved.getId()).getRole());
    }

    @Test
    void testDuplicateEmailOnUpdate() {
        newParticipant("Alice", "alice@x.com");
        User bob = newParticipant("Bob", "bob@x.com");
        bob.setEmail("alice@x.com");

        assertThrows(DuplicateEmailException.class, () -> users.update(bob));
    }

    @Test
    void testUpdateInvalidAge() {
        User saved = newParticipant("Alice", "alice@x.com");
        saved.setAge(5);

        assertThrows(DataAccessException.class, () -> users.update(saved));
        assertEquals(25, users.findById(saved.getId()).getAge());
    }

    @Test
    void testUpdateUnkown() {
        User ghost = new User(9999, "Ghost", "ghost@x.com", 30, Role.PARTICIPANT, null);
        assertFalse(users.update(ghost));
    }

    //deletion
    @Test
    void testDeleteUser() throws SQLException {
        User saved = newParticipant("Alice", "alice@x.com");

        assertTrue(users.delete(saved.getId()));

        assertNull(users.findById(saved.getId()));
        assertEquals(0, count("SELECT COUNT(*) FROM credentials"));
    }

    @Test
    void testDeleteUnknown() {
        assertFalse(users.delete(9999));
    }

    @Test
    void testDeleteWhileRegistered() throws SQLException {
        User saved = newParticipant("Alice", "alice@x.com");
        try (Connection c = db.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO conferences (title, capacity, start_datetime, end_datetime) VALUES ('C', 5, '2027-01-01T09:00:00', '2027-01-01T10:00:00')")) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO registrations (user_id, conference_id, status) VALUES (?, 1, 'CONFIRMED')")) {
                ps.setLong(1, saved.getId());
                ps.executeUpdate();
            }
        }

        assertThrows(DataAccessException.class, () -> users.delete(saved.getId()));
        assertNotNull(users.findById(saved.getId()));
    }
}
