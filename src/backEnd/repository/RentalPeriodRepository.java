package backEnd.repository;

import entity.RentalPeriod;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RentalPeriodRepository {
    private final Connection connection;

    public RentalPeriodRepository(Connection connection) {
        this.connection = connection;
    }

    public void save(RentalPeriod period) throws SQLException {
        if (period.getId() <= 0) {
            // Thêm mới
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
        } else {
            // Cập nhật
            String sql = "UPDATE rental_periods SET months = ?, discount = ?, description = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, period.getMonths());
                stmt.setDouble(2, period.getDiscount());
                stmt.setString(3, period.getDescription());
                stmt.setInt(4, period.getId());
                stmt.executeUpdate();
            }
        }
    }

    public List<RentalPeriod> findAll() throws SQLException {
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

    public Optional<RentalPeriod> findById(int id) throws SQLException {
        String sql = "SELECT * FROM rental_periods WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    RentalPeriod period = new RentalPeriod(
                            rs.getInt("id"),
                            rs.getInt("months"),
                            rs.getDouble("discount"),
                            rs.getString("description")
                    );
                    return Optional.of(period);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<RentalPeriod> findByMonths(int months) throws SQLException {
        String sql = "SELECT * FROM rental_periods WHERE months = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, months);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    RentalPeriod period = new RentalPeriod(
                            rs.getInt("id"),
                            rs.getInt("months"),
                            rs.getDouble("discount"),
                            rs.getString("description")
                    );
                    return Optional.of(period);
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