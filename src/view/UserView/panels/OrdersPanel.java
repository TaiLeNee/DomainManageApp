package view.UserView.panels;

import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import model.OrderDetails;
import repository.OrderDetailsRepository;
import service.OrderDetailsService;
import utils.UserSession;

public class OrdersPanel extends JPanel {
    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private OrderDetailsService orderDetailsService;

    public OrdersPanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Tên miền đã thuê", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        add(label, BorderLayout.NORTH);

        // Tạo bảng hiển thị tên miền đã thuê
        String[] columnNames = { "Tên miền", "Giá", "Ngày thanh toán", "Tình trạng" };
        tableModel = new DefaultTableModel(columnNames, 0);
        ordersTable = new JTable(tableModel);
        ordersTable.setRowHeight(30);
        ordersTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(ordersTable);
        add(scrollPane, BorderLayout.CENTER);

        // Initialize the service
        orderDetailsService = new OrderDetailsService();

        // Load user's domain orders
        loadUserDomainOrders();
    }

    // Load the user's domain orders from the database
    private void loadUserDomainOrders() {
        clearTable();

        // Get current user ID 
        int userId = UserSession.getInstance().getCurrentUser().getId();

        // Get all order details for the current user
        List<OrderDetails> orderDetailsList = orderDetailsService.getOrderDetailsByUserId(userId);

        // Add each domain to the table
        for (OrderDetails detail : orderDetailsList) {
            addDomainOrder(
                    detail.getFullDomainName(),
                    detail.getPrice(),
                    Timestamp.valueOf(detail.getPurchaseDate()),
                    detail.getStatus());
        }
    }

    // Add a domain order to the table
    public void addDomainOrder(String domainName, double price, Timestamp paymentDate, String status) {
        tableModel.addRow(new Object[] {
                domainName,
                String.format("%,.2f VND", price),
                paymentDate.toString(),
                status
        });
    }

    // Legacy method for compatibility - uses the new method internally
    public void addOrder(String domainName, double price, Timestamp paymentDate, String status) {
        addDomainOrder(domainName, price, paymentDate, status);
    }

    public void clearTable() {
        tableModel.setRowCount(0); // Xóa tất cả các hàng trong bảng
    }

    // Refresh the order panel data
    public void refresh() {
        loadUserDomainOrders();
    }
}