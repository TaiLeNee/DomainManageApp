package repository;

import model.User;
import model.DomainPurchase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private Connection connection;

    public UserRepository(Connection connection) {
        this.connection = connection;
    }

    // Constructor mặc định để AdminDashboardView có thể khởi tạo
    public UserRepository() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Error creating UserRepository: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Các phương thức được gọi từ AdminDashboardView
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        try {
            return findAll();
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    public User getUserById(int id) {
        try {
            Optional<User> user = findById(id);
            return user.orElse(null);
        } catch (SQLException e) {
            System.err.println("Error getting user by id: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateUser(User user) {
        try {
            save(user);
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void save(User user) throws SQLException {
        if (user.getId() <= 0) {
            // Thêm mới
            String sql = "INSERT INTO users (fullname, username, password, email, role) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.getFullName());
                stmt.setString(2, user.getUsername());
                stmt.setString(3, user.getPassword());
                stmt.setString(4, user.getEmail());
                stmt.setString(5, user.getRole());
                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } else {
            // Cập nhật
            String sql = "UPDATE users SET fullname = ?, username = ?, password = ?, email = ?, role = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, user.getFullName());
                stmt.setString(2, user.getUsername());
                stmt.setString(3, user.getPassword());
                stmt.setString(4, user.getEmail());
                stmt.setString(5, user.getRole());
                stmt.setInt(6, user.getId());
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
                            rs.getString("email"),
                            rs.getString("role"));
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
                        rs.getString("email"),
                        rs.getString("role")));
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
                            rs.getString("email"),
                            rs.getString("role"));
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("fullname"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email"),
                            rs.getString("role"));
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    // Phương thức xác thực người dùng
    public Optional<User> authenticate(String usernameOrEmail, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE (username = ? OR email = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, usernameOrEmail);
            stmt.setString(2, usernameOrEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    // So sánh mật khẩu
                    if (password.equals(storedPassword)) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setUsername(rs.getString("username"));
                        user.setEmail(rs.getString("email"));
                        user.setFullName(rs.getString("fullname"));
                        user.setRole(rs.getString("role"));

                        // Kiểm tra có cột active không trước khi đọc
                        try {
                            rs.findColumn("active");
                            user.setActive(rs.getBoolean("active"));
                            // Kiểm tra nếu tài khoản không active
                            if (!rs.getBoolean("active")) {
                                return Optional.empty();
                            }
                        } catch (SQLException e) {
                            // Nếu không có cột active, mặc định là active
                            user.setActive(true);
                        }

                        // Lấy ngày tạo nếu có
                        try {
                            Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
                            if (createdAtTimestamp != null) {
                                user.setCreatedDate(new Date(createdAtTimestamp.getTime()));
                            }
                        } catch (SQLException e) {
                            // Nếu không có cột created_at, bỏ qua
                        }

                        return Optional.of(user);
                    }
                }
            }
        }
        return Optional.empty();
    }

    // Phương thức kiểm tra email đã tồn tại chưa
    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }

    // Phương thức kiểm tra username đã tồn tại chưa
    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
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
                            rs.getInt("rental_period"));
                    purchases.add(purchase);
                }
            }
        }
        return purchases;
    }
}