package patterns.lia.model;

import java.time.LocalDateTime;

import patterns.lia.model.enums.Role;

/**
 * Profile information of a user.
 */
public class User {

    private long id;
    private String fullName;
    private String email;
    private int age;
    private Role role;
    private LocalDateTime createdAt;

    public User(long id, String fullName, String email, int age, Role role, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.age = age;
        this.role = role;
        this.createdAt = createdAt;
    }

    //creates a user that isnt saved to the database yet
    public User(String fullName, String email, int age, Role role) {
        this(0, fullName, email, age, role, null);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    @Override
    public String toString() {
        return "User[id=" + id + ", fullName=" + fullName + ", email=" + email + ", age=" + age + ", role=" + role + "]";
    }
}
