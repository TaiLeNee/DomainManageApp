package entity;

import java.time.LocalDateTime;

public class Order {
    private int id;
    private int buyerId;
    private int domainId;
    private String status; // Pending, Confirmed, etc.
    private LocalDateTime createdAt;

    // Constructors
    public Order() {
    }

    public Order(int id, int buyerId, int domainId, String status, LocalDateTime createdAt) {
        this.id = id;
        this.buyerId = buyerId;
        this.domainId = domainId;
        this.status = status;
        this.createdAt = createdAt;
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
}