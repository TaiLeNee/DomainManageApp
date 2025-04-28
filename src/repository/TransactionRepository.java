package repository;

import model.Transaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class TransactionRepository {
    private Connection connection;

    public TransactionRepository(Connection connection) {
        this.connection = connection;
    }

    // Constructor mặc định để AdminDashboardView có thể khởi tạo
    public TransactionRepository() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Error creating TransactionRepository: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Phương thức được gọi từ AdminDashboardView
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        try {
            String sql = "SELECT * FROM transactions ORDER BY timestamp DESC";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Transaction transaction = new Transaction();
                    transaction.setId(rs.getInt("id"));
                    transaction.setOrderId(rs.getInt("order_id"));
                    transaction.setAmount(rs.getDouble("amount"));

                    // Kiểm tra xem có cột domain_id không
                    if (hasColumn(rs, "domain_id")) {
                        transaction.setDomainId(rs.getInt("domain_id"));
                    }

                    // Cố gắng lấy user_id nếu có
                    if (hasColumn(rs, "user_id")) {
                        transaction.setUserId(rs.getInt("user_id"));
                    }

                    // Cố gắng lấy các trường khác nếu có
                    if (hasColumn(rs, "transaction_type")) {
                        transaction.setTransactionType(rs.getString("transaction_type"));
                    }

                    if (hasColumn(rs, "payment_method")) {
                        transaction.setPaymentMethod(rs.getString("payment_method"));
                    }

                    if (hasColumn(rs, "status")) {
                        transaction.setStatus(rs.getString("status"));
                    }

                    // Lấy ngày giao dịch từ cột timestamp
                    Timestamp timestampDate = rs.getTimestamp("timestamp");
                    if (timestampDate != null) {
                        transaction.setTransactionDate(timestampDate.toLocalDateTime());
                    }

                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting all transactions: " + e.getMessage());
            e.printStackTrace();
        }

        return transactions;
    }

    // Thêm phương thức hỗ trợ kiểm tra xem một cột có tồn tại trong ResultSet không
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

    // Phương thức để lưu một giao dịch mới
    public void save(Transaction transaction) throws SQLException {
        if (transaction.getId() <= 0) {
            // Thêm mới
            String sql = "INSERT INTO transactions (user_id, order_id, amount, transaction_type, payment_method, transaction_date, status) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, transaction.getUserId());
                stmt.setInt(2, transaction.getOrderId());
                stmt.setDouble(3, transaction.getAmount());
                stmt.setString(4, transaction.getTransactionType());
                stmt.setString(5, transaction.getPaymentMethod());
                stmt.setTimestamp(6, Timestamp.valueOf(transaction.getTransactionDate()));
                stmt.setString(7, transaction.getStatus());

                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        transaction.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } else {
            // Cập nhật
            String sql = "UPDATE transactions SET user_id = ?, order_id = ?, amount = ?, transaction_type = ?, " +
                    "payment_method = ?, transaction_date = ?, status = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, transaction.getUserId());
                stmt.setInt(2, transaction.getOrderId());
                stmt.setDouble(3, transaction.getAmount());
                stmt.setString(4, transaction.getTransactionType());
                stmt.setString(5, transaction.getPaymentMethod());
                stmt.setTimestamp(6, Timestamp.valueOf(transaction.getTransactionDate()));
                stmt.setString(7, transaction.getStatus());
                stmt.setInt(8, transaction.getId());

                stmt.executeUpdate();
            }
        }
    }

    // Phương thức để lấy giao dịch theo ID
    public Optional<Transaction> findById(int id) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Transaction transaction = new Transaction();
                    transaction.setId(rs.getInt("id"));
                    transaction.setUserId(rs.getInt("user_id"));
                    transaction.setOrderId(rs.getInt("order_id"));
                    transaction.setAmount(rs.getDouble("amount"));
                    transaction.setTransactionType(rs.getString("transaction_type"));
                    transaction.setPaymentMethod(rs.getString("payment_method"));
                    transaction.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
                    transaction.setStatus(rs.getString("status"));

                    return Optional.of(transaction);
                }
            }
        }
        return Optional.empty();
    }

    // Phương thức để lấy tất cả các giao dịch theo ID người dùng
    public List<Transaction> findByUserId(int userId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY transaction_date DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = new Transaction();
                    transaction.setId(rs.getInt("id"));
                    transaction.setUserId(rs.getInt("user_id"));
                    transaction.setOrderId(rs.getInt("order_id"));
                    transaction.setAmount(rs.getDouble("amount"));
                    transaction.setTransactionType(rs.getString("transaction_type"));
                    transaction.setPaymentMethod(rs.getString("payment_method"));
                    transaction.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
                    transaction.setStatus(rs.getString("status"));

                    transactions.add(transaction);
                }
            }
        }

        return transactions;
    }

    // Phương thức để lấy tất cả các giao dịch theo ID đơn hàng
    public List<Transaction> findByOrderId(int orderId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE order_id = ? ORDER BY transaction_date DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = new Transaction();
                    transaction.setId(rs.getInt("id"));
                    transaction.setUserId(rs.getInt("user_id"));
                    transaction.setOrderId(rs.getInt("order_id"));
                    transaction.setAmount(rs.getDouble("amount"));
                    transaction.setTransactionType(rs.getString("transaction_type"));
                    transaction.setPaymentMethod(rs.getString("payment_method"));
                    transaction.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
                    transaction.setStatus(rs.getString("status"));

                    transactions.add(transaction);
                }
            }
        }

        return transactions;
    }

    // Phương thức để xóa một giao dịch theo ID
    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Phương thức để lấy tổng doanh thu
    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(amount) as total FROM transactions WHERE status = 'Completed'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0;
    }

    // Phương thức để lấy doanh thu trong khoảng thời gian
    public double getRevenueInPeriod(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = "SELECT SUM(amount) as total FROM transactions WHERE status = 'Completed' AND transaction_date BETWEEN ? AND ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0;
    }
}