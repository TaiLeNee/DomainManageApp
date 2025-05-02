package repository;

import model.OrderDetails;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDetailsRepository {
    private Connection connection;

    public OrderDetailsRepository() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Create OrderDetails table if it doesn't exist
    public void createTableIfNotExists() {
        String sql = "IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[order_details]') AND type in (N'U'))"
                +
                "BEGIN " +
                "CREATE TABLE order_details (" +
                "    id INT IDENTITY(1,1) PRIMARY KEY," +
                "    order_id INT NOT NULL," +
                "    domain_id INT NOT NULL," +
                "    domain_name VARCHAR(100) NOT NULL," +
                "    domain_extension VARCHAR(20) NOT NULL," +
                "    price DECIMAL(10, 2) NOT NULL," +
                "    purchase_date DATETIME DEFAULT GETDATE()," +
                "    status VARCHAR(20) NOT NULL," +
                "    FOREIGN KEY (order_id) REFERENCES orders(id)," +
                "    FOREIGN KEY (domain_id) REFERENCES domains(id)" +
                ");" +
                "END";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Error creating order_details table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean insert(OrderDetails orderDetails) {
        String sql = "INSERT INTO order_details (order_id, domain_id, domain_name, domain_extension, price, purchase_date, status) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, orderDetails.getOrderId());
            stmt.setInt(2, orderDetails.getDomainId());
            stmt.setString(3, orderDetails.getDomainName());
            stmt.setString(4, orderDetails.getDomainExtension());
            stmt.setDouble(5, orderDetails.getPrice());
            stmt.setTimestamp(6, Timestamp.valueOf(orderDetails.getPurchaseDate()));
            stmt.setString(7, orderDetails.getStatus());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        orderDetails.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting order details: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<OrderDetails> findByOrderId(int orderId) {
        List<OrderDetails> detailsList = new ArrayList<>();
        String sql = "SELECT * FROM order_details WHERE order_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderDetails details = new OrderDetails(
                            rs.getInt("id"),
                            rs.getInt("order_id"),
                            rs.getInt("domain_id"),
                            rs.getString("domain_name"),
                            rs.getString("domain_extension"),
                            rs.getDouble("price"),
                            rs.getTimestamp("purchase_date").toLocalDateTime(),
                            rs.getString("status"));
                    detailsList.add(details);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding order details by order ID: " + e.getMessage());
            e.printStackTrace();
        }
        return detailsList;
    }

    public OrderDetails getOrderDetailById(int detailId) {
        String sql = "SELECT * FROM order_details WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, detailId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new OrderDetails(
                            rs.getInt("id"),
                            rs.getInt("order_id"),
                            rs.getInt("domain_id"),
                            rs.getString("domain_name"),
                            rs.getString("domain_extension"),
                            rs.getDouble("price"),
                            rs.getTimestamp("purchase_date").toLocalDateTime(),
                            rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting order detail by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<OrderDetails> findByUserId(int userId) {
        List<OrderDetails> detailsList = new ArrayList<>();
        String sql = "SELECT od.* FROM order_details od " +
                "JOIN orders o ON od.order_id = o.id " +
                "WHERE o.buyer_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderDetails details = new OrderDetails(
                            rs.getInt("id"),
                            rs.getInt("order_id"),
                            rs.getInt("domain_id"),
                            rs.getString("domain_name"),
                            rs.getString("domain_extension"),
                            rs.getDouble("price"),
                            rs.getTimestamp("purchase_date").toLocalDateTime(),
                            rs.getString("status"));
                    detailsList.add(details);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding order details by user ID: " + e.getMessage());
            e.printStackTrace();
        }
        return detailsList;
    }

    public boolean update(OrderDetails orderDetails) {
        String sql = "UPDATE order_details SET domain_id = ?, domain_name = ?, domain_extension = ?, " +
                "price = ?, status = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderDetails.getDomainId());
            stmt.setString(2, orderDetails.getDomainName());
            stmt.setString(3, orderDetails.getDomainExtension());
            stmt.setDouble(4, orderDetails.getPrice());
            stmt.setString(5, orderDetails.getStatus());
            stmt.setInt(6, orderDetails.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order details: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateOrderDetailStatus(int detailId, String status) {
        String sql = "UPDATE order_details SET status = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, detailId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                // If status is updated successfully, check if all domain statuses are complete
                // and update the parent order status accordingly
                updateParentOrderStatusIfNeeded(detailId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error updating order detail status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void updateParentOrderStatusIfNeeded(int detailId) {
        try {
            // Get the order ID for this detail
            int orderId = -1;

            String getOrderIdSql = "SELECT order_id FROM order_details WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(getOrderIdSql)) {
                stmt.setInt(1, detailId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        orderId = rs.getInt("order_id");
                    } else {
                        return; // Detail not found
                    }
                }
            }

            // Check if all domains in this order are complete
            boolean allComplete = true;

            String checkStatusSql = "SELECT status FROM order_details WHERE order_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(checkStatusSql)) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next() && allComplete) {
                        String status = rs.getString("status");
                        allComplete = "Hoàn thành".equals(status);
                    }
                }
            }

            // If all domains are complete, update the order status
            if (allComplete) {
                String updateOrderSql = "UPDATE orders SET status = 'Hoàn thành' WHERE id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(updateOrderSql)) {
                    stmt.setInt(1, orderId);
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking/updating parent order status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean deleteByOrderId(int orderId) {
        String sql = "DELETE FROM order_details WHERE order_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting order details: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<OrderDetails> findByDomainId(int domainId) {
        List<OrderDetails> detailsList = new ArrayList<>();
        String sql = "SELECT * FROM order_details WHERE domain_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, domainId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderDetails details = new OrderDetails(
                            rs.getInt("id"),
                            rs.getInt("order_id"),
                            rs.getInt("domain_id"),
                            rs.getString("domain_name"),
                            rs.getString("domain_extension"),
                            rs.getDouble("price"),
                            rs.getTimestamp("purchase_date").toLocalDateTime(),
                            rs.getString("status"));
                    detailsList.add(details);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding order details by domain ID: " + e.getMessage());
            e.printStackTrace();
        }
        return detailsList;
    }
}