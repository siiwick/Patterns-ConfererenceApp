package patterns.lia.model;

import patterns.lia.model.enums.RegistrationStatus;

public class Registration {
    private long id;
    private long conferenceId;
    private long userId;
    private RegistrationStatus registrationStatus;
    private int waitlistPosition;
    public Registration(long id, long conferenceId, long userId, RegistrationStatus registrationStatus,
            int waitlistPosition) {
        this.id = id;
        this.conferenceId = conferenceId;
        this.userId = userId;
        this.registrationStatus = registrationStatus;
        this.waitlistPosition = waitlistPosition;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getConferenceId() {
        return conferenceId;
    }
    public void setConferenceId(long conferenceId) {
        this.conferenceId = conferenceId;
    }
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public RegistrationStatus getRegistrationStatus() {
        return registrationStatus;
    }
    public void setRegistrationStatus(RegistrationStatus registrationStatus) {
        this.registrationStatus = registrationStatus;
    }
    public int getWaitlistPosition() {
        return waitlistPosition;
    }
    public void setWaitlistPosition(int waitlistPosition) {
        this.waitlistPosition = waitlistPosition;
    }
    @Override
    public String toString() {
        return userId+" | "+conferenceId+" | "+registrationStatus+" | "+waitlistPosition;
    }
    public boolean equals(Registration registration){
        if(id!=registration.getId()) return false;
        if(userId!=registration.getUserId()) return false;
        if(conferenceId!=registration.getConferenceId()) return false;
        if(registrationStatus!=registration.getRegistrationStatus()) return false;
        if(waitlistPosition!=registration.getWaitlistPosition()) return false;
        return true;
    }
}
