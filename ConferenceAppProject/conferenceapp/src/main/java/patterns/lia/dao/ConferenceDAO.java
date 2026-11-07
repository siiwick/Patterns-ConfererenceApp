package patterns.lia.dao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

import patterns.lia.db.DatabaseManager;
import patterns.lia.model.Conference;
import patterns.lia.utilities.DateTimeUtil;

public class ConferenceDAO {
    private final DatabaseManager dbm;
    public ConferenceDAO(DatabaseManager dbm){
        this.dbm=dbm;
    }
    public List<Conference> getConferences(){
        String selectAll="Select * From conferences";
        try(Connection connection=dbm.getConnection();
            Statement statement=connection.createStatement()){
            ResultSet results=statement.executeQuery(selectAll);
            List<Conference> conferences=new ArrayList<>();
            while(results.next()){
                conferences.add(new Conference(results.getInt("conference_id"),results.getString("title"),results.getInt("capacity"),DateTimeUtil.parse(results.getString("start_datetime")),DateTimeUtil.parse(results.getString("end_datetime")),results.getString("venue")));
            }
            return conferences;
        }catch(SQLException e){
            DBLogger.log("Failed to get all conferences: "+e);
            return null;
        }
    }
    public Conference getConferenceById(int id){
        String selectConf="SELECT * FROM conferences WHERE conference_id=?;";
        try(Connection connection=dbm.getConnection();
            PreparedStatement statement=connection.prepareStatement(selectConf)){
            statement.setInt(1, id);
            ResultSet results=statement.executeQuery();
            Conference conference;
            if(results.next()) {
                conference=new Conference(results.getInt("conference_id"),results.getString("title"),results.getInt("capacity"),DateTimeUtil.parse(results.getString("start_datetime")),DateTimeUtil.parse(results.getString("end_datetime")),results.getString("venue"));
                return conference;
            }
            else return null;
        }catch(SQLException e){
            DBLogger.log("Failed to get conference by ID: "+e);
            return null;
        }
    }
    public void insertConference(Conference conference){
        String insertConf="INSERT INTO conferences(title,capacity,start_datetime,end_datetime,venue) VALUES(?,?,?,?,?)";
        try(Connection connection=dbm.getConnection();
            PreparedStatement statement=connection.prepareStatement(insertConf)){ 
            statement.setString(1,conference.getTitle());
            statement.setInt(2,conference.getCapacity());
            statement.setString(3,DateTimeUtil.format(conference.getStartTime()));
            statement.setString(4,DateTimeUtil.format(conference.getEndTime()));
            statement.setString(5,conference.getVenue());
            statement.executeUpdate();
        }catch(SQLException e){
            DBLogger.log("Failed to insert conference: "+e);
        }
    }
    public void delete(int id){
        String delete="DELETE FROM conferences WHERE conference_id=?";
        try(Connection connection=dbm.getConnection();PreparedStatement statement=connection.prepareStatement(delete)){
            statement.setInt(1, id);
            statement.executeUpdate();
        }catch(SQLException e){
            DBLogger.log("Failed to delete conference: "+e);
        }
    }
    public void update(Conference conference){
        String delete="UPDATE conferences SET title=?,capacity=?,start_datetime=?,end_datetime=?,venue=?";
        try(Connection connection=dbm.getConnection();PreparedStatement statement=connection.prepareStatement(delete)){
            statement.setString(1,conference.getTitle());
            statement.setInt(2,conference.getCapacity());
            statement.setString(3, DateTimeUtil.format(conference.getStartTime()));
            statement.setString(4, DateTimeUtil.format(conference.getEndTime()));
            statement.setString(5,conference.getVenue());
            statement.executeUpdate();
        }catch(SQLException e){
            DBLogger.log("Failed to update conference: "+e);
        }
    }
    private class DBLogger{
        private static final Logger logger=Logger.getLogger(DBLogger.class.getName());
        private static void log(String line){
            logger.log(Level.SEVERE,line);
        }
    }
}
