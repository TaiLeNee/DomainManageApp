package utils;

import java.io.*;
import java.util.Properties;

public class LoginPreferences {
    private static final String PREFERENCES_FILE = "src/resource/login.properties";
    private static final String USERNAME_KEY = "saved_username";
    private static final String PASSWORD_KEY = "saved_password";
    private static final String REMEMBER_KEY = "remember_login";
    
    /**
     * Lưu thông tin đăng nhập
     */
    public static void saveLoginInfo(String username, String password, boolean remember) {
        Properties props = new Properties();
        
        if (remember) {
            props.setProperty(USERNAME_KEY, username);
            props.setProperty(PASSWORD_KEY, encodePassword(password));
            props.setProperty(REMEMBER_KEY, "true");
        } else {
            // Nếu không ghi nhớ, xóa thông tin đã lưu
            props.setProperty(USERNAME_KEY, "");
            props.setProperty(PASSWORD_KEY, "");
            props.setProperty(REMEMBER_KEY, "false");
        }
        
        try (FileOutputStream out = new FileOutputStream(PREFERENCES_FILE)) {
            props.store(out, "Login Preferences");
        } catch (IOException e) {
            System.err.println("Không thể lưu thông tin đăng nhập: " + e.getMessage());
        }
    }
    
    /**
     * Đọc thông tin đăng nhập đã lưu
     */
    public static LoginInfo loadLoginInfo() {
        Properties props = new Properties();
        
        try (FileInputStream in = new FileInputStream(PREFERENCES_FILE)) {
            props.load(in);
            
            String remember = props.getProperty(REMEMBER_KEY, "false");
            if ("true".equals(remember)) {
                String username = props.getProperty(USERNAME_KEY, "");
                String password = decodePassword(props.getProperty(PASSWORD_KEY, ""));
                
                return new LoginInfo(username, password, true);
            }
        } catch (IOException e) {
            // File không tồn tại hoặc không đọc được, trả về thông tin rỗng
        }
        
        return new LoginInfo("", "", false);
    }
    
    /**
     * Xóa thông tin đăng nhập đã lưu
     */
    public static void clearLoginInfo() {
        saveLoginInfo("", "", false);
    }
    
    /**
     * Mã hóa mật khẩu đơn giản (chỉ để che giấu, không phải bảo mật cao)
     */
    private static String encodePassword(String password) {
        StringBuilder encoded = new StringBuilder();
        for (char c : password.toCharArray()) {
            encoded.append((char)(c + 3)); // Dịch chuyển ký tự đi 3 vị trí
        }
        return encoded.toString();
    }
    
    /**
     * Giải mã mật khẩu
     */
    private static String decodePassword(String encodedPassword) {
        StringBuilder decoded = new StringBuilder();
        for (char c : encodedPassword.toCharArray()) {
            decoded.append((char)(c - 3)); // Dịch chuyển ngược lại 3 vị trí
        }
        return decoded.toString();
    }
    
    /**
     * Lớp chứa thông tin đăng nhập
     */
    public static class LoginInfo {
        private String username;
        private String password;
        private boolean remember;
        
        public LoginInfo(String username, String password, boolean remember) {
            this.username = username;
            this.password = password;
            this.remember = remember;
        }
        
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public boolean isRemember() { return remember; }
    }
}
