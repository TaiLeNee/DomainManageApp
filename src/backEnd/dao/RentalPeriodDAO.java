package backEnd.dao;

import entity.RentalPeriod;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RentalPeriodDAO {
    private final Connection connection;

    public RentalPeriodDAO(Connection connection) {
        this.connection = connection;
    }

    public List<RentalPeriod> getAllRentalPeriods() throws SQLException {
        List<RentalPeriod> periods = new ArrayList<>();
        String sql = "SELECT * FROM rental_periods ORDER BY months";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                periods.add(new RentalPeriod(
                        rs.getInt("id"),
                        rs.getInt("months"),
                        rs.getDouble("discount"),
                        rs.getString("description")
                ));
            }
        }
        return periods;
    }

    public RentalPeriod getRentalPeriodById(int id) throws SQLException {
        String sql = "SELECT * FROM rental_periods WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new RentalPeriod(
                            rs.getInt("id"),
                            rs.getInt("months"),
                            rs.getDouble("discount"),
                            rs.getString("description")
                    );
                }
            }
        }
        return null;
    }

    public void addRentalPeriod(RentalPeriod period) throws SQLException {
        String sql = "INSERT INTO rental_periods (months, discount, description) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, period.getMonths());
            stmt.setDouble(2, period.getDiscount());
            stmt.setString(3, period.getDescription());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    period.setId(generatedKeys.getInt(1));
                }
            }
        }
    }
}