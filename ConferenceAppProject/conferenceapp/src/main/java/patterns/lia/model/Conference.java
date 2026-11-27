package patterns.lia.model;
import java.time.LocalDateTime;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import patterns.lia.utilities.DateTimeUtil;

public class Conference {
    private SimpleIntegerProperty conferenceId;
    private SimpleStringProperty title;
    private int capacity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String venue;
    public Conference(int conferenceId, String title, int capacity, LocalDateTime startTime,LocalDateTime endTime, String venue) {
        this.conferenceId = new SimpleIntegerProperty(conferenceId);
        this.title = new SimpleStringProperty(title);
        this.capacity = capacity;
        this.startTime = startTime;
        this.endTime = endTime;
        this.venue = venue;
    }
    public int getConferenceId() {
        return conferenceId.get();
    }
    public String getTitle() {
        return title.get();
    }
    public int getCapacity() {
        return capacity;
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }
    public String getVenue() {
        return venue;
    }
    public void setConferenceId(int conferenceId) {
        this.conferenceId = new SimpleIntegerProperty(conferenceId);
    }
    public void setTitle(String title) {
        this.title = new SimpleStringProperty(title);
    }
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    public void setVenue(String venue) {
        this.venue = venue;
    }
    @Override
    public String toString() {
        return conferenceId+" | "+title+" | "+startTime+" | "+endTime+" | "+venue;
    }
    public boolean equals(Conference conference){
        if(conferenceId.get()!=conference.getConferenceId()) return false;
        if(!title.get().equals(conference.getTitle()))return false;
        if(capacity!=conference.getCapacity()) return false;
        if(!DateTimeUtil.parse(DateTimeUtil.format(startTime)).equals(DateTimeUtil.parse(DateTimeUtil.format(conference.getStartTime())))) return false;
        if(!DateTimeUtil.parse(DateTimeUtil.format(endTime)).equals(DateTimeUtil.parse(DateTimeUtil.format(conference.getEndTime())))) return false;
        if(!venue.equals(conference.getVenue())) return false;
        return true;
    }
}
