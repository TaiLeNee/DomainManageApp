package entity;

import java.time.LocalDateTime;

public class Transaction {
    private int id;
    private int orderId;
    private int domainId;
    private double total;
    private LocalDateTime timestamp;

    // Constructors
    public Transaction() {
    }

    public Transaction(int id, int orderId, int domainId, double total, LocalDateTime timestamp) {
        this.id = id;
        this.orderId = orderId;
        this.domainId = domainId;
        this.total = total;
        this.timestamp = timestamp;
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

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}