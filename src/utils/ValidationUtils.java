package utils;

import java.util.regex.Pattern;

public class ValidationUtils {

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    
    // Domain name validation pattern (for domain part only, without extension)
    private static final Pattern DOMAIN_NAME_PATTERN = 
        Pattern.compile("^(?!-)(?!.*--)[A-Za-z0-9-]{1,63}(?<!-)$");
    
    // Domain extension validation pattern
    private static final Pattern DOMAIN_EXTENSION_PATTERN = 
        Pattern.compile("^\\.[A-Za-z]{2,6}$");
    
    // Full domain validation pattern
    private static final Pattern FULL_DOMAIN_PATTERN = 
        Pattern.compile("^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$");
    
    // Username validation pattern
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_]{4,20}$");
    
    // Password validation patterns
    private static final Pattern PASSWORD_LETTER = Pattern.compile("[a-zA-Z]");
    private static final Pattern PASSWORD_DIGIT = Pattern.compile("[0-9]");
    private static final Pattern PASSWORD_SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");
    
    // Maximum price value to prevent overflow (less than DECIMAL(15,2) limit)
    private static final double MAX_PRICE = 999999999.99;
    
    // ==================== USER VALIDATION ====================
    
    /**
     * Kiểm tra tên người dùng (username)
     * Username từ 4-20 ký tự, chỉ chứa chữ cái, số và dấu gạch dưới
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Kiểm tra mật khẩu
     * Mật khẩu ít nhất 6 ký tự, có ít nhất 1 chữ cái, 1 số và 1 ký tự đặc biệt
     */
    public static boolean isValidPassword(String password) {
        return password != null && 
               password.length() >= 6 &&
               PASSWORD_LETTER.matcher(password).find() &&
               PASSWORD_DIGIT.matcher(password).find() &&
               PASSWORD_SPECIAL.matcher(password).find();
    }

    /**
     * Kiểm tra email hợp lệ
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Kiểm tra tên đầy đủ
     */
    public static boolean isValidFullName(String fullName) {
        return fullName != null && 
               fullName.trim().length() >= 2 && 
               fullName.trim().length() <= 100;
    }

    // ==================== DOMAIN VALIDATION ====================
    
    /**
     * Kiểm tra tên miền đầy đủ (bao gồm extension)
     */
    public static boolean isValidDomainName(String domain) {
        return domain != null && FULL_DOMAIN_PATTERN.matcher(domain).matches();
    }

    /**
     * Kiểm tra tên miền với name và extension riêng biệt
     */
    public static boolean isValidDomainName(String name, String extension) {
        return isValidDomainNamePart(name) && isValidDomainExtension(extension);
    }

    /**
     * Kiểm tra phần tên của domain (không bao gồm đuôi)
     */
    public static boolean isValidDomainNamePart(String name) {
        return name != null && DOMAIN_NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Kiểm tra đuôi tên miền
     */
    public static boolean isValidDomainExtension(String extension) {
        return extension != null && DOMAIN_EXTENSION_PATTERN.matcher(extension).matches();
    }

    // ==================== PRICE VALIDATION ====================
    
    /**
     * Validate price value to prevent arithmetic overflow
     * 
     * @param price Price value to validate
     * @return true if price is valid, false otherwise
     */
    public static boolean isValidPrice(double price) {
        return !Double.isNaN(price) && 
               !Double.isInfinite(price) && 
               price > 0 && 
               price <= MAX_PRICE;
    }
    
    /**
     * Validate calculated price result to prevent overflow
     * 
     * @param basePrice Base price per month
     * @param months Number of months  
     * @param discount Discount ratio (0-1)
     * @return true if calculation will not overflow, false otherwise
     */
    public static boolean isValidPriceCalculation(double basePrice, int months, double discount) {
        if (!isValidPrice(basePrice) || months <= 0 || months > 120) {
            return false;
        }
        
        if (discount < 0 || discount >= 1) {
            return false;
        }
        
        try {
            // Check if multiplication will overflow
            if (basePrice > MAX_PRICE / months) {
                return false;
            }
            
            double result = basePrice * months * (1 - discount);
            return result > 0 && result <= 9999999999999.99; // DECIMAL(15,2) max value
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Format price value safely
     * 
     * @param price Price to format
     * @return Formatted price string
     */
    public static String formatPrice(double price) {
        if (!isValidPrice(price)) {
            return "Invalid Price";
        }
        
        return String.format("%,.2f", price);
    }

    // ==================== ORDER VALIDATION ====================
    
    /**
     * Kiểm tra trạng thái đơn hàng
     */
    public static boolean isValidOrderStatus(String status) {
        if (status == null) return false;
        String statusUpperCase = status.toUpperCase();
        return statusUpperCase.equals("PENDING") ||
                statusUpperCase.equals("APPROVED") ||
                statusUpperCase.equals("COMPLETED") ||
                statusUpperCase.equals("CANCELLED") ||
                statusUpperCase.equals("ĐANG XỬ LÝ") ||
                statusUpperCase.equals("HOÀN THÀNH") ||
                statusUpperCase.equals("HỦY");
    }

    // ==================== PAYMENT VALIDATION ====================
    
    /**
     * Kiểm tra số thẻ (chỉ chứa 16 chữ số)
     */
    public static boolean isValidCardNumber(String cardNumber) {
        return cardNumber != null && cardNumber.matches("\\d{16}");
    }

    /**
     * Kiểm tra ngày hết hạn (định dạng MM/YY)
     */
    public static boolean isValidExpiryDate(String expiryDate) {
        return expiryDate != null && expiryDate.matches("(0[1-9]|1[0-2])/\\d{2}");
    }

    /**
     * Kiểm tra mã CVV (chỉ chứa 3 hoặc 4 chữ số)
     */
    public static boolean isValidCVV(String cvv) {
        return cvv != null && cvv.matches("\\d{3,4}");
    }

    // ==================== GENERAL VALIDATION ====================
    
    /**
     * Kiểm tra chuỗi không rỗng
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Kiểm tra số nguyên dương
     */
    public static boolean isPositiveInteger(int value) {
        return value > 0;
    }

    /**
     * Kiểm tra số thực không âm
     */
    public static boolean isNonNegativeDouble(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value) && value >= 0;
    }

    /**
     * Kiểm tra nếu đối tượng không null
     */
    public static boolean isNotNull(Object obj) {
        return obj != null;
    }
}