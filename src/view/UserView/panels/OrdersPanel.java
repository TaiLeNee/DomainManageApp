package view.UserView.panels;

import java.awt.*;
import java.sql.Timestamp;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class OrdersPanel extends JPanel {
    private JTable ordersTable;
    private DefaultTableModel tableModel;

    public OrdersPanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Đơn hàng", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        add(label, BorderLayout.NORTH);

        // Tạo bảng hiển thị đơn hàng
        String[] columnNames = {"Tên miền", "Tổng tiền", "Ngày thanh toán", "Tình trạng"};
        tableModel = new DefaultTableModel(columnNames, 0);
        ordersTable = new JTable(tableModel);
        ordersTable.setRowHeight(30);
        ordersTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(ordersTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addOrder(String domainName, double totalPrice, Timestamp paymentDate, String status) {
        tableModel.addRow(new Object[]{
                domainName,
                String.format("%,.2f VND", totalPrice),
                paymentDate.toString(),
                status
        });
    }

    public void clearTable() {
        tableModel.setRowCount(0); // Xóa tất cả các hàng trong bảng
    }
}