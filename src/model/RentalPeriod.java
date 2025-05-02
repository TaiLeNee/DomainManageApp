package model;

/**
 * Represents a rental period with discount for domain rentals
 */
public class RentalPeriod {
    private int id;
    private int months;
    private double discount;
    private String description;

    public RentalPeriod() {
    }

    public RentalPeriod(int id, int months, double discount, String description) {
        this.id = id;
        this.months = months;
        this.discount = discount;
        this.description = description;
    }

    /**
     * @return the rental period ID
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the rental period ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the number of months in this rental period
     */
    public int getMonths() {
        return months;
    }

    /**
     * @param months the number of months to set
     */
    public void setMonths(int months) {
        this.months = months;
    }

    /**
     * @return the discount percentage (as a decimal, e.g., 0.1 for 10%)
     */
    public double getDiscount() {
        return discount;
    }

    /**
     * @param discount the discount percentage to set (as a decimal)
     */
    public void setDiscount(double discount) {
        this.discount = discount;
    }

    /**
     * @return the description of this rental period
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}