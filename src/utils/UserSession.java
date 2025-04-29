package utils;

import model.User;

/**
 * Lớp singleton để quản lý thông tin người dùng đăng nhập trong phiên hiện tại
 */
public class UserSession {
    private static UserSession instance;
    private User currentUser;

    private UserSession() {
        // Private constructor để ngăn việc tạo instance từ bên ngoài
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return isLoggedIn() && "admin".equalsIgnoreCase(currentUser.getRole());
    }

    public boolean isUser() {
        return isLoggedIn() && "user".equalsIgnoreCase(currentUser.getRole());
    }

    public boolean isStaff() {
        return isLoggedIn() && "staff".equalsIgnoreCase(currentUser.getRole());
    }

    public void clearSession() {
        currentUser = null;
    }
}