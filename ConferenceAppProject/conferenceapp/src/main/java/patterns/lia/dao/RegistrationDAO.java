package patterns.lia.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import patterns.lia.db.DatabaseManager;
import patterns.lia.model.Conference;
import patterns.lia.model.Registration;
import patterns.lia.model.User;
import patterns.lia.model.enums.RegistrationStatus;
import patterns.lia.model.enums.Role;
import patterns.lia.utilities.DateTimeUtil;

public class RegistrationDAO {
    private final DatabaseManager dbm;
    public RegistrationDAO(DatabaseManager dbm){
        this.dbm=dbm;
    }
    public List<Registration> getRegistrations(){
        String selectAll = "Select * From registrations";
        try (Connection connection = dbm.getConnection(); Statement statement = connection.createStatement()) {
            ResultSet results = statement.executeQuery(selectAll);
            List<Registration> registrations = new ArrayList<>();
            while (results.next()) {
                registrations.add(new Registration(results.getInt("registration_id"), results.getInt("conference_id"),results.getInt("user_id"),RegistrationStatus.valueOf(results.getString("status")),results.getInt("waitlist_position")));
            }
            return registrations;
        } catch (SQLException e) {
            DBLogger.log("Failed to get all registrations: " + e);
            return null;
        }
    }
    public void register(User user,Conference conference){
        String insertConf = "INSERT INTO registrations(user_id,conference_id,status,waitlist_position) VALUES(?,?,?,?)";
        try (Connection connection = dbm.getConnection(); PreparedStatement statement = connection.prepareStatement(insertConf)) {
            statement.setLong(1,user.getId());
            statement.setLong(2,conference.getConferenceId());
            if(registrationFull(conference)){
                statement.setString(3,"WAITLISTED");
                statement.setInt(4,countRegistrations(conference));
            }
            else{
                statement.setString(3,"CONFIRMED");
                statement.setNull(4,Types.INTEGER);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            DBLogger.log("Failed to insert registration: " + e);
        }
    }
    public Registration getRegistrationById(long id){
        String selectConf = "SELECT * FROM registrations WHERE registration_id=?;";
        try (Connection connection = dbm.getConnection(); PreparedStatement statement = connection.prepareStatement(selectConf)) {
            statement.setLong(1, id);
            ResultSet results=statement.executeQuery();
            Registration registration;
            if(results.next()) {
                registration=new Registration(results.getInt("registration_id"), results.getInt("conference_id"),results.getInt("user_id"),RegistrationStatus.valueOf(results.getString("status")),results.getInt("waitlist_position"));
                return registration;
            }
            else return null;
        }catch(SQLException e){
            DBLogger.log("Failed to get registration by ID: "+e);
            return null;
        }
    }
    public List<User> getRegisteredUsers(Conference conference){
        String getRegistered="""
                                SELECT * FROM users
                                WHERE user_id IN (
                                        SELECT user_id FROM registrations WHERE conference_id=?)""";
        try (Connection connection = dbm.getConnection(); PreparedStatement statement = connection.prepareStatement(getRegistered)) {
            List<User> registeredUsers=new ArrayList<>();
            statement.setInt(1,conference.getConferenceId());
            ResultSet results=statement.executeQuery();
            while(results.next()){
                registeredUsers.add(new User(results.getLong("user_id"),results.getString("full_name"),results.getString("email"),results.getInt("age"), Role.valueOf(results.getString("role")), DateTimeUtil.parse(results.getString("created_at"))));
            }
            return registeredUsers;
        }catch (SQLException e) {
            DBLogger.log("Failed to get registered users: " + e);
            return null;
        }
    }
    public List<Conference> getConferencesRegistered(User user){
        String getRegistered="""
                                SELECT * FROM conferences
                                WHERE conference_id IN (
                                        SELECT conference_id FROM registrations WHERE user_id=?)""";
        try (Connection connection = dbm.getConnection(); PreparedStatement statement = connection.prepareStatement(getRegistered)) {
            List<Conference> conferencesRegistered=new ArrayList<>();
            statement.setLong(1,user.getId());
            ResultSet results=statement.executeQuery();
            while(results.next()){
                conferencesRegistered.add(new Conference(results.getInt("conference_id"),results.getString("title"),results.getInt("capacity"),DateTimeUtil.parse(results.getString("start_datetime")),DateTimeUtil.parse(results.getString("end_datetime")),results.getString("venue")));
            }
            return conferencesRegistered;
        }catch (SQLException e) {
            DBLogger.log("Failed to get registered users: " + e);
            return null;
        }
    }
    public void update(Registration registration){
        String delete = "UPDATE registrations SET waitlist_position=?,status=? WHERE registration_id=?";
        try (Connection connection = dbm.getConnection(); PreparedStatement statement = connection.prepareStatement(delete)) {
            if(registration.getRegistrationStatus()==RegistrationStatus.CONFIRMED) statement.setNull(1,Types.INTEGER);
            else statement.setInt(1,registration.getWaitlistPosition());
            statement.setString(2,registration.getRegistrationStatus().name());
            statement.setLong(3,registration.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            DBLogger.log("Failed to update registration: " + e);
        }
    }
    public void delete(int id) {
        String delete = "DELETE FROM registrations WHERE registration_id=?";
        try (Connection connection = dbm.getConnection(); PreparedStatement statement = connection.prepareStatement(delete)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            DBLogger.log("Failed to delete registration: " + e);
        }
    }

    private boolean registrationFull(Conference conference){
        String selectCapacity = "SELECT capacity FROM conferences WHERE conference_id=?;";
        String selectAttendance="SELECT COUNT(*) FROM registrations WHERE conference_id=?";
        try (Connection connection = dbm.getConnection(); PreparedStatement statement = connection.prepareStatement(selectCapacity);PreparedStatement attendenceStatement=connection.prepareStatement(selectAttendance);){
            statement.setInt(1,conference.getConferenceId());
            ResultSet results=statement.executeQuery();
            if(results.next()) {
                int capacity=results.getInt("capacity");
                attendenceStatement.setInt(1,conference.getConferenceId());
                ResultSet count=attendenceStatement.executeQuery();
                int attendence=count.getInt(1);
                if(attendence<capacity) return false;
                return true;
            }
            else return true;
        }catch(SQLException e){
            DBLogger.log("Failed to get conference by ID: "+e);
            return true;
        }
    }
    private int countRegistrations(Conference conference){
        String selectAttendance="SELECT COUNT(*) FROM registrations WHERE conference_id=?";
        try (Connection connection = dbm.getConnection(); PreparedStatement statement = connection.prepareStatement(selectAttendance)){
            statement.setInt(1,conference.getConferenceId());
            ResultSet count=statement.executeQuery();
            return count.getInt(1);
        }catch(SQLException e){
            DBLogger.log("Failed to get conference by ID: "+e);
            return 0;
        }
    }
    private class DBLogger {

        private static final Logger logger = Logger.getLogger(DBLogger.class.getName());

        private static void log(String line) {
            logger.log(Level.SEVERE, line);
        }
    }
}