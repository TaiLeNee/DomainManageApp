package view.adminDashboard;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class OrderManagementPanel extends JPanel {
    private JTable domainTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;

    public OrderManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Tạo TableModel với các cột
        String[] columnNames = {"ID", "Người mua", "Tên miền", "Trạng thái", "Ngày hết hạn", "Giá"};
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
        JButton approveButton = new JButton("Duyệt");
        approveButton.setEnabled(false);
        JButton deleteButton = new JButton("Xóa");
        deleteButton.setEnabled(false);

        //Điều chỉnh cho các nút
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 14);
        Dimension buttonSize = new Dimension(200, 40);

        refreshButton.setFont(buttonFont);
        approveButton.setFont(buttonFont);
        deleteButton.setFont(buttonFont);

        refreshButton.setPreferredSize(buttonSize);
        approveButton.setPreferredSize(buttonSize);
        deleteButton.setPreferredSize(buttonSize);

        // Panel chứa các nút, căn giữa
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(refreshButton);
        buttonPanel.add(approveButton);
        buttonPanel.add(deleteButton);

        // Thêm buttonPanel vào phía dưới giao diện
        add(buttonPanel, BorderLayout.SOUTH);

        domainTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // Kiểm tra xem sự kiện có phải là kết thúc của một chuỗi thay đổi không
                // và có hàng nào được chọn không
                if (!e.getValueIsAdjusting()) {
                    boolean rowIsSelected = domainTable.getSelectedRow() != -1;
                    approveButton.setEnabled(rowIsSelected);
                    deleteButton.setEnabled(rowIsSelected);
                }
            }
        });

        // Hành động khi nhấn nút Làm mới
        refreshButton.addActionListener(e -> {
            if (scrollPane.getParent() == null) {
                add(scrollPane, BorderLayout.CENTER);
                revalidate();
                repaint();
            }
            loadTestData();
            // Sau khi tải lại dữ liệu, lựa chọn có thể bị mất, listener ở trên sẽ tự động disable các nút
        });

        // Hành động khi nhấn nút Duyệt
        approveButton.addActionListener(e -> {
            int selectedRow = domainTable.getSelectedRow();
            // Không cần kiểm tra selectedRow != -1 nữa vì nút đã bị disable nếu không có dòng nào được chọn
            // if (selectedRow != -1) { ... } // Có thể bỏ điều kiện này
            Object orderId = tableModel.getValueAt(selectedRow, 0);
            String domainName = (String) tableModel.getValueAt(selectedRow, 2);
            tableModel.setValueAt("Đã duyệt", selectedRow, 3);
            JOptionPane.showMessageDialog(this, "Đã duyệt đơn hàng ID: " + orderId + " cho tên miền: " + domainName);
            // Gọi Controller/Service
        });

        // Hành động khi nhấn nút Xóa
        deleteButton.addActionListener(e -> {
            int selectedRow = domainTable.getSelectedRow();
            // Không cần kiểm tra selectedRow != -1 nữa vì nút đã bị disable nếu không có dòng nào được chọn
            // if (selectedRow != -1) { ... } // Có thể bỏ điều kiện này
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn xóa đơn hàng này không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                Object orderId = tableModel.getValueAt(selectedRow, 0);
                tableModel.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, "Đã xóa đơn hàng ID: " + orderId);
                // Gọi Controller/Service
            }
            // } else { // Không cần khối else này nữa
            //    JOptionPane.showMessageDialog(this, "Vui lòng chọn một đơn hàng để xóa.");
            // }
        });
    }

    private void loadTestData() {
        tableModel.setRowCount(0);

        Object[][] sampleData = {
                {1, "Nguyễn Văn An", "example.com", "Đang chờ duyệt", "2026-05-15", 250000.0},
                {2, "Trần Thị Bình", "mydomain.vn", "Đã duyệt", "2025-12-31", 300000.0},
                {3, "Lê Hoàng Cường", "testsite.org", "Đã duyệt", "2026-01-20", 150000.0},
                {4, "Phạm Thị Dung", "anotherdomain.net", "Hết hạn", "2025-03-01", 280000.0},
                {5, "Hoàng Văn Em", "webshop.store", "Đang chờ duyệt", "2026-11-10", 500000.0},
                {6, "Vũ Thị Giang", "blogsite.info", "Đã duyệt", "2027-02-28", 180000.0}
        };

        for (Object[] rowData : sampleData) {
            tableModel.addRow(rowData);
        }
    }
}
