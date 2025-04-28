package model;

import java.time.LocalDateTime;

public class Domain {
    private int id;
    private String name;        // Chỉ phần tên (ví dụ: google, facebook)
    private String extension;   // Phần đuôi (.com, .vn, .io)
    private double price;       // Giá dựa trên extension cho 1 tháng
    private String status;      // Available, Rented, Reserved, etc.
    private LocalDateTime expiryDate; // Ngày hết hạn thuê

    // Constructors
    public Domain() {
    }

    public Domain(int id, String name, String extension, double price, String status) {
        this.id = id;
        this.name = name;
        this.extension = extension;
        this.price = price;
        this.status = status;
    }

    public Domain(int id, String name, String extension, double price, String status, LocalDateTime expiryDate) {
        this.id = id;
        this.name = name;
        this.extension = extension;
        this.price = price;
        this.status = status;
        this.expiryDate = expiryDate;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    // Phương thức để lấy tên miền đầy đủ
    public String getFullDomainName() {
        return name + extension;
    }

    // Tính giá cho một khoảng thời gian thuê
    public double calculatePriceForPeriod(int months) {
        // Giảm giá khi thuê dài hạn
        switch (months) {
            case 1:
                return price; // Giá gốc cho 1 tháng
            case 6:
                return price * 6 * 0.9; // Giảm 10% khi thuê 6 tháng
            case 12:
                return price * 12 * 0.8; // Giảm 20% khi thuê 12 tháng
            default:
                return price * months; // Không có khuyến mãi
        }
    }
}