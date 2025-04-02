package utils;

import java.util.regex.Pattern;

public class ValidationUtils {

    // Kiểm tra tên người dùng (username)
    public static boolean isValidUsername(String username) {
        // Username từ 4-20 ký tự, chỉ chứa chữ cái, số và dấu gạch dưới
        return username != null && Pattern.matches("^[a-zA-Z0-9_]{4,20}$", username);
    }

    // Kiểm tra mật khẩu
    public static boolean isValidPassword(String password) {
        // Mật khẩu ít nhất 6 ký tự, có ít nhất 1 chữ cái và 1 số
        return password != null && password.length() >= 6
                && Pattern.matches(".*[a-zA-Z].*", password)
                && Pattern.matches(".*[0-9].*", password);
    }

    // Kiểm tra email
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email != null && Pattern.matches(emailRegex, email);
    }

    // Kiểm tra tên miền
    public static boolean isValidDomainName(String domain) {
        // Kiểm tra tên miền hợp lệ
        String domainRegex = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$";
        return domain != null && Pattern.matches(domainRegex, domain);
    }

    public static boolean isValidDomainName(String name, String extension) {
        return isValidDomainName(name + extension);
    }

    public static boolean isValidDomainNamePart(String name) {
        // Kiểm tra phần tên của domain (không bao gồm đuôi)
        String nameRegex = "^(?!-)(?!.*--)[A-Za-z0-9-]{1,63}(?<!-)$";
        return name != null && Pattern.matches(nameRegex, name);
    }

    public static boolean isValidDomainExtension(String extension) {
        // Kiểm tra đuôi tên miền
        String extensionRegex = "^\\.[A-Za-z]{2,6}$";
        return extension != null && Pattern.matches(extensionRegex, extension);
    }

    // Kiểm tra giá tiền
    public static boolean isValidPrice(double price) {
        // Giá phải lớn hơn 0
        return price > 0;
    }

    // Kiểm tra trạng thái đơn hàng
    public static boolean isValidOrderStatus(String status) {
        if (status == null) return false;
        String statusUpperCase = status.toUpperCase();
        return statusUpperCase.equals("PENDING") ||
                statusUpperCase.equals("APPROVED") ||
                statusUpperCase.equals("COMPLETED") ||
                statusUpperCase.equals("CANCELLED");
    }

    // Kiểm tra chuỗi không rỗng
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    // Kiểm tra số nguyên dương
    public static boolean isPositiveInteger(int value) {
        return value > 0;
    }

    // Kiểm tra nếu đối tượng là null
    public static boolean isNotNull(Object obj) {
        return obj != null;
    }
}