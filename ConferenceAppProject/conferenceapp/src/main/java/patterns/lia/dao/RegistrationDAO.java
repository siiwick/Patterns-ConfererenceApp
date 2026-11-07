package patterns.lia.dao;

public class RegistrationDAO {
    private static final String url="jdbc:sqlite:conferenceManager.db";
    public static void initialize(){
        String createTable="""
                            CREATE TABLE registrations (
                            registration_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            user_id INTEGER NOT NULL,
                            conference_id INTEGER NOT NULL,
                            status TEXT NOT NULL CHECK (status IN ('CONFIRMED', 'WAITLISTED')),
                            waitlist_position INTEGER,
                            registered_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now')),
                            FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT,
                            FOREIGN KEY (conference_id) REFERENCES conferences (conference_id) ON DELETE RESTRICT,
                            UNIQUE (user_id, conference_id),
                            UNIQUE (conference_id, waitlist_position),
                            CHECK ((status = 'CONFIRMED'  AND waitlist_position IS NULL) OR (status = 'WAITLISTED' AND waitlist_position IS NOT NULL AND waitlist_position > 0))
                        );
                            """;
    }
}
