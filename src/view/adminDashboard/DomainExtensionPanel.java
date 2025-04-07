package view.adminDashboard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DomainExtensionPanel extends JPanel {

    private JTable domainTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;

    public DomainExtensionPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Tạo TableModel với các cột
        String[] columnNames = {"ID", "Extension", "Giá", "Trạng thái"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            //Không thể thay đổi thông tin trong bàng
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Tạo JTable nhưng KHÔNG thêm vào giao diện lúc đầu
        domainTable = new JTable(tableModel);
        domainTable.setFillsViewportHeight(true);
        domainTable.setRowHeight(25);
        domainTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        domainTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        scrollPane = new JScrollPane(domainTable);

        // Tạo các nút chức năng
        JButton refreshButton = new JButton("Hiện danh sách");
        JButton addButton = new JButton("Thêm");
        JButton deleteButton = new JButton("Xóa");

        //Điều chỉnh cho các nút
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 14);
        Dimension buttonSize = new Dimension(200, 40);

        refreshButton.setFont(buttonFont);
        addButton.setFont(buttonFont);
        deleteButton.setFont(buttonFont);

        refreshButton.setPreferredSize(buttonSize);
        addButton.setPreferredSize(buttonSize);
        deleteButton.setPreferredSize(buttonSize);

        // Panel chứa các nút, căn giữa
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);

        // Thêm buttonPanel vào phía dưới giao diện
        add(buttonPanel, BorderLayout.SOUTH);

        // Hành động khi nhấn nút Làm mới
        refreshButton.addActionListener(e -> {
            if (scrollPane.getParent() == null) {
                add(scrollPane, BorderLayout.CENTER);
                revalidate(); // cập nhật giao diện
                repaint();
            }
            loadTestData();
        });

        // Hành động khi nhấn nút Thêm
        addButton.addActionListener(e -> {
            showAddDialog();
        });

        // Hành động khi nhấn nút Xóa
        deleteButton.addActionListener(e -> {
            int selectedRow = domainTable.getSelectedRow();
            if (selectedRow != -1) {
                tableModel.removeRow(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để xóa.");
            }
        });
    }

    private void loadTestData() {
        tableModel.setRowCount(0);

        Object[][] sampleData = {
                {1, ".com", 250000, "Active"},
                {2, ".vn", 750000, "Active"},
                {3, ".net", 300000, "Active"},
                {4, ".org", 350000, "Available"},
                {5, ".io", 950000, "Active"},
                {6, ".ai", 1500000, "Active"},
                {7, ".info", 180000, "Available"},
                {8, ".xyz", 30000, "Active"},
                {9, ".shop", 80000, "Active"},
                {10, ".tech", 120000, "Active"},
                {11, ".store", 90000, "Available"},
                {12, ".online", 50000, "Active"},
                {13, ".dev", 400000, "Active"},
                {14, ".app", 450000, "Active"},
                {15, ".me", 320000, "Available"},
                {16, ".co", 500000, "Active"},
                {17, ".asia", 400000, "Available"},
                {18, ".us", 200000, "Active"},
                {19, ".ca", 380000, "Active"},
                {20, ".uk", 220000, "Available"}
        };

        for (Object[] rowData : sampleData) {
            tableModel.addRow(rowData);
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm Domain Extension", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        // Panel chứa các field
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel extensionLabel = new JLabel("Extension:");
        JTextField extensionField = new JTextField();

        JLabel priceLabel = new JLabel("Giá:");
        JTextField priceField = new JTextField();

        JLabel statusLabel = new JLabel("Trạng thái:");
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"Active", "Available", "Expired"});

        extensionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        extensionField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        priceField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        formPanel.add(extensionLabel);
        formPanel.add(extensionField);
        formPanel.add(priceLabel);
        formPanel.add(priceField);
        formPanel.add(statusLabel);
        formPanel.add(statusComboBox);

        dialog.add(formPanel, BorderLayout.CENTER);

        // Panel chứa các nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton saveButton = new JButton("Lưu");
        JButton cancelButton = new JButton("Hủy");

        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Xử lý nút Lưu
        saveButton.addActionListener(ev -> {
            String extension = extensionField.getText().trim();
            String priceText = priceField.getText().trim();
            String status = (String) statusComboBox.getSelectedItem();

            if (extension.isEmpty() || priceText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ thông tin.");
                return;
            }

            try {
                int price = Integer.parseInt(priceText);
                int newId = tableModel.getRowCount() + 1; // ID tự tăng
                tableModel.addRow(new Object[]{newId, extension, price, status});
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Giá phải là một số nguyên.");
            }
        });

        // Xử lý nút Hủy
        cancelButton.addActionListener(ev -> dialog.dispose());

        dialog.setVisible(true);
    }
}
