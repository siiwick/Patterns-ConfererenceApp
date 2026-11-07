package patterns.lia.model.report;

import java.time.LocalDateTime;

public class ParticipantRow {

    private long userId;
    private String fullName;
    private String email;
    private int age;
    private LocalDateTime registeredAt;
    private Integer waitlistPosition;

    public ParticipantRow(long userId, String fullName, String email, int age, LocalDateTime registeredAt, Integer waitlistPosition) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.age = age;
        this.registeredAt = registeredAt;
        this.waitlistPosition = waitlistPosition;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Integer getWaitlistPosition() {
        return waitlistPosition;
    }

    public void setWaitlistPosition(Integer waitlistPosition) {
        this.waitlistPosition = waitlistPosition;
    }
}
