
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
import static org.junit.jupiter.api.Assertions.assertNull;


public class ConferenceDAOTest {

    ConferenceDAO dao;

    @BeforeEach
    void setup() {
        DatabaseManager dbm = TestDb.create();
        DatabaseInitializer.reset(dbm);
        dao = new ConferenceDAO(dbm);
    }
    //Insert + get all
    @Test 
    void testBasicInsertGet(){
        Conference conference=new Conference(1, "AnyConference", 10000, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6), "AnyVenue");
        dao.insertConference(conference);
        List<Conference> conferences = dao.getConferences();
        assertTrue(conference.equals(conferences.get(0)));
    }
    @Test 
    void testNullInsertGet(){
        Conference conference=new Conference(0, null, 0, null, null, null);
        dao.insertConference(conference);
        List<Conference> conferences = dao.getConferences();
        assertEquals(conferences.size(), 0);
    }
    //Inser + get By ID
    @Test 
    void testGetByIdExists(){
        Conference conference=new Conference(1, "AnyConference", 10000, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6), "AnyVenue");
        dao.insertConference(conference);
        Conference getConference=dao.getConferenceById(1);
        assertTrue(conference.equals(getConference));
    }
    @Test 
    void testGetByIdNotExists(){
        Conference conference=new Conference(1, "AnyConference", 10000, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6), "AnyVenue");
        dao.insertConference(conference);
        Conference getConference=dao.getConferenceById(2);
        assertNull(getConference);
    }
    //Delete
    @Test 
    void testDeleteExistsGetById(){
        Conference conference=new Conference(1, "AnyConference", 10000, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6), "AnyVenue");
        dao.insertConference(conference);
        dao.delete(1);
        Conference getConference=dao.getConferenceById(1);
        assertNull(getConference);
    }
    @Test 
    void testDeleteExistsGetAll(){
        Conference[] conferences={new Conference(1, "AnyConference", 10000, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6), "AnyVenue"),new Conference(2, "GenericConference", 7000, LocalDateTime.now().plusDays(8), LocalDateTime.now().plusDays(10), "ConventionCenter"),new Conference(3, "Convention Number Three", 9000, LocalDateTime.now().plusWeeks(4), LocalDateTime.now().plusWeeks(4).plusDays(2), "City Convention Center")};
        for(Conference conference:conferences){dao.insertConference(conference);}
        dao.delete(2);
        List<Conference> getConferences=dao.getConferences();
        Boolean isDeleted=true;
        for(Conference conference:getConferences){if(conference.equals(conferences[1])) isDeleted=false;}
        assertTrue(isDeleted);;
    }
    //Update 
    @Test 
    void testUpdateExists(){
        Conference conference=new Conference(1, "AnyConference", 10000, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6), "AnyVenue");
        dao.insertConference(conference);
        conference.setTitle("Not just Any Conference");
        conference.setCapacity(20000);
        dao.update(conference);
        Conference getConference=dao.getConferenceById(1);
        assertTrue(conference.equals(getConference));
    }
}
