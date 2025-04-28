package model;

import java.util.Date;

public class User {
    private int id;
    private String fullName;
    private String username;
    private String password;
    private String email;
    private String role; // ADMIN or BUYER
    private boolean active = true; // Mặc định là hoạt động
    private Date createdDate = new Date(); // Mặc định là ngày hiện tại

    // Constructors
    public User() {
    }

    public User(int id, String fullName, String username, String password, String email, String role) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    // Constructor không có email (để tương thích ngược)
    public User(int id, String fullName, String username, String password, String role) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Constructor đầy đủ với active và createdDate
    public User(int id, String fullName, String username, String password, String email, String role, boolean active,
                Date createdDate) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.active = active;
        this.createdDate = createdDate;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    // Phương thức tiện ích để lấy int từ role string cho AdminDashboardView
    public int getRoleAsInt() {
        if (role == null) {
            return 3; // Customer là mặc định
        }

        switch (role.toLowerCase()) {
            case "admin":
                return 1;
            case "staff":
                return 2;
            default:
                return 3; // Customer
        }
    }
}