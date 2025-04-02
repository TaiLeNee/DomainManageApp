package entity;

import java.time.LocalDateTime;

public class Order {
    private int id;
    private int buyerId;
    private int domainId;
    private int rentalPeriodId;  // Thêm trường cho gói thuê
    private String status; // Pending, Approved, Completed, Cancelled
    private LocalDateTime createdAt;
    private LocalDateTime expiryDate;  // Ngày hết hạn thuê
    private double totalPrice;        // Tổng giá cho toàn bộ thời gian thuê

    // Constructors
    public Order() {
    }

    public Order(int id, int buyerId, int domainId, int rentalPeriodId,
                 String status, LocalDateTime createdAt, LocalDateTime expiryDate, double totalPrice) {
        this.id = id;
        this.buyerId = buyerId;
        this.domainId = domainId;
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
    }

    public int getDomainId() {
        return domainId;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

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
}