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
                    Order order = mapToOrder(rs);
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
                    Order order = mapToOrder(rs);
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
                        return mapToOrder(rs);
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

    public void save(Order order) throws SQLException {
        // Gán mặc định nếu null
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(java.time.LocalDateTime.now());
        }

        if (order.getExpiryDate() == null) {
            // Nếu không có rentalPeriodId, mặc định cộng 12 tháng
            int months = 12;
            try {
                if (order.getRentalPeriodId() > 0) {
                    RentalPeriodRepository rentalRepo = new RentalPeriodRepository(connection);
                    model.RentalPeriod rental = rentalRepo.getRentalPeriodById(order.getRentalPeriodId());
                    if (rental != null) {
                        months = rental.getMonths();
                    }
                }
            } catch (Exception e) {
                System.err.println("Không thể lấy kỳ hạn thuê, dùng mặc định 12 tháng: " + e.getMessage());
            }
            order.setExpiryDate(order.getCreatedAt().plusMonths(months));
        }

        if (order.getId() <= 0) {
            // Thêm mới order
            String sql = "INSERT INTO orders (buyer_id, rental_period_id, status, created_at, expiry_date, total_price) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, order.getUserId());
                stmt.setInt(2, order.getRentalPeriodId());
                stmt.setString(3, order.getStatus());
                stmt.setTimestamp(4, Timestamp.valueOf(order.getCreatedAt()));
                stmt.setTimestamp(5, Timestamp.valueOf(order.getExpiryDate()));
                stmt.setDouble(6, order.getTotalPrice());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Tạo đơn hàng thất bại, không có dòng nào được thêm vào");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        order.setId(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Tạo đơn hàng thất bại, không lấy được ID");
                    }
                }
            }
        } else {
            // Cập nhật order hiện có
            String sql = "UPDATE orders SET buyer_id = ?, rental_period_id = ?, status = ?, created_at = ?, expiry_date = ?, total_price = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, order.getUserId());
                stmt.setInt(2, order.getRentalPeriodId());
                stmt.setString(3, order.getStatus());
                stmt.setTimestamp(4, Timestamp.valueOf(order.getCreatedAt()));
                stmt.setTimestamp(5, Timestamp.valueOf(order.getExpiryDate()));
                stmt.setDouble(6, order.getTotalPrice());
                stmt.setInt(7, order.getId());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Cập nhật đơn hàng thất bại, không tìm thấy ID: " + order.getId());
                }
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
     * Tìm đơn hàng gần nhất (mới nhất) liên quan đến một tên miền
     * Được sử dụng để xác định người sở hữu hiện tại của tên miền
     * Sử dụng bảng order_details để tìm thay vì dùng trường domain_id
     *
     * @param domainId ID của tên miền cần tìm đơn hàng
     * @return Order đơn hàng gần nhất của tên miền đó, hoặc null nếu không tìm thấy
     */
    public Order findLatestOrderByDomainId(int domainId) {
        try {
            // Tìm qua bảng order_details thay vì trường domain_id trong bảng orders
            String sql = "SELECT o.* FROM orders o " +
                    "JOIN order_details od ON o.id = od.order_id " +
                    "WHERE od.domain_id = ? " +
                    "ORDER BY o.created_at DESC";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, domainId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapToOrder(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding latest order by domain id: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Cập nhật trạng thái đơn hàng và tự động cập nhật trạng thái tên miền liên
     * quan
     * 
     * @param orderId   ID của đơn hàng cần cập nhật
     * @param newStatus Trạng thái mới của đơn hàng
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    public boolean updateOrderStatus(int orderId, String newStatus) {
        try {
            connection.setAutoCommit(false);

            // Cập nhật trạng thái đơn hàng
            String updateOrderSQL = "UPDATE orders SET status = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateOrderSQL)) {
                stmt.setString(1, newStatus);
                stmt.setInt(2, orderId);
                int updatedRows = stmt.executeUpdate();

                if (updatedRows == 0) {
                    connection.rollback();
                    return false;
                }
            }

            // Cập nhật trạng thái chi tiết đơn hàng
            String updateDetailsSQL = "UPDATE order_details SET status = ? WHERE order_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateDetailsSQL)) {
                stmt.setString(1, newStatus);
                stmt.setInt(2, orderId);
                stmt.executeUpdate();
            }

            // Cập nhật trạng thái tên miền tương ứng nếu đơn hàng đã hoàn thành
            if (newStatus.equalsIgnoreCase("Hoàn thành")) {
                // Lấy thông tin chi tiết đơn hàng
                OrderDetailsRepository orderDetailsRepo = new OrderDetailsRepository(connection);
                List<model.OrderDetails> orderDetails = orderDetailsRepo.findByOrderId(orderId);

                RentalPeriodRepository rentalPeriodRepo = new RentalPeriodRepository(connection);
                DomainRepository domainRepo = new DomainRepository(connection);

                java.util.Date currentDate = new java.util.Date();

                // Cập nhật từng tên miền trong đơn hàng với thời hạn thuê tương ứng
                for (model.OrderDetails detail : orderDetails) {
                    // Lấy thông tin về rental period
                    int rentalPeriodId = detail.getRentalPeriodId();
                    int months = 1; // Mặc định 1 tháng

                    try {
                        model.RentalPeriod rentalPeriod = rentalPeriodRepo.getRentalPeriodById(rentalPeriodId);
                        if (rentalPeriod != null) {
                            months = rentalPeriod.getMonths();
                        }
                    } catch (Exception ex) {
                        System.err.println("Lỗi lấy thông tin rental period: " + ex.getMessage());
                    }

                    // Tính ngày hết hạn dựa trên ngày hiện tại và số tháng thuê
                    java.util.Calendar calendar = java.util.Calendar.getInstance();
                    calendar.setTime(currentDate);
                    calendar.add(java.util.Calendar.MONTH, months);
                    java.sql.Timestamp expiryDate = new java.sql.Timestamp(calendar.getTimeInMillis());

                    // Cập nhật trạng thái và ngày hết hạn cho từng domain
                    try {
                        String updateDomainSQL = "UPDATE domains SET status = ?, expiry_date = ? WHERE id = ?";
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateDomainSQL)) {
                            updateStmt.setString(1, "Đã thuê");
                            updateStmt.setTimestamp(2, expiryDate);
                            updateStmt.setInt(3, detail.getDomainId());
                            updateStmt.executeUpdate();
                        }
                    } catch (SQLException e) {
                        System.err.println("Error updating domain status: " + e.getMessage());
                    }
                }

                // Cập nhật thời gian hết hạn cho đơn hàng chính dựa trên rental period
                Order order = getOrderById(orderId);
                if (order != null) {
                    int rentalPeriodId = order.getRentalPeriodId();
                    int months = 1;

                    try {
                        model.RentalPeriod rentalPeriod = rentalPeriodRepo.getRentalPeriodById(rentalPeriodId);
                        if (rentalPeriod != null) {
                            months = rentalPeriod.getMonths();
                        }
                    } catch (Exception ex) {
                        System.err.println("Lỗi lấy thông tin rental period: " + ex.getMessage());
                    }

                    // Tính ngày hết hạn cho đơn hàng
                    java.util.Calendar calendar = java.util.Calendar.getInstance();
                    calendar.setTime(currentDate);
                    calendar.add(java.util.Calendar.MONTH, months);
                    java.sql.Timestamp expiryDate = new java.sql.Timestamp(calendar.getTimeInMillis());

                    // Cập nhật thời gian hết hạn cho đơn hàng
                    String updateOrderExpirySQL = "UPDATE orders SET expiry_date = ? WHERE id = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateOrderExpirySQL)) {
                        updateStmt.setTimestamp(1, expiryDate);
                        updateStmt.setInt(2, orderId);
                        updateStmt.executeUpdate();
                    }
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            System.err.println("Error updating order status: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error setting auto commit: " + e.getMessage());
            }
        }
    }

    /**
     * Lấy tất cả đơn hàng cho một tên miền cụ thể
     * Sửa đổi để sử dụng order_details thay vì domain_id trong bảng orders
     * 
     * @param domainId ID của tên miền cần lấy đơn hàng
     * @return Danh sách các đơn hàng liên quan đến tên miền
     */
    public List<Order> getOrdersByDomainId(int domainId) {
        List<Order> orders = new ArrayList<>();

        try {
            // Sử dụng JOIN với bảng order_details thay vì trường domain_id
            String sql = "SELECT o.* FROM orders o " +
                    "JOIN order_details od ON o.id = od.order_id " +
                    "WHERE od.domain_id = ? " +
                    "ORDER BY o.created_at DESC";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, domainId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Order order = mapToOrder(rs);
                        orders.add(order);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting orders by domain id: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    // Chuyển đổi kết quả từ ResultSet sang đối tượng Order
    private Order mapToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setOrderDate(rs.getTimestamp("created_at"));
        order.setUserId(rs.getInt("buyer_id"));
        order.setTotalPrice(rs.getDouble("total_price"));

        // Chuẩn hóa status từ tiếng Anh sang tiếng Việt
        String status = rs.getString("status");
        if ("Completed".equalsIgnoreCase(status)) {
            order.setStatus("Hoàn thành");
        } else if ("Pending".equalsIgnoreCase(status)) {
            order.setStatus("Đang xử lý");
        } else if ("Cancelled".equalsIgnoreCase(status) || "Canceled".equalsIgnoreCase(status)) {
            order.setStatus("Hủy");
        } else if (status == null || status.isEmpty()) {
            order.setStatus("Đang xử lý"); // Mặc định là Đang xử lý nếu không có status
        } else {
            order.setStatus(status); // Giữ nguyên nếu đã là tiếng Việt
        }

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