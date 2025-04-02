package backEnd.dao;

import entity.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private final Connection connection;

    public TransactionDAO(Connection connection) {
        this.connection = connection;
    }

    public void addTransaction(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (order_id, domain_id, total, timestamp) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, transaction.getOrderId());
            stmt.setInt(2, transaction.getDomainId());
            stmt.setDouble(3, transaction.getTotal());
            stmt.setTimestamp(4, Timestamp.valueOf(transaction.getTimestamp()));
            stmt.executeUpdate();
        }
    }

    public Transaction getTransactionById(int id) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Transaction(
                            rs.getInt("id"),
                            rs.getInt("order_id"),
                            rs.getInt("domain_id"),
                            rs.getDouble("total"),
                            rs.getTimestamp("timestamp").toLocalDateTime()
                    );
                }
            }
        }
        return null;
    }

    public List<Transaction> getAllTransactions() throws SQLException {
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

    public void updateTransaction(Transaction transaction) throws SQLException {
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

    public void deleteTransaction(int id) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}