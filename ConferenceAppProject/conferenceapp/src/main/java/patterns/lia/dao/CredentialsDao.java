package patterns.lia.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import patterns.lia.db.DatabaseManager;
import patterns.lia.exceptions.DataAccessException;

/**
 * Data access for the credentials table.
 */
public class CredentialsDao {

    private static final Logger LOG = Logger.getLogger(CredentialsDao.class.getName());

    private final DatabaseManager db;

    public CredentialsDao(DatabaseManager db) {
        this.db = db;
    }

    //returns users passwd hash
    public String findPasswordHash(long userId) {
        String sql = "SELECT password_hash FROM credentials WHERE user_id = ?";
        try (Connection connection = db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString("password_hash");
            }
        } catch (SQLException e) {
            throw failure("read credentials", e);
        }
    }

    //returns true if the table was updated
    public boolean updatePasswordHash(long userId, String passwordHash) {
        String sql = "UPDATE credentials SET password_hash = ?, updated_at = strftime('%Y-%m-%dT%H:%M:%S', 'now') WHERE user_id = ?";
        try (Connection connection = db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw failure("update password", e);
        }
    }

    private static DataAccessException failure(String operation, SQLException e) {
        LOG.log(Level.SEVERE, "Database error while trying to " + operation, e);
        return new DataAccessException(e);
    }
}
