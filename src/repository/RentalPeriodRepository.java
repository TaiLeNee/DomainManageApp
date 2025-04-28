package repository;

import model.RentalPeriod;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RentalPeriodRepository {
    private Connection connection;

    public RentalPeriodRepository(Connection connection) {
        this.connection = connection;
    }

    // Constructor mặc định để AdminDashboardView có thể khởi tạo
    public RentalPeriodRepository() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Error creating RentalPeriodRepository: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Các phương thức được gọi từ AdminDashboardView
    public List<RentalPeriod> getAllRentalPeriods() {
        try {
            return findAll();
        } catch (SQLException e) {
            System.err.println("Error getting all rental periods: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public RentalPeriod getRentalPeriodById(int id) {
        try {
            Optional<RentalPeriod> rentalPeriod = findById(id);
            return rentalPeriod.orElse(null);
        } catch (SQLException e) {
            System.err.println("Error getting rental period by id: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Phương thức gốc
    public void save(RentalPeriod rentalPeriod) throws SQLException {
        if (rentalPeriod.getId() <= 0) {
            // Thêm mới
            String sql = "INSERT INTO rental_periods (months, discount, description) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, rentalPeriod.getMonths());
                stmt.setDouble(2, rentalPeriod.getDiscount());
                stmt.setString(3, rentalPeriod.getDescription());
                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        rentalPeriod.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } else {
            // Cập nhật
            String sql = "UPDATE rental_periods SET months = ?, discount = ?, description = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, rentalPeriod.getMonths());
                stmt.setDouble(2, rentalPeriod.getDiscount());
                stmt.setString(3, rentalPeriod.getDescription());
                stmt.setInt(4, rentalPeriod.getId());
                stmt.executeUpdate();
            }
        }
    }

    public Optional<RentalPeriod> findById(int id) throws SQLException {
        String sql = "SELECT * FROM rental_periods WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    RentalPeriod rentalPeriod = new RentalPeriod(
                            rs.getInt("id"),
                            rs.getInt("months"),
                            rs.getDouble("discount"),
                            rs.getString("description"));
                    return Optional.of(rentalPeriod);
                }
            }
        }
        return Optional.empty();
    }

    public List<RentalPeriod> findAll() throws SQLException {
        List<RentalPeriod> rentalPeriods = new ArrayList<>();
        String sql = "SELECT * FROM rental_periods ORDER BY months ASC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rentalPeriods.add(new RentalPeriod(
                        rs.getInt("id"),
                        rs.getInt("months"),
                        rs.getDouble("discount"),
                        rs.getString("description")));
            }
        }
        return rentalPeriods;
    }

    public Optional<RentalPeriod> findByMonths(int months) throws SQLException {
        String sql = "SELECT * FROM rental_periods WHERE months = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, months);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    RentalPeriod rentalPeriod = new RentalPeriod(
                            rs.getInt("id"),
                            rs.getInt("months"),
                            rs.getDouble("discount"),
                            rs.getString("description"));
                    return Optional.of(rentalPeriod);
                }
            }
        }
        return Optional.empty();
    }

    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM rental_periods WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}