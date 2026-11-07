
import patterns.lia.db.DatabaseInitializer;
import patterns.lia.db.DatabaseManager;

/**
 * Makes a test database.
 */
public final class TestDb {

    private TestDb() {
    }

    public static DatabaseManager create() {
        DatabaseManager db = new DatabaseManager("jdbc:sqlite:test.db");
        DatabaseInitializer.reset(db);
        return db;
    }
}
