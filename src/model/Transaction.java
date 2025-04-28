package model;

import java.time.LocalDateTime;

public class Transaction {
    private int id;
    private int orderId;
    private int domainId;
    private int userId; // Thêm trường userId
    private double total;
    private double amount; // Thêm trường amount
    private String transactionType; // Thêm trường transactionType
    private String paymentMethod; // Thêm trường paymentMethod
    private String status; // Thêm trường status
    private LocalDateTime timestamp;
    private LocalDateTime transactionDate; // Thêm trường transactionDate

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

    // Constructor đầy đủ
    public Transaction(int id, int userId, int orderId, double amount, String transactionType,
                       String paymentMethod, LocalDateTime transactionDate, String status) {
        this.id = id;
        this.userId = userId;
        this.orderId = orderId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.paymentMethod = paymentMethod;
        this.transactionDate = transactionDate;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
}