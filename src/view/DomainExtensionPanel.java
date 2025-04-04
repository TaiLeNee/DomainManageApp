package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DomainExtensionPanel extends JPanel {

    private JTable domainTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;

    public DomainExtensionPanel() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Tạo TableModel với các cột
        String[] columnNames = {"ID", "Extension", "Giá", "Trạng thái"};
        tableModel = new DefaultTableModel(columnNames, 0);

        // Tạo JTable
        domainTable = new JTable(tableModel);
        domainTable.setFillsViewportHeight(true);
        domainTable.setRowHeight(25);
        domainTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        domainTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        scrollPane = new JScrollPane(domainTable); // Đặt JTable vào trong JScrollPane
        add(scrollPane, gbc); // Thêm JScrollPane vào trong JPanel

        // Nút làm mới
        JButton refreshButton = new JButton("Làm mới (Test Data)");
        refreshButton.addActionListener(e -> loadTestData());

        // Panel chứa nút làm mới
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(refreshButton);

        // Cấu hình GridBagConstraints cho bottomPanel
        gbc.gridy = 1;
        gbc.weighty = 0; // Không chiếm không gian dọc thừa
        gbc.fill = GridBagConstraints.HORIZONTAL; // Lấp đầy chiều ngang
        add(bottomPanel, gbc);
    }

    private void loadTestData() {
        tableModel.setRowCount(0);

        Object[][] sampleData = {
                {1, ".com", 250000, "active"},
                {2, ".vn", 750000, "active"},
                {3, ".net", 300000, "active"},
                {4, ".org", 350000, "available"},
                {5, ".io", 950000, "active"},
                {6, ".ai", 1500000, "active"},
                {7, ".info", 180000, "available"},
                {8, ".xyz", 30000, "active"},
                {9, ".shop", 80000, "active"},
                {10, ".tech", 120000, "active"},
                {11, ".store", 90000, "available"},
                {12, ".online", 50000, "active"},
                {13, ".dev", 400000, "active"},
                {14, ".app", 450000, "active"},
                {15, ".me", 320000, "available"},
                {16, ".co", 500000, "active"},
                {17, ".asia", 400000, "available"},
                {18, ".us", 200000, "active"},
                {19, ".ca", 380000, "active"},
                {20, ".uk", 220000, "available"}
        };

        for (Object[] rowData : sampleData) {
            tableModel.addRow(rowData);
        }
    }
}