package patterns.lia.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import patterns.lia.db.DatabaseManager;
import patterns.lia.exceptions.DataAccessException;
import patterns.lia.exceptions.DuplicateEmailException;
import patterns.lia.model.Role;
import patterns.lia.model.User;
import patterns.lia.utilities.DateTimeUtil;

/**
 * Data access for the {@code users} (profile) and {@code credentials} tables.
 * Only SQL lives here: validation and hashing are done by the service layer.
 */
public class UserDao {

    private static final Logger LOG = Logger.getLogger(UserDao.class.getName());

    private final DatabaseManager db;

    public UserDao(DatabaseManager db) {
        this.db = db;
    }

    //create user methods, inserts into users and credentials table at once
    public User create(User user, String passwordHash) {
        try (Connection connection = db.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long id = insertUser(connection, user);
                insertCredentials(connection, id, passwordHash);
                connection.commit();
                return findById(id);
            } catch (SQLException | RuntimeException e) {
                rollback(connection);
                throw e;
            }
        } catch (SQLException e) {
            if (isDuplicateEmail(e)) {
                throw new DuplicateEmailException();
            }
            throw failure("create user", e);
        }
    }

    private static long insertUser(Connection connection, User user) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, age, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.fullName());
            ps.setString(2, user.email());
            ps.setInt(3, user.age());
            ps.setString(4, user.role().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No generated key returned for new user");
                }
                return keys.getLong(1);
            }
        }
    }

    private static void insertCredentials(Connection connection, long userId, String passwordHash) throws SQLException {
        String sql = "INSERT INTO credentials (user_id, password_hash) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, passwordHash);
            ps.executeUpdate();
        }
    }

    //find-by methods
    public User findById(long id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection connection = db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeQuery();
            try (ResultSet rs = ps.getResultSet()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw failure("find user by id", e);
        }
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection connection = db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeQuery();
            try (ResultSet rs = ps.getResultSet()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw failure("find user by email", e);
        }
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY full_name, user_id";
        try (Connection connection = db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return readAll(rs);
        } catch (SQLException e) {
            throw failure("list users", e);
        }
    }

    public List<User> findByRole(Role role) {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY full_name, user_id";
        try (Connection connection = db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                return readAll(rs);
            }
        } catch (SQLException e) {
            throw failure("list users by role", e);
        }
    }

    //updates user info, role isnt updatable for security
    public boolean update(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, age = ? WHERE user_id = ?";
        try (Connection connection = db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.fullName());
            ps.setString(2, user.email());
            ps.setInt(3, user.age());
            ps.setLong(4, user.id());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (isDuplicateEmail(e)) {
                throw new DuplicateEmailException();
            }
            throw failure("update user", e);
        }
    }

    //deletes a user and its credentials with cascade on delete. Will fail if the user has registrations
    public boolean delete(long userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection connection = db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw failure("delete user", e);
        }
    }

    //helpers
    private static List<User> readAll(ResultSet rs) throws SQLException {
        List<User> users = new ArrayList<>();
        while (rs.next()) {
            users.add(map(rs));
        }
        return users;
    }

    private static User map(ResultSet rs) throws SQLException {
        User u = new User(rs.getLong("user_id"), rs.getString("full_name"), rs.getString("email"), rs.getInt("age"), Role.valueOf(rs.getString("role")), DateTimeUtil.parse(rs.getString("created_at")));
        return u;
    }

    private static boolean isDuplicateEmail(SQLException e) {
        String message = e.getMessage();
        return message != null && message.contains("UNIQUE") && message.contains("users.email");
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            LOG.log(Level.SEVERE, "Rollback failed", rollbackError);
        }
    }

    private static DataAccessException failure(String operation, SQLException e) {
        LOG.log(Level.SEVERE, "Database error while trying to " + operation, e);
        return new DataAccessException(e);
    }
}
