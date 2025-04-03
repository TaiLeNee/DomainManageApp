package backEnd.repository;

import entity.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionRepository {
    private final Connection connection;

    public TransactionRepository(Connection connection) {
        this.connection = connection;
    }

    public void save(Transaction transaction) throws SQLException {
        if (transaction.getId() <= 0) {
            // Thêm mới
            String sql = "INSERT INTO transactions (order_id, domain_id, total, timestamp) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, transaction.getOrderId());
                stmt.setInt(2, transaction.getDomainId());
                stmt.setDouble(3, transaction.getTotal());
                stmt.setTimestamp(4, Timestamp.valueOf(transaction.getTimestamp()));
                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        transaction.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } else {
            // Cập nhật
            String sql = "UPDATE transactions SET order_id = ?, domain_id = ?, total = ?, timestamp = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, transaction.getOrderId());
                stmt.setInt(2, transaction.getDomainId());
                stmt.setDouble(3, transaction.getTotal());
                stmt.setTimestamp(4, Timestamp.valueOf(transaction.getTimestamp()));
                stmt.setInt(5, transaction.getId());
                stmt.executeUpdate();
            }
        }
    }

    public Optional<Transaction> findById(int id) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Transaction transaction = new Transaction(
                            rs.getInt("id"),
                            rs.getInt("order_id"),
                            rs.getInt("domain_id"),
                            rs.getDouble("total"),
                            rs.getTimestamp("timestamp").toLocalDateTime()
                    );
                    return Optional.of(transaction);
                }
            }
        }
        return Optional.empty();
    }

    public List<Transaction> findAll() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                transactions.add(new Transaction(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        rs.getInt("domain_id"),
                        rs.getDouble("total"),
                        rs.getTimestamp("timestamp").toLocalDateTime()
                ));
            }
        }
        return transactions;
    }

    public List<Transaction> findByOrderId(int orderId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Transaction(
                            rs.getInt("id"),
                            rs.getInt("order_id"),
                            rs.getInt("domain_id"),
                            rs.getDouble("total"),
                            rs.getTimestamp("timestamp").toLocalDateTime()
                    ));
                }
            }
        }
        return transactions;
    }

    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}