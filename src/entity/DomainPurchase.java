package entity;

import java.time.LocalDateTime;

public class DomainPurchase {
    private int orderId;
    private String domainName;
    private double price;
    private LocalDateTime purchaseDate;
    private LocalDateTime expiryDate;
    private String status;
    private int rentalPeriod;

    public DomainPurchase() {
    }

    public DomainPurchase(int orderId, String domainName, double price,
                          LocalDateTime purchaseDate, LocalDateTime expiryDate,
                          String status, int rentalPeriod) {
        this.orderId = orderId;
        this.domainName = domainName;
        this.price = price;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.rentalPeriod = rentalPeriod;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRentalPeriod() {
        return rentalPeriod;
    }

    public void setRentalPeriod(int rentalPeriod) {
        this.rentalPeriod = rentalPeriod;
    }
}