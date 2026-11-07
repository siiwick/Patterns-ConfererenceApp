import patterns.lia.dao.ConferenceDAO;
import patterns.lia.db.DatabaseInitializer;
import patterns.lia.db.DatabaseManager;
import patterns.lia.model.Conference;
import patterns.lia.utilities.DateTimeUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;


public class ConferenceDAOInsertTest {
    ConferenceDAO dao;

    @BeforeEach 
    void setup(){
        DatabaseManager dbm=new DatabaseManager();
        DatabaseInitializer.reset(dbm);
        dao=new ConferenceDAO(dbm);
    }
    @Test 
    void testBasicInsert(){
        //necessary to convert back and fourth to get rid of the decimal
        Conference conference=new Conference(1, "AnyConference", 10000, DateTimeUtil.parse(DateTimeUtil.format(LocalDateTime.now().plusDays(3))), DateTimeUtil.parse(DateTimeUtil.format(LocalDateTime.now().plusDays(6))), "AnyVenue");
        dao.insertConference(conference);
        List<Conference> conferences=dao.getConferences();
        assertTrue(conference.equals(conferences.get(0)));
    }
    
}
