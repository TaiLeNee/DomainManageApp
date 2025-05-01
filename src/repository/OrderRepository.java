package repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Order;


public class OrderRepository {
    private Connection connection;

    public OrderRepository(Connection connection) {
        this.connection = connection;
    }

    // Constructor mặc định để AdminDashboardView có thể khởi tạo
    public OrderRepository() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Error creating OrderRepository: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Các phương thức được gọi từ AdminDashboardView
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();

        try {
            String sql = "SELECT * FROM orders ORDER BY created_at DESC";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setUserId(rs.getInt("buyer_id"));
                    order.setDomainId(rs.getInt("domain_id"));
                    order.setTotalPrice(rs.getDouble("total_price"));
                    order.setOrderDate(rs.getTimestamp("created_at"));
                    order.setStatus(rs.getString("status"));

                    // Thêm rental period id nếu có trong schema
                    if (hasColumn(rs, "rental_period_id")) {
                        order.setRentalPeriodId(rs.getInt("rental_period_id"));
                    }

                    // Thêm ngày hết hạn nếu có trong schema
                    if (hasColumn(rs, "expiry_date")) {
                        Timestamp expiryDate = rs.getTimestamp("expiry_date");
                        if (expiryDate != null) {
                            order.setExpiryDate(expiryDate.toLocalDateTime());
                        }
                    }

                    // Thêm ngày tạo nếu có trong schema
                    if (hasColumn(rs, "created_at")) {
                        Timestamp createdAt = rs.getTimestamp("created_at");
                        if (createdAt != null) {
                            order.setCreatedAt(createdAt.toLocalDateTime());
                        }
                    }

                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting all orders: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    public List<Order> getRecentOrders(int limit) {
        List<Order> orders = new ArrayList<>();

        try {
            String sql = "SELECT TOP " + limit + " * FROM orders ORDER BY created_at DESC";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setUserId(rs.getInt("buyer_id"));
                    order.setDomainId(rs.getInt("domain_id"));
                    order.setTotalPrice(rs.getDouble("total_price"));
                    order.setOrderDate(rs.getTimestamp("created_at"));
                    order.setStatus(rs.getString("status"));

                    // Thêm rental period id nếu có trong schema
                    if (hasColumn(rs, "rental_period_id")) {
                        order.setRentalPeriodId(rs.getInt("rental_period_id"));
                    }

                    // Thêm ngày hết hạn nếu có trong schema
                    if (hasColumn(rs, "expiry_date")) {
                        Timestamp expiryDate = rs.getTimestamp("expiry_date");
                        if (expiryDate != null) {
                            order.setExpiryDate(expiryDate.toLocalDateTime());
                        }
                    }

                    // Thêm ngày tạo nếu có trong schema
                    if (hasColumn(rs, "created_at")) {
                        Timestamp createdAt = rs.getTimestamp("created_at");
                        if (createdAt != null) {
                            order.setCreatedAt(createdAt.toLocalDateTime());
                        }
                    }

                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting recent orders: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    public Order getOrderById(int id) {
        try {
            String sql = "SELECT * FROM orders WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Order order = new Order();
                        order.setId(rs.getInt("id"));
                        order.setUserId(rs.getInt("buyer_id"));
                        order.setDomainId(rs.getInt("domain_id"));
                        order.setTotalPrice(rs.getDouble("total_price"));
                        order.setOrderDate(rs.getTimestamp("created_at"));
                        order.setStatus(rs.getString("status"));

                        // Thêm rental period id nếu có trong schema
                        if (hasColumn(rs, "rental_period_id")) {
                            order.setRentalPeriodId(rs.getInt("rental_period_id"));
                        }

                        // Thêm ngày hết hạn nếu có trong schema
                        if (hasColumn(rs, "expiry_date")) {
                            Timestamp expiryDate = rs.getTimestamp("expiry_date");
                            if (expiryDate != null) {
                                order.setExpiryDate(expiryDate.toLocalDateTime());
                            }
                        }

                        // Thêm ngày tạo nếu có trong schema
                        if (hasColumn(rs, "created_at")) {
                            Timestamp createdAt = rs.getTimestamp("created_at");
                            if (createdAt != null) {
                                order.setCreatedAt(createdAt.toLocalDateTime());
                            }
                        }

                        return order;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting order by id: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateOrder(Order order) {
        try {
            String sql = "UPDATE orders SET status = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, order.getStatus());
                stmt.setInt(2, order.getId());
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating order: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Các phương thức cho logic nghiệp vụ
    public void save(Order order) throws SQLException {
        if (order.getId() <= 0) {
            // Thêm mới
            String sql = "INSERT INTO orders (buyer_id, domain_id, rental_period_id, status, created_at, expiry_date, total_price) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, order.getUserId());
                stmt.setInt(2, order.getDomainId());
                stmt.setInt(3, order.getRentalPeriodId());
                stmt.setString(4, order.getStatus());
                stmt.setTimestamp(5, Timestamp.valueOf(order.getCreatedAt()));
                stmt.setTimestamp(6, Timestamp.valueOf(order.getExpiryDate()));
                stmt.setDouble(7, order.getTotalPrice());
                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        order.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } else {
            // Cập nhật
            String sql = "UPDATE orders SET buyer_id = ?, domain_id = ?, rental_period_id = ?, status = ?, created_at = ?, expiry_date = ?, total_price = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, order.getUserId());
                stmt.setInt(2, order.getDomainId());
                stmt.setInt(3, order.getRentalPeriodId());
                stmt.setString(4, order.getStatus());
                stmt.setTimestamp(5, Timestamp.valueOf(order.getCreatedAt()));
                stmt.setTimestamp(6, Timestamp.valueOf(order.getExpiryDate()));
                stmt.setDouble(7, order.getTotalPrice());
                stmt.setInt(8, order.getId());
                stmt.executeUpdate();
            }
        }
    }

    // Phương thức hỗ trợ kiểm tra xem một cột có tồn tại trong ResultSet không
    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columns = metaData.getColumnCount();
        for (int i = 1; i <= columns; i++) {
            if (columnName.equals(metaData.getColumnName(i))) {
                return true;
            }
        }
        return false;
    }

    public Optional<Order> findById(int id) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order(
                            rs.getInt("id"),
                            rs.getInt("buyer_id"),
                            rs.getInt("domain_id"),
                            rs.getInt("rental_period_id"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("expiry_date").toLocalDateTime(),
                            rs.getDouble("total_price"));
                    return Optional.of(order);
                }
            }
        }
        return Optional.empty();
    }

    public List<Order> findAll() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                orders.add(new Order(
                        rs.getInt("id"),
                        rs.getInt("buyer_id"),
                        rs.getInt("domain_id"),
                        rs.getInt("rental_period_id"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("expiry_date").toLocalDateTime(),
                        rs.getDouble("total_price")));
            }
        }
        return orders;
    }

    public List<Order> findByBuyerId(int buyerId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE buyer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, buyerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(new Order(
                            rs.getInt("id"),
                            rs.getInt("buyer_id"),
                            rs.getInt("domain_id"),
                            rs.getInt("rental_period_id"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("expiry_date").toLocalDateTime(),
                            rs.getDouble("total_price")));
                }
            }
        }
        return orders;
    }

    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM orders WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Tìm đơn hàng gần nhất (mới nhất) theo domainId
     * Được sử dụng để xác định người sở hữu hiện tại của tên miền
     *
     * @param domainId ID của tên miền cần tìm đơn hàng
     * @return Order đơn hàng gần nhất của tên miền đó, hoặc null nếu không tìm thấy
     */
    public Order findLatestOrderByDomainId(int domainId) {
        try {
            String sql = "SELECT * FROM orders WHERE domain_id = ? ORDER BY created_at DESC";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, domainId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Order order = new Order();
                        order.setId(rs.getInt("id"));
                        order.setUserId(rs.getInt("buyer_id"));
                        order.setDomainId(rs.getInt("domain_id"));
                        order.setTotalPrice(rs.getDouble("total_price"));
                        order.setOrderDate(rs.getTimestamp("created_at"));
                        order.setStatus(rs.getString("status"));

                        // Thêm rental period id nếu có trong schema
                        if (hasColumn(rs, "rental_period_id")) {
                            order.setRentalPeriodId(rs.getInt("rental_period_id"));
                        }

                        // Thêm ngày hết hạn nếu có trong schema
                        if (hasColumn(rs, "expiry_date")) {
                            Timestamp expiryDate = rs.getTimestamp("expiry_date");
                            if (expiryDate != null) {
                                order.setExpiryDate(expiryDate.toLocalDateTime());
                            }
                        }

                        // Thêm ngày tạo nếu có trong schema
                        if (hasColumn(rs, "created_at")) {
                            Timestamp createdAt = rs.getTimestamp("created_at");
                            if (createdAt != null) {
                                order.setCreatedAt(createdAt.toLocalDateTime());
                            }
                        }

                        return order;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding latest order by domain id: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}