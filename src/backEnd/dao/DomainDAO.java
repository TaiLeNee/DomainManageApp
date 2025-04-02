package backEnd.dao;

import entity.Domain;
import utils.ValidationUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DomainDAO {
    private final Connection connection;

    public DomainDAO(Connection connection) {
        this.connection = connection;
    }

    public void addDomain(Domain domain) throws SQLException, IllegalArgumentException {
        // Kiểm tra tính hợp lệ của domain trước khi thêm
        validateDomain(domain);

        String sql = "INSERT INTO domains (name, extension, price, status, expiry_date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, domain.getName());
            stmt.setString(2, domain.getExtension());
            stmt.setDouble(3, domain.getPrice());
            stmt.setString(4, domain.getStatus());

            // Xử lý ngày hết hạn
            if (domain.getExpiryDate() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(domain.getExpiryDate()));
            } else {
                stmt.setNull(5, java.sql.Types.TIMESTAMP);
            }

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    domain.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public Domain getDomainById(int id) throws SQLException, IllegalArgumentException {
        if (!ValidationUtils.isPositiveInteger(id)) {
            throw new IllegalArgumentException("ID phải là số nguyên dương");
        }

        String sql = "SELECT * FROM domains WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Domain domain = new Domain(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("extension"),
                            rs.getDouble("price"),
                            rs.getString("status")
                    );

                    // Đọc ngày hết hạn nếu có
                    Timestamp expiryTimestamp = rs.getTimestamp("expiry_date");
                    if (expiryTimestamp != null) {
                        domain.setExpiryDate(expiryTimestamp.toLocalDateTime());
                    }

                    return domain;
                }
            }
        }
        return null;
    }

    // Thêm phương thức để lấy domain theo tên và đuôi
    public Domain getDomainByNameAndExtension(String name, String extension) throws SQLException {
        String sql = "SELECT * FROM domains WHERE name = ? AND extension = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, extension);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Domain domain = new Domain(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("extension"),
                            rs.getDouble("price"),
                            rs.getString("status")
                    );

                    // Đọc ngày hết hạn nếu có
                    Timestamp expiryTimestamp = rs.getTimestamp("expiry_date");
                    if (expiryTimestamp != null) {
                        domain.setExpiryDate(expiryTimestamp.toLocalDateTime());
                    }

                    return domain;
                }
            }
        }
        return null;
    }

    // Implement the isDomainExists method
    public boolean isDomainExists(String name, String extension) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM domains WHERE name = ? AND extension = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, extension);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }

    // Implement the updateDomain method
    public void updateDomain(Domain domain) throws SQLException, IllegalArgumentException {
        // Kiểm tra tính hợp lệ của domain trước khi cập nhật
        validateDomain(domain);
        if (!ValidationUtils.isPositiveInteger(domain.getId())) {
            throw new IllegalArgumentException("ID phải là số nguyên dương");
        }

        String sql = "UPDATE domains SET name = ?, extension = ?, price = ?, status = ?, expiry_date = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, domain.getName());
            stmt.setString(2, domain.getExtension());
            stmt.setDouble(3, domain.getPrice());
            stmt.setString(4, domain.getStatus());

            // Xử lý ngày hết hạn
            if (domain.getExpiryDate() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(domain.getExpiryDate()));
            } else {
                stmt.setNull(5, java.sql.Types.TIMESTAMP);
            }

            stmt.setInt(6, domain.getId());
            stmt.executeUpdate();
        }
    }

    public List<Domain> getAllDomains() throws SQLException {
        List<Domain> domains = new ArrayList<>();
        String sql = "SELECT * FROM domains";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Domain domain = new Domain(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("extension"),
                        rs.getDouble("price"),
                        rs.getString("status")
                );

                // Đọc ngày hết hạn nếu có
                Timestamp expiryTimestamp = rs.getTimestamp("expiry_date");
                if (expiryTimestamp != null) {
                    domain.setExpiryDate(expiryTimestamp.toLocalDateTime());
                }

                domains.add(domain);
            }
        }
        return domains;
    }

    // Phương thức kiểm tra tên miền hết hạn
    public List<Domain> getExpiredDomains() throws SQLException {
        List<Domain> domains = new ArrayList<>();
        String sql = "SELECT * FROM domains WHERE status = 'Rented' AND expiry_date < CURRENT_TIMESTAMP";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Domain domain = new Domain(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("extension"),
                        rs.getDouble("price"),
                        rs.getString("status")
                );

                // Đọc ngày hết hạn
                Timestamp expiryTimestamp = rs.getTimestamp("expiry_date");
                if (expiryTimestamp != null) {
                    domain.setExpiryDate(expiryTimestamp.toLocalDateTime());
                }

                domains.add(domain);
            }
        }

        return domains;
    }

    // Phương thức kiểm tra tính hợp lệ của domain
    private void validateDomain(Domain domain) throws IllegalArgumentException {
        if (!ValidationUtils.isNotNull(domain)) {
            throw new IllegalArgumentException("Domain không được null");
        }

        if (!ValidationUtils.isValidDomainNamePart(domain.getName())) {
            throw new IllegalArgumentException("Tên miền không hợp lệ");
        }

        if (!ValidationUtils.isValidDomainExtension(domain.getExtension())) {
            throw new IllegalArgumentException("Đuôi tên miền không hợp lệ");
        }

        if (!ValidationUtils.isValidPrice(domain.getPrice())) {
            throw new IllegalArgumentException("Giá phải lớn hơn 0");
        }

        if (!ValidationUtils.isNotEmpty(domain.getStatus())) {
            throw new IllegalArgumentException("Trạng thái không được để trống");
        }
    }
}