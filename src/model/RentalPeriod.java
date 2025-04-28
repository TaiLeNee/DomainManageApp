package model;

public class RentalPeriod {
    private int id;
    private int months;         // Số tháng thuê
    private double discount;    // Giảm giá (tỷ lệ phần trăm)
    private String description; // Mô tả

    public RentalPeriod() {
    }

    public RentalPeriod(int id, int months, double discount, String description) {
        this.id = id;
        this.months = months;
        this.discount = discount;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMonths() {
        return months;
    }

    public void setMonths(int months) {
        this.months = months;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Tính giá sau khi giảm
    public double calculateDiscountedPrice(double originalPrice) {
        return originalPrice * months * (1 - discount);
    }
}