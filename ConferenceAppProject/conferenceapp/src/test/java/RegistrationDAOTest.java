import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import patterns.lia.dao.ConferenceDAO;
import patterns.lia.dao.RegistrationDAO;
import patterns.lia.dao.UserDao;
import patterns.lia.db.DatabaseInitializer;
import patterns.lia.db.DatabaseManager;
import patterns.lia.model.Conference;
import patterns.lia.model.Registration;
import patterns.lia.model.User;
import patterns.lia.model.enums.RegistrationStatus;
import patterns.lia.model.enums.Role;

public class RegistrationDAOTest {
    RegistrationDAO regDao;
    ConferenceDAO confDao;
    UserDao userDao;
    @BeforeEach
    void setup() {
        DatabaseManager dbm = TestDb.create();
        DatabaseInitializer.reset(dbm);
        regDao = new RegistrationDAO(dbm);
        confDao=new ConferenceDAO(dbm);
        userDao=new UserDao(dbm);
    }
    //Insert + get all
    @Test 
    void validInsertGetAll(){
        User user=new User(1,"John Doe","johndoe@gmail.com",22,Role.PARTICIPANT,null);
        Conference conference=new Conference(1, "AnyConference", 10000, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6), "AnyVenue");
        userDao.create(user,"hashedPassword");
        confDao.insertConference(conference);
        regDao.register(user,conference);
        List<Registration> registrations=regDao.getRegistrations();
        assertTrue(registrations.get(0).getConferenceId()==conference.getConferenceId()&&registrations.get(0).getUserId()==user.getId());
    }
    @Test 
    void invalidInsertGetAll(){
        User user=new User(1,"John Doe","johndoe@gmail.com",22,Role.PARTICIPANT,null);
        Conference conference=new Conference(1, "AnyConference", 10000, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6), "AnyVenue");
        regDao.register(user,conference);
        List<Registration> registrations=regDao.getRegistrations();
        assertEquals(registrations.size(),0);
    }
    //Get all registered
    @Test
    void getAllRegisteredUsers(){
        User user=new User(1,"John Doe","johndoe@gmail.com",22,Role.PARTICIPANT,null);
        Conference conference=new Conference(1, "AnyConference", 10000, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6), "AnyVenue");
        userDao.create(user,"hashedPassword");
        confDao.insertConference(conference);
        regDao.register(user,conference);
        List<User> registeredUsers=regDao.getRegisteredUsers(conference);
        assertEquals(registeredUsers.get(0).getId(),user.getId());
        assertEquals(registeredUsers.get(0).getFullName(),user.getFullName());
    }
    @Test 
    void getAllRegisteredUsersEmpty(){
        User user=new User(1,"John Doe","johndoe@gmail.com",22,Role.PARTICIPANT,null);
        Conference conference1=new Conference(1, "AnyConference", 10000, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6), "AnyVenue");
        Conference conference2=new Conference(2, "GenericConference", 7000, LocalDateTime.now().plusDays(8), LocalDateTime.now().plusDays(10), "ConventionCenter");
        userDao.create(user,"hashedPassword");
        confDao.insertConference(conference1);
        confDao.insertConference(conference2);
        regDao.register(user,conference1);
        List<User> registeredUsers=regDao.getRegisteredUsers(conference2);
        assertEquals(registeredUsers.size(), 0);
    }
    //Get registration by id
    @Test 
    void getRegistrationByIdExists(){
        User user=new User(1,"John Doe","johndoe@gmail.com",22,Role.PARTICIPANT,null);
        Conference conference=new Conference(1, "AnyConference", 10000, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6), "AnyVenue");
        userDao.create(user,"hashedPassword");
        confDao.insertConference(conference);
        regDao.register(user,conference);
        Registration registration=regDao.getRegistrationById(1);
        assertEquals(user.getId(),registration.getUserId());
        assertEquals(conference.getConferenceId(), registration.getConferenceId());
    }
    @Test 
    void getRegistrationByIdNotExists(){
        Registration registration=regDao.getRegistrationById(1);
        assertNull(registration);
    }
    //Get conferences registered for
    @Test 
    void getAllConferencesRegistered(){
        User user=new User(1,"John Doe","johndoe@gmail.com",22,Role.PARTICIPANT,null);
        Conference conference=new Conference(1, "AnyConference", 10000, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6), "AnyVenue");
        userDao.create(user,"hashedPassword");
        confDao.insertConference(conference);
        regDao.register(user,conference);
        List<Conference> conferencesRegistered=regDao.getConferencesRegistered(user);
        assertTrue(conferencesRegistered.get(0).equals(conference));
    }
    @Test 
    void getAllConferencesRegisteredEmpty(){
        User user=new User(1,"John Doe","johndoe@gmail.com",22,Role.PARTICIPANT,null);
        Conference conference=new Conference(1, "AnyConference", 10000, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6), "AnyVenue");
        userDao.create(user,"hashedPassword");
        confDao.insertConference(conference);
        List<Conference> conferencesRegistered=regDao.getConferencesRegistered(user);
        assertEquals(conferencesRegistered.size(),0);
    }
    //Correct status
    @Test 
    void registrationConfirmed(){
        User user=new User(1,"John Doe","johndoe@gmail.com",22,Role.PARTICIPANT,null);
        Conference conference=new Conference(1, "AnyConference", 10000, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6), "AnyVenue");
        userDao.create(user,"hashedPassword");
        confDao.insertConference(conference);
        regDao.register(user,conference);
        Registration registration=regDao.getRegistrationById(1);
        assertEquals(registration.getRegistrationStatus(),RegistrationStatus.CONFIRMED);
    }
    @Test 
    void registrationWaitlisted(){
        User user1=new User(1,"John Doe","johndoe@gmail.com",22,Role.PARTICIPANT,null);
        User user2=new User(2,"Jane Doe","janedoe@gmail.com",34,Role.PARTICIPANT,null);
        Conference conference=new Conference(1, "AnyConference", 1, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6), "AnyVenue");
        userDao.create(user1,"hashedPassword");
        userDao.create(user2,"hashedPassword");
        confDao.insertConference(conference);
        regDao.register(user1,conference);
        regDao.register(user2,conference);
        Registration registration=regDao.getRegistrationById(2);
        assertEquals(registration.getRegistrationStatus(),RegistrationStatus.WAITLISTED);
        assertEquals(registration.getWaitlistPosition(),1);
    }
    //Update
    @Test 
    void updateRegistration(){
        User user1=new User(1,"John Doe","johndoe@gmail.com",22,Role.PARTICIPANT,null);
        User user2=new User(2,"Jane Doe","janedoe@gmail.com",34,Role.PARTICIPANT,null);
        Conference conference=new Conference(1, "AnyConference", 1, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6), "AnyVenue");
        userDao.create(user1,"hashedPassword");
        userDao.create(user2,"hashedPassword");
        confDao.insertConference(conference);
        regDao.register(user1,conference);
        regDao.register(user2,conference);
        Registration registration=regDao.getRegistrationById(2);
        registration.setWaitlistPosition(2);
        regDao.update(registration);
        assertTrue(regDao.getRegistrationById(registration.getId()).equals(registration));
    }
    //Delete
    @Test
    void deleteRegistration(){
        User user=new User(1,"John Doe","johndoe@gmail.com",22,Role.PARTICIPANT,null);
        Conference conference=new Conference(1, "AnyConference", 10000, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6), "AnyVenue");
        userDao.create(user,"hashedPassword");
        confDao.insertConference(conference);
        regDao.register(user,conference);
        regDao.delete(1);
        Registration registration=regDao.getRegistrationById(1);
        assertNull(registration);
    }
}
