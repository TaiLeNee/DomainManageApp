package view.UserView.panels;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import repository.DatabaseConnection;

public class MyDomainsPanel extends JPanel {
    private JTable domainsTable;
    private DefaultTableModel tableModel;
    private JButton payButton;
    private JButton deleteButton;

    public MyDomainsPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        JLabel label = new JLabel("Tên miền của tôi", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(label, BorderLayout.NORTH);

        // Tạo bảng hiển thị tên miền
        String[] columnNames = {"Tên miền", "Giá", ""}; // Cột thứ ba để chứa checkbox
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Chỉ cho phép chỉnh sửa cột checkbox
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 2 ? Boolean.class : String.class; // Cột checkbox
            }
        };

        domainsTable = new JTable(tableModel);
        domainsTable.setRowHeight(40);
        domainsTable.setShowVerticalLines(false);
        domainsTable.setIntercellSpacing(new Dimension(0, 0));
        domainsTable.getTableHeader().setReorderingAllowed(false);
        domainsTable.getTableHeader().setPreferredSize(new Dimension(100, 40));
        domainsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        domainsTable.getTableHeader().setBackground(new Color(240, 240, 240));

        // Tùy chỉnh renderer cho các cột
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        domainsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // Cột giá

        // Tùy chỉnh cột checkbox
        TableColumn checkboxColumn = domainsTable.getColumnModel().getColumn(2);
        checkboxColumn.setHeaderValue(""); // Không hiển thị tiêu đề cho cột checkbox
        checkboxColumn.setMaxWidth(50); // Giới hạn chiều rộng của cột checkbox

        JScrollPane scrollPane = new JScrollPane(domainsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Panel chứa các nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));

        // Nút thanh toán
        payButton = new JButton("Thanh toán");
        payButton.setBackground(new Color(39, 174, 96));
        payButton.setForeground(Color.BLACK);
        payButton.setFont(new Font("Arial", Font.BOLD, 14));
        payButton.setEnabled(false); // Vô hiệu hóa ban đầu
        payButton.addActionListener(e -> handlePayment());
        buttonPanel.add(payButton);

        // Nút xóa
        deleteButton = new JButton("Xóa");
        deleteButton.setBackground(new Color(231, 76, 60));
        deleteButton.setForeground(Color.BLACK);
        deleteButton.setFont(new Font("Arial", Font.BOLD, 14));
        deleteButton.setEnabled(false); // Vô hiệu hóa ban đầu
        deleteButton.addActionListener(e -> handleDelete());
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Lắng nghe thay đổi trong bảng để kích hoạt nút
        tableModel.addTableModelListener(e -> updateButtonStates());

        // Tải dữ liệu từ SQL khi khởi tạo
        loadDomainsFromDatabase();
    }

    public void loadDomainsFromDatabase() {
        // Xóa dữ liệu cũ trong bảng
        tableModel.setRowCount(0);

        // Kết nối cơ sở dữ liệu và lấy dữ liệu
        String query = "SELECT * FROM cart";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String domainName = resultSet.getString("domain_name");
                double price = resultSet.getDouble("price");
                tableModel.addRow(new Object[]{domainName, String.format("%,.2f VND", price), false}); // Thêm dữ liệu vào bảng
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu từ cơ sở dữ liệu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateButtonStates() {
        // Kiểm tra nếu có ít nhất một hàng được đánh dấu
        boolean hasSelected = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((boolean) tableModel.getValueAt(i, 2)) {
                hasSelected = true;
                break;
            }
        }
        payButton.setEnabled(hasSelected);
        deleteButton.setEnabled(hasSelected);
    }

    private void handlePayment() {
        // Xử lý thanh toán
        StringBuilder selectedDomains = new StringBuilder("Tên miền được thanh toán:\n");
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((boolean) tableModel.getValueAt(i, 2)) {
                selectedDomains.append("- ").append(tableModel.getValueAt(i, 0)).append("\n");
            }
        }
        JOptionPane.showMessageDialog(this, selectedDomains.toString(), "Thanh toán", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleDelete() {
        // Xóa các hàng được đánh dấu
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            if ((boolean) tableModel.getValueAt(i, 2)) {
                String domainName = (String) tableModel.getValueAt(i, 0);
                deleteDomainFromDatabase(domainName);
                tableModel.removeRow(i);
            }
        }
        JOptionPane.showMessageDialog(this, "Đã xóa các tên miền được chọn.", "Xóa", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteDomainFromDatabase(String domainName) {
        String query = "DELETE FROM cart WHERE domain_name = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, domainName);
            statement.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa dữ liệu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}