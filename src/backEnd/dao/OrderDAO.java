package backEnd.dao;

import entity.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private final Connection connection;

    public OrderDAO(Connection connection) {
        this.connection = connection;
    }

    public void addOrder(Order order) throws SQLException {
        String sql = "INSERT INTO orders (buyer_id, domain_id, rental_period_id, status, created_at, expiry_date, total_price) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, order.getBuyerId());
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
    }

    public Order getOrderById(int id) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Order(
                            rs.getInt("id"),
                            rs.getInt("buyer_id"),
                            rs.getInt("domain_id"),
                            rs.getInt("rental_period_id"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("expiry_date").toLocalDateTime(),
                            rs.getDouble("total_price")
                    );
                }
            }
        }
        return null;
    }

    public List<Order> getAllOrders() throws SQLException {
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
                        rs.getDouble("total_price")
                ));
            }
        }
        return orders;
    }

    public void updateOrder(Order order) throws SQLException {
        String sql = "UPDATE orders SET buyer_id = ?, domain_id = ?, rental_period_id = ?, status = ?, created_at = ?, expiry_date = ?, total_price = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, order.getBuyerId());
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

    public void deleteOrder(int id) throws SQLException {
        String sql = "DELETE FROM orders WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Phương thức lấy đơn hàng theo id người dùng
    public List<Order> getOrdersByUserId(int userId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE buyer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
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
                            rs.getDouble("total_price")
                    ));
                }
            }
        }
        return orders;
    }
}