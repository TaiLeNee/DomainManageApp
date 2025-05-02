package repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Domain;
import utils.ValidationUtils;

public class DomainRepository {
    private Connection connection;

    public DomainRepository(Connection connection) {
        this.connection = connection;
    }

    // Constructor mặc định để AdminDashboardView có thể khởi tạo
    public DomainRepository() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Error creating DomainRepository: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Các phương thức được gọi từ AdminDashboardView
    public List<Domain> getAllDomains() {
        try {
            return findAll();
        } catch (SQLException e) {
            System.err.println("Error getting all domains: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Domain getDomainById(int id) {
        try {
            Optional<Domain> domain = findById(id);
            return domain.orElse(null);
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error getting domain by id: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteDomain(int id) {
        try {
            // Xóa các bản ghi liên quan trong bảng orders
            String deleteOrdersSql = "DELETE FROM orders WHERE domain_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteOrdersSql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

            // Xóa các bản ghi liên quan trong bảng transactions
            String deleteTransactionsSql = "DELETE FROM transactions WHERE domain_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteTransactionsSql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

            // Xóa domain
            String deleteDomainSql = "DELETE FROM domains WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteDomainSql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting domain: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void save(Domain domain) throws SQLException, IllegalArgumentException {
        validateDomain(domain);

        if (domain.getId() <= 0) {
            // Thêm mới
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
        } else {
            // Cập nhật
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
    }

    public Optional<Domain> findById(int id) throws SQLException, IllegalArgumentException {
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
                            rs.getString("status"));

                    // Đọc ngày hết hạn nếu có
                    Timestamp expiryTimestamp = rs.getTimestamp("expiry_date");
                    if (expiryTimestamp != null) {
                        domain.setExpiryDate(expiryTimestamp.toLocalDateTime());
                    }

                    return Optional.of(domain);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Domain> findByNameAndExtension(String name, String extension) throws SQLException {
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
                            rs.getString("status"));

                    // Đọc ngày hết hạn nếu có
                    Timestamp expiryTimestamp = rs.getTimestamp("expiry_date");
                    if (expiryTimestamp != null) {
                        domain.setExpiryDate(expiryTimestamp.toLocalDateTime());
                    }

                    return Optional.of(domain);
                }
            }
        }
        return Optional.empty();
    }

    public List<Domain> findAll() throws SQLException {
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
                        rs.getString("status"));

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

    public boolean existsByNameAndExtension(String name, String extension) throws SQLException {
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

    public List<Domain> findExpiredDomains() throws SQLException {
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
                        rs.getString("status"));

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

    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM domains WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

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

    /**
     * Tìm kiếm tên miền theo từ khóa
     * 
     * @param searchTerm Từ khóa tìm kiếm (tên miền hoặc phần mở rộng)
     * @return Danh sách các tên miền phù hợp với từ khóa
     */
    public List<Domain> searchDomains(String searchTerm) {
        List<Domain> results = new ArrayList<>();

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            try {
                return findAll(); // Trả về tất cả nếu từ khóa tìm kiếm trống
            } catch (SQLException e) {
                System.err.println("Error getting all domains: " + e.getMessage());
                e.printStackTrace();
                return results;
            }
        }

        String searchPattern = "%" + searchTerm.trim() + "%";
        String sql = "SELECT * FROM domains WHERE name LIKE ? OR extension LIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Domain domain = new Domain(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("extension"),
                            rs.getDouble("price"),
                            rs.getString("status"));

                    // Đọc ngày hết hạn nếu có
                    Timestamp expiryTimestamp = rs.getTimestamp("expiry_date");
                    if (expiryTimestamp != null) {
                        domain.setExpiryDate(expiryTimestamp.toLocalDateTime());
                    }

                    results.add(domain);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching domains: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Cập nhật trạng thái các tên miền theo ID đơn hàng
     * 
     * @param orderId ID của đơn hàng cần cập nhật tên miền
     * @param status  Trạng thái mới của tên miền (Đã thuê, Khả dụng, v.v.)
     * @return Số lượng tên miền được cập nhật thành công
     */
    public int updateDomainStatusByOrderId(int orderId, String status) {
        int updatedCount = 0;

        try {
            // Lấy danh sách domain_id từ order_details
            String selectSQL = "SELECT domain_id FROM order_details WHERE order_id = ?";
            List<Integer> domainIds = new ArrayList<>();

            try (PreparedStatement stmt = connection.prepareStatement(selectSQL)) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        domainIds.add(rs.getInt("domain_id"));
                    }
                }
            }

            if (domainIds.isEmpty()) {
                return 0;
            }

            // Cập nhật trạng thái cho tất cả domain_id đã lấy
            for (int domainId : domainIds) {
                String updateSQL = "UPDATE domains SET status = ? WHERE id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(updateSQL)) {
                    stmt.setString(1, status);
                    stmt.setInt(2, domainId);
                    updatedCount += stmt.executeUpdate();
                }
            }

            return updatedCount;
        } catch (SQLException e) {
            System.err.println("Error updating domain status by order: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Cập nhật trạng thái và ngày hết hạn các tên miền theo ID đơn hàng
     * 
     * @param orderId    ID của đơn hàng cần cập nhật tên miền
     * @param status     Trạng thái mới của tên miền (Đã thuê, Khả dụng, v.v.)
     * @param expiryDate Ngày hết hạn mới cho tên miền
     * @return Số lượng tên miền được cập nhật thành công
     */
    public int updateDomainStatusByOrderIdWithExpiryDate(int orderId, String status, Timestamp expiryDate) {
        int updatedCount = 0;

        try {
            // Lấy danh sách domain_id từ order_details
            String selectSQL = "SELECT domain_id FROM order_details WHERE order_id = ?";
            List<Integer> domainIds = new ArrayList<>();

            try (PreparedStatement stmt = connection.prepareStatement(selectSQL)) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        domainIds.add(rs.getInt("domain_id"));
                    }
                }
            }

            if (domainIds.isEmpty()) {
                return 0;
            }

            // Cập nhật trạng thái và ngày hết hạn cho tất cả domain_id đã lấy
            for (int domainId : domainIds) {
                String updateSQL = "UPDATE domains SET status = ?, expiry_date = ? WHERE id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(updateSQL)) {
                    stmt.setString(1, status);
                    stmt.setTimestamp(2, expiryDate);
                    stmt.setInt(3, domainId);
                    updatedCount += stmt.executeUpdate();
                }
            }

            return updatedCount;
        } catch (SQLException e) {
            System.err.println("Error updating domain status and expiry date by order: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

}