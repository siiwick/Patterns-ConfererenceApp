package patterns.lia.model;
import java.time.LocalDateTime;

public class Conference {
    private int conferenceId;
    private String title;
    private int capacity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String venue;
    public Conference(int conferenceId, String title, int capacity, LocalDateTime startTime,LocalDateTime endTime, String venue) {
        this.conferenceId = conferenceId;
        this.title = title;
        this.capacity = capacity;
        this.startTime = startTime;
        this.endTime = endTime;
        this.venue = venue;
    }
    public int getConferenceId() {
        return conferenceId;
    }
    public String getTitle() {
        return title;
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
        this.conferenceId = conferenceId;
    }
    public void setTitle(String title) {
        this.title = title;
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
        if(conferenceId!=conference.getConferenceId()) return false;
        if(!title.equals(conference.getTitle()))return false;
        if(capacity!=conference.getCapacity()) return false;
        return true;
    }
}
