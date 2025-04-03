package backEnd.repository;

import entity.User;
import entity.DomainPurchase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private final Connection connection;

    public UserRepository(Connection connection) {
        this.connection = connection;
    }

    public void save(User user) throws SQLException {
        if (user.getId() <= 0) {
            // Thêm mới
            String sql = "INSERT INTO users (fullname, username, password, role) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.getFullName());
                stmt.setString(2, user.getUsername());
                stmt.setString(3, user.getPassword());
                stmt.setString(4, user.getRole());
                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } else {
            // Cập nhật
            String sql = "UPDATE users SET fullname = ?, username = ?, password = ?, role = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, user.getFullName());
                stmt.setString(2, user.getUsername());
                stmt.setString(3, user.getPassword());
                stmt.setString(4, user.getRole());
                stmt.setInt(5, user.getId());
                stmt.executeUpdate();
            }
        }
    }

    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("fullname"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role")
                    );
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("fullname"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                ));
            }
        }
        return users;
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("fullname"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role")
                    );
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<DomainPurchase> findDomainPurchasesByUserId(int userId) throws SQLException {
        List<DomainPurchase> purchases = new ArrayList<>();
        String sql = "SELECT o.id as order_id, " +
                "CONCAT(d.name, d.extension) as domain_name, " +
                "o.total_price as price, " +
                "o.created_at as purchase_date, " +
                "o.expiry_date, " +
                "o.status, " +
                "rp.months as rental_period " +
                "FROM orders o " +
                "JOIN domains d ON o.domain_id = d.id " +
                "JOIN rental_periods rp ON o.rental_period_id = rp.id " +
                "WHERE o.buyer_id = ? " +
                "ORDER BY o.created_at DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DomainPurchase purchase = new DomainPurchase(
                            rs.getInt("order_id"),
                            rs.getString("domain_name"),
                            rs.getDouble("price"),
                            rs.getTimestamp("purchase_date").toLocalDateTime(),
                            rs.getTimestamp("expiry_date").toLocalDateTime(),
                            rs.getString("status"),
                            rs.getInt("rental_period")
                    );
                    purchases.add(purchase);
                }
            }
        }
        return purchases;
    }
}