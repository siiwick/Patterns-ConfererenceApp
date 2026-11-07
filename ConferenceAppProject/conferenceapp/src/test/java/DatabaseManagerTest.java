
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;

import patterns.lia.db.DatabaseManager;

class DatabaseManagerTest {

    @Test
    void testConnection() throws SQLException {
        DatabaseManager db = TestDb.create();
        try (Connection c = db.getConnection()) {
            assertFalse(c.isClosed());
        }
    }

    @Test
    void testIndependentConnections() throws SQLException {
        DatabaseManager db = TestDb.create();
        try (Connection a = db.getConnection(); Connection b = db.getConnection()) {
            assertNotSame(a, b);
        }
    }

    @Test
    void testDefaultUrl() {
        assertEquals("jdbc:sqlite:conference.db", new DatabaseManager().getUrl());
    }

    @Test
    void testSingleton() {
        assertSame(DatabaseManager.getDefault(), DatabaseManager.getDefault());
    }
}
