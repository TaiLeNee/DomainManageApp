package model;

import java.time.LocalDateTime;

public class OrderDetails {
    private int id;
    private int orderId;
    private int domainId;
    private String domainName;
    private String domainExtension;
    private double price; // This will store the final discounted price
    private double originalPrice; // This will store the original price before discount
    private LocalDateTime purchaseDate;
    private LocalDateTime expiryDate;
    private int rentalPeriodId;
    private String status;

    public OrderDetails() {
    }

    public OrderDetails(int id, int orderId, int domainId, String domainName,
            String domainExtension, double price, double originalPrice, LocalDateTime purchaseDate,
            LocalDateTime expiryDate, int rentalPeriodId, String status) {
        this.id = id;
        this.orderId = orderId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.domainExtension = domainExtension;
        this.price = price;
        this.originalPrice = originalPrice;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.rentalPeriodId = rentalPeriodId;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getDomainId() {
        return domainId;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getDomainExtension() {
        return domainExtension;
    }

    public void setDomainExtension(String domainExtension) {
        this.domainExtension = domainExtension;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
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

    public String getFullDomainName() {
        return domainName + domainExtension;
    }

    // Get discount percentage
    public double getDiscountPercentage() {
        if (originalPrice <= 0)
            return 0;
        return ((originalPrice - price) / originalPrice) * 100;
    }
}