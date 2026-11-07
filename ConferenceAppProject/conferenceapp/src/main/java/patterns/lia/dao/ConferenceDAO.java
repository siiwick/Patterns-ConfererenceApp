package patterns.lia.dao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

import patterns.lia.model.Conference;
import patterns.lia.utilities.DateTimeUtil;

public class ConferenceDAO {
    private static final String url="jdbc:sqlite:conferenceManager.db";
    public static void initialize(){
        String createTable="""
        CREATE TABLE IF NOT EXISTS conference (
            conference_id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL CHECK (length(trim(title)) > 0),
            capacity INTEGER NOT NULL CHECK (capacity > 0),
            start_datetime TEXT NOT NULL,
            end_datetime TEXT NOT NULL,
            venue TEXT,
            created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now')),
            CHECK (datetime(start_datetime) IS NOT NULL AND datetime(end_datetime) IS NOT NULL AND datetime(end_datetime) > datetime(start_datetime))
        );""";
        try(Connection connection=DriverManager.getConnection(url);
            Statement statement=connection.createStatement()){
            statement.execute(createTable);
        }catch(SQLException e){
            DBLogger.log("Failed to create conference table");
        }
    }
    public static List<Conference> getConferences(){
        String selectAll="Select * From conference";
        try(Connection connection=DriverManager.getConnection(url);
            Statement statement=connection.createStatement()){
            ResultSet results=statement.executeQuery(selectAll);
            List<Conference> conferences=new ArrayList<>();
            while(results.next()){
                conferences.add(new Conference(results.getInt("conference_id"),results.getString("title"),results.getInt("capacity"),DateTimeUtil.parse(results.getString("start_datetime")),DateTimeUtil.parse(results.getString("end_datetime")),results.getString("venue")));
            }
            return conferences;
        }catch(SQLException e){
            DBLogger.log("Failed to get all conferences");
            return null;
        }
    }
    public static Conference getConferenceById(int id){
        String selectConf="SELECT * FROM conference WHERE conference_id=?;";
        try(Connection connection=DriverManager.getConnection(url);
            PreparedStatement statement=connection.prepareStatement(selectConf)){
            statement.setInt(1, id);
            ResultSet results=statement.executeQuery();
            Conference conference=new Conference(results.getInt("conference_id"),results.getString("title"),results.getInt("capacity"),DateTimeUtil.parse(results.getString("start_datetime")),DateTimeUtil.parse(results.getString("end_datetime")),results.getString("venue"));
            return conference;
        }catch(SQLException e){
            DBLogger.log("Failed to get conference by ID");
            return null;
        }
    }
    public static void insertConference(Conference conference){
        String insertConf="INSERT INTO conference(title,capacity,start_datetime,end_datetime) VALUES(?,?,?,?)";
        try(Connection connection=DriverManager.getConnection(url);
            PreparedStatement statement=connection.prepareStatement(insertConf)){ 
            statement.setString(1,conference.getTitle());
            statement.setInt(2,conference.getCapacity());
            statement.setString(3,DateTimeUtil.format(conference.getStartTime()));
            statement.setString(4,DateTimeUtil.format(conference.getEndTime()));
            statement.executeUpdate();
        }catch(SQLException e){
            throw new RuntimeException("Failed to insert conference",e);
        }
    }
    public void delete(int id){
        String delete="DELETE FROM conference WHERE conference_id=?";
        try(Connection connection=DriverManager.getConnection(url);PreparedStatement statement=connection.prepareStatement(delete)){
            statement.setInt(1, id);
            statement.executeUpdate();
        }catch(SQLException e){
            throw new RuntimeException("Failed to delete conference",e);
        }
    }
    public void update(Conference conference){
        String delete="UPDATE conference SET title=?,capacity=?,start_datetime=?,end_datetime=?,venue=?";
        try(Connection connection=DriverManager.getConnection(url);PreparedStatement statement=connection.prepareStatement(delete)){
            statement.setString(1,conference.getTitle());
            statement.setInt(2,conference.getCapacity());
            statement.setString(3, DateTimeUtil.format(conference.getStartTime()));
            statement.setString(4, DateTimeUtil.format(conference.getEndTime()));
            statement.setString(5,conference.getVenue());
            statement.executeUpdate();
        }catch(SQLException e){
            throw new RuntimeException("Failed to delete conference",e);
        }
    }
    private class DBLogger{
        private static final Logger logger=Logger.getLogger(DBLogger.class.getName());
        private static void log(String line){
            logger.log(Level.SEVERE,line);
        }
    }
}
