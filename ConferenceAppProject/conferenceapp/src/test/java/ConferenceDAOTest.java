
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import patterns.lia.dao.ConferenceDAO;
import patterns.lia.db.DatabaseInitializer;
import patterns.lia.db.DatabaseManager;
import patterns.lia.model.Conference;

public class ConferenceDAOTest {

    ConferenceDAO dao;

    @BeforeEach
    void setup() {
        DatabaseManager dbm = TestDb.create();
        DatabaseInitializer.reset(dbm);
        dao = new ConferenceDAO(dbm);
    }

    @Test
    void testBasicInsert() {
        //necessary to convert back and fourth to get rid of the decimal
        Conference conference = new Conference(1, "AnyConference", 10000, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6), "AnyVenue");
        dao.insertConference(conference);
        List<Conference> conferences = dao.getConferences();
        assertTrue(conference.equals(conferences.get(0)));
    }

    @Test
    void testNullInsert() {
        Conference conference = new Conference(0, null, 0, null, null, null);
        dao.insertConference(conference);
        List<Conference> conferences = dao.getConferences();
        assertEquals(conferences.size(), 0);
    }
}
