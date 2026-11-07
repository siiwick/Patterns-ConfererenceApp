package patterns.lia.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.sqlite.SQLiteConfig;

/**
 * The single place that knows the database URL and opens connections.
 * Foreign-keys are enforced for each connection.
 */
public final class DatabaseManager {

    private static final String DEFAULT_URL = "jdbc:sqlite:conference.db";

    private static DatabaseManager defaultInstance;

    private final String url;
    private final Properties properties;

    //Normal app uses the real database file.
    public DatabaseManager() {
        this(DEFAULT_URL);
    }

    //Tests pass any url so that they can use a temporary db.
    public DatabaseManager(String url) {
        this.url = url;
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        config.setBusyTimeout(5000);
        this.properties = config.toProperties();
    }

    public static synchronized DatabaseManager getDefault() {
        if (defaultInstance == null) {
            defaultInstance = new DatabaseManager();
        }
        return defaultInstance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, properties);
    }

    public String getUrl() {
        return url;
    }
}
