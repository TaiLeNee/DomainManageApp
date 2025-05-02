package model;

import java.time.LocalDateTime;

public class OrderDetails {
    private int id;
    private int orderId;
    private int domainId;
    private String domainName;
    private String domainExtension;
    private double price;
    private LocalDateTime purchaseDate;
    private String status;

    public OrderDetails() {
    }

    public OrderDetails(int id, int orderId, int domainId, String domainName,
            String domainExtension, double price, LocalDateTime purchaseDate, String status) {
        this.id = id;
        this.orderId = orderId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.domainExtension = domainExtension;
        this.price = price;
        this.purchaseDate = purchaseDate;
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

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
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
}