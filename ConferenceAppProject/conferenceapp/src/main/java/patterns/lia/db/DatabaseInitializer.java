package patterns.lia.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import patterns.lia.exceptions.DataAccessException;

/**
 * Creates the database.
 */
public final class DatabaseInitializer {

    private static final Logger LOG = Logger.getLogger(DatabaseInitializer.class.getName());

    private DatabaseInitializer() {
    }

    //Creates the database only if the schema does not exist yet.
    public static void initializeIfMissing(DatabaseManager db) {
        if (!tableExists(db, "users")) {
            reset(db);
        }
    }

    static boolean tableExists(DatabaseManager db, String table) {
        String sql = "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?";
        try (Connection connection = db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Failed to inspect the database", e);
            throw new DataAccessException(e);
        }
    }

    public static void reset(DatabaseManager db) {

        try (Connection connection = db.getConnection(); Statement statement = connection.createStatement()) {

            //enable foreign keys
            statement.execute("PRAGMA foreign_keys = ON");

            //drop existing triggers
            statement.execute("DROP TRIGGER IF EXISTS trg_registration_capacity_insert");
            statement.execute("DROP TRIGGER IF EXISTS trg_registration_capacity_update");
            statement.execute("DROP TRIGGER IF EXISTS trg_capacity_reduction");

            //drop existing tables
            statement.execute("DROP TABLE IF EXISTS registrations");
            statement.execute("DROP TABLE IF EXISTS credentials");
            statement.execute("DROP TABLE IF EXISTS conferences");
            statement.execute("DROP TABLE IF EXISTS users");

            //create users
            statement.execute("""
                CREATE TABLE users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL CHECK (length(trim(full_name)) > 0),
                    email TEXT NOT NULL COLLATE NOCASE UNIQUE,
                    age INTEGER NOT NULL CHECK (age BETWEEN 16 AND 120),
                    role TEXT NOT NULL DEFAULT 'PARTICIPANT' CHECK (role IN ('ADMIN', 'PARTICIPANT')),
                    created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now'))
                )
                """);

            //create credentials
            statement.execute("""
                CREATE TABLE credentials (
                    user_id INTEGER PRIMARY KEY,
                    password_hash TEXT NOT NULL,
                    updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now')),
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                )
                """);

            //create conferences
            statement.execute("""
                CREATE TABLE conferences (
                    conference_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL CHECK (length(trim(title)) > 0),
                    capacity INTEGER NOT NULL CHECK (capacity > 0),
                    start_datetime TEXT NOT NULL,
                    end_datetime TEXT NOT NULL,
                    venue TEXT,
                    created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now')),
                    CHECK (datetime(start_datetime) IS NOT NULL AND datetime(end_datetime) IS NOT NULL AND datetime(end_datetime) > datetime(start_datetime))
                )
                """);

            //create registrations
            statement.execute("""
                CREATE TABLE registrations (
                    registration_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    conference_id INTEGER NOT NULL,
                    status TEXT NOT NULL CHECK (status IN ('CONFIRMED', 'WAITLISTED')),
                    waitlist_position INTEGER,
                    registered_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now')),
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT,
                    FOREIGN KEY (conference_id) REFERENCES conferences(conference_id) ON DELETE RESTRICT,
                    UNIQUE (user_id, conference_id),
                    UNIQUE (conference_id, waitlist_position),
                    CHECK ((status = 'CONFIRMED' AND waitlist_position IS NULL) OR (status = 'WAITLISTED' AND waitlist_position IS NOT NULL AND waitlist_position > 0))
                )
                """);

            //create indexes
            statement.execute("""
                CREATE INDEX idx_registrations_queue
                ON registrations (conference_id, status, waitlist_position)
                """);

            statement.execute("""
                CREATE INDEX idx_registrations_user
                ON registrations (user_id)
                """);

            //create registration capacity trigger
            statement.execute("""
                CREATE TRIGGER trg_registration_capacity_insert
                BEFORE INSERT ON registrations
                WHEN NEW.status = 'CONFIRMED'
                BEGIN
                    SELECT RAISE(ABORT, 'Conference capacity exceeded')
                    WHERE (
                        SELECT COUNT(*)
                        FROM registrations
                        WHERE conference_id = NEW.conference_id
                        AND status = 'CONFIRMED'
                    ) >= (
                        SELECT capacity
                        FROM conferences
                        WHERE conference_id = NEW.conference_id
                    );
                END
                """);

            //create registration update trigger
            statement.execute("""
                CREATE TRIGGER trg_registration_capacity_update
                BEFORE UPDATE OF status ON registrations
                WHEN NEW.status = 'CONFIRMED'
                    AND OLD.status != 'CONFIRMED'
                BEGIN
                    SELECT RAISE(ABORT, 'Conference capacity exceeded')
                    WHERE (
                        SELECT COUNT(*)
                        FROM registrations
                        WHERE conference_id = NEW.conference_id
                        AND status = 'CONFIRMED'
                    ) >= (
                        SELECT capacity
                        FROM conferences
                        WHERE conference_id = NEW.conference_id
                    );
                END
                """);

            //create capacity reduction trigger
            statement.execute("""
                CREATE TRIGGER trg_capacity_reduction
                BEFORE UPDATE OF capacity ON conferences
                WHEN NEW.capacity < (
                    SELECT COUNT(*)
                    FROM registrations
                    WHERE conference_id = NEW.conference_id
                    AND status = 'CONFIRMED'
                )
                BEGIN
                    SELECT RAISE(ABORT, 'Capacity cannot be lower than the number of confirmed participants');
                END
                """);

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Failed to initialize database", e);
            throw new DataAccessException(e);
        }
    }
}
