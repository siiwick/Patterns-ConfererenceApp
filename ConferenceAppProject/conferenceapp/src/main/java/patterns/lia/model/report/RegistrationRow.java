package patterns.lia.model.report;

import java.time.LocalDateTime;

import patterns.lia.model.enums.RegistrationStatus;

public class RegistrationRow {

    private long conferenceId;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private RegistrationStatus status;
    private Integer waitlistPosition;

    public RegistrationRow(long conferenceId, String title, LocalDateTime start, LocalDateTime end, RegistrationStatus status, Integer waitlistPosition) {
        this.conferenceId = conferenceId;
        this.title = title;
        this.start = start;
        this.end = end;
        this.status = status;
        this.waitlistPosition = waitlistPosition;
    }

    public long getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(long conferenceId) {
        this.conferenceId = conferenceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public Integer getWaitlistPosition() {
        return waitlistPosition;
    }

    public void setWaitlistPosition(Integer waitlistPosition) {
        this.waitlistPosition = waitlistPosition;
    }
}
