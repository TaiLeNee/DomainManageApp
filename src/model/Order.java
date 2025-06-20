package model;

import java.time.LocalDateTime;
import java.util.Date;

public class Order {
    private int id;
    private int buyerId;
    private int userId; // Thêm trường userId cho tương thích
    private int rentalPeriodId; // Thêm trường cho gói thuê
    private String status; // Pending, Approved, Completed, Cancelled
    private LocalDateTime createdAt;
    private LocalDateTime expiryDate; // Ngày hết hạn thuê
    private double totalPrice; // Tổng giá cho toàn bộ thời gian thuê
    private Date orderDate; // Thêm trường orderDate

    // Constructors
    public Order() {
        this.createdAt = LocalDateTime.now(); // Initialize with current date/time
    }

    public Order(int id, int buyerId, int rentalPeriodId,
            String status, LocalDateTime createdAt, LocalDateTime expiryDate, double totalPrice) {
        this.id = id;
        this.buyerId = buyerId;
        this.rentalPeriodId = rentalPeriodId;
        this.status = status;
        this.createdAt = createdAt;
        this.expiryDate = expiryDate;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
        this.userId = buyerId; // Để đảm bảo tính nhất quán
    }

    public int getUserId() {
        return userId != 0 ? userId : buyerId; // Trả về userId nếu có, nếu không trả về buyerId
    }

    public void setUserId(int userId) {
        this.userId = userId;
        this.buyerId = userId; // Để đảm bảo tính nhất quán
    }

    // Các phương thức getDomainId và setDomainId đã được loại bỏ

    public int getRentalPeriodId() {
        return rentalPeriodId;
    }

    public void setRentalPeriodId(int rentalPeriodId) {
        this.rentalPeriodId = rentalPeriodId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
}