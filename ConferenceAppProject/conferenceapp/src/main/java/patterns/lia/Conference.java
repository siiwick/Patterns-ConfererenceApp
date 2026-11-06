package patterns.lia;

public class Conference {
    private int conferenceId;
    private String title;
    private int capacity;
    private String startTime;
    private String endTime;
    private String venue;
    public Conference(int conferenceId, String title, int capacity, String startTime, String endTime, String venue) {
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
    public String getStartTime() {
        return startTime;
    }
    public String getEndTime() {
        return endTime;
    }
    public String getVenue() {
        return venue;
    }
}
