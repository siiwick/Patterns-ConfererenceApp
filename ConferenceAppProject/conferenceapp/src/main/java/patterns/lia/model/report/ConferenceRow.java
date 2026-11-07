package patterns.lia.model.report;

import java.time.LocalDateTime;

/**
 * A conference together with its number of confirmed registrations.
 */
public class ConferenceRow {

    private long id;
    private String title;
    private String venue;
    private LocalDateTime start;
    private LocalDateTime end;
    private int capacity;
    private int confirmed;

    public ConferenceRow(long id, String title, String venue, LocalDateTime start, LocalDateTime end, int capacity, int confirmed) {
        this.id = id;
        this.title = title;
        this.venue = venue;
        this.start = start;
        this.end = end;
        this.capacity = capacity;
        this.confirmed = confirmed;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(int confirmed) {
        this.confirmed = confirmed;
    }

    public int getAvailableSeats() {
        return capacity - confirmed;
    }

    public double getOccupancyRate() {
        return capacity <= 0 ? 0.0 : (double) confirmed / capacity;
    }
}
