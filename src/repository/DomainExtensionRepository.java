package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DomainExtensionRepository {
    private Connection connection;

    public DomainExtensionRepository(Connection connection) {
        this.connection = connection;
    }

    // Constructor mặc định
    public DomainExtensionRepository() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Error creating DomainExtensionRepository: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Lấy tất cả các phần mở rộng tên miền
    public List<String> getAllExtensions() throws SQLException {
        List<String> extensions = new ArrayList<>();
        String sql = "SELECT extension FROM domain_extensions ORDER BY extension";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                extensions.add(rs.getString("extension"));
            }
        }

        return extensions;
    }

    // Lấy giá mặc định của một phần mở rộng
    public double getDefaultPrice(String extension) throws SQLException {
        String sql = "SELECT default_price FROM domain_extensions WHERE extension = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, extension);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("default_price");
                }
            }
        }

        return 100000.0; // Giá mặc định nếu không tìm thấy
    }

    // Lấy tất cả các phần mở rộng và giá mặc định
    public Map<String, Double> getAllExtensionsWithPrices() throws SQLException {
        Map<String, Double> priceMap = new HashMap<>();
        String sql = "SELECT extension, default_price FROM domain_extensions";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                priceMap.put(rs.getString("extension"), rs.getDouble("default_price"));
            }
        }

        return priceMap;
    }

    // Thêm hoặc cập nhật một phần mở rộng
    public void saveExtension(String extension, double defaultPrice, String description) throws SQLException {
        // Kiểm tra xem phần mở rộng đã tồn tại chưa
        String checkSql = "SELECT COUNT(*) AS count FROM domain_extensions WHERE extension = ?";
        boolean exists = false;

        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, extension);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    exists = rs.getInt("count") > 0;
                }
            }
        }

        if (exists) {
            // Cập nhật
            String updateSql = "UPDATE domain_extensions SET default_price = ?, description = ? WHERE extension = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
                stmt.setDouble(1, defaultPrice);
                stmt.setString(2, description);
                stmt.setString(3, extension);
                stmt.executeUpdate();
            }
        } else {
            // Thêm mới
            String insertSql = "INSERT INTO domain_extensions (extension, default_price, description) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
                stmt.setString(1, extension);
                stmt.setDouble(2, defaultPrice);
                stmt.setString(3, description);
                stmt.executeUpdate();
            }
        }
    }

    // Xóa một phần mở rộng
    public void deleteExtension(String extension) throws SQLException {
        String sql = "DELETE FROM domain_extensions WHERE extension = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, extension);
            stmt.executeUpdate();
        }
    }

    // Lấy thông tin chi tiết của một phần mở rộng
    public Optional<Map<String, Object>> getExtensionDetails(String extension) throws SQLException {
        String sql = "SELECT * FROM domain_extensions WHERE extension = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, extension);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> details = new HashMap<>();
                    details.put("id", rs.getInt("id"));
                    details.put("extension", rs.getString("extension"));
                    details.put("default_price", rs.getDouble("default_price"));
                    details.put("description", rs.getString("description"));
                    return Optional.of(details);
                }
            }
        }

        return Optional.empty();
    }

    
    public List<String[]> getAllDomainExtensions() throws SQLException {
        List<String[]> domainExtensions = new ArrayList<>();
        String sql = "SELECT extension, description, default_price FROM domain_extensions ORDER BY extension";
    
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String extension = rs.getString("extension");
                String description = rs.getString("description");
                String price = String.format("%.0f VND", rs.getDouble("default_price"));
                domainExtensions.add(new String[]{extension, description, price});
            }
        }
    
        return domainExtensions;
    }

    public List<String[]> searchDomainWithExtensions(String domainName) throws SQLException {
        List<String[]> results = new ArrayList<>();
        String sql = "SELECT CONCAT(?, extension) AS domain, " +
                     "       CASE WHEN EXISTS (SELECT 1 FROM domains WHERE name = CONCAT(?, extension)) THEN N'Không khả dụng' ELSE N'Khả dụng' END AS status, " +
                     "       default_price AS price " +
                     "FROM domain_extensions";
    
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, domainName);
            stmt.setString(2, domainName);
    
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String domain = rs.getString("domain");
                    String status = rs.getString("status");
                    String price = rs.getString("price");
                    results.add(new String[]{domain, status, price});
                }
            }
        }
    
        return results;
    }
}