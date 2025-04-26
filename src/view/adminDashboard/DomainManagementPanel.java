package view.adminDashboard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class DomainManagementPanel extends JPanel {

    private JTable fullDomainTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    private JTextField searchField;
    private JButton searchButton;
    private TableRowSorter<DefaultTableModel> sorter;

    public DomainManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Panel Tìm kiếm (Phía trên) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchField = new JTextField(25); // Tăng kích thước trường nhập
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchButton = new JButton("Tìm kiếm");
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH); // Thêm panel tìm kiếm vào phía Bắc

        // --- Bảng Dữ liệu (Ở giữa) ---
        String[] columnNames = {"ID", "Name", "Extension", "Giá", "Tình trạng"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            //Không thể thay đổi thông tin trong bàng
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Tạo JTable
        fullDomainTable = new JTable(tableModel);
        fullDomainTable.setFillsViewportHeight(true);
        fullDomainTable.setRowHeight(25);
        fullDomainTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fullDomainTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        fullDomainTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Chỉ cho phép chọn 1 dòng

        // Thêm TableRowSorter để lọc và sắp xếp
        sorter = new TableRowSorter<>(tableModel);
        fullDomainTable.setRowSorter(sorter);

        // ScrollPane sẽ được thêm vào CENTER khi nhấn nút "Hiện danh sách"
        scrollPane = new JScrollPane(fullDomainTable);
        add(scrollPane, BorderLayout.CENTER);

        loadDomainData();

        // --- Panel Nút Chức Năng (Phía dưới) ---
        JButton refreshButton = new JButton("Hiện danh sách");
        JButton deleteButton = new JButton("Xóa");

        //Điều chỉnh cho các nút
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 14);
        Dimension buttonSize = new Dimension(200, 40);

        refreshButton.setFont(buttonFont);
        deleteButton.setFont(buttonFont);

        refreshButton.setPreferredSize(buttonSize);
        deleteButton.setPreferredSize(buttonSize);

        // Panel chứa các nút, căn giữa
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // Thêm khoảng cách ngang
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(refreshButton);
        buttonPanel.add(deleteButton);

        // Thêm buttonPanel vào phía dưới giao diện
        add(buttonPanel, BorderLayout.SOUTH);


        // --- Gán Hành Động cho các Nút ---

        // Hành động khi nhấn nút Hiện danh sách (Làm mới)
        refreshButton.addActionListener(e -> {
            // Chỉ thêm scrollPane nếu nó chưa có parent (chưa được thêm vào panel)
            if (scrollPane.getParent() == null) {
                add(scrollPane, BorderLayout.CENTER);
                revalidate(); // Cập nhật layout
                repaint(); // Vẽ lại giao diện
            }
            // Xóa bộ lọc hiện tại và nội dung trường tìm kiếm khi làm mới
            searchField.setText("");
            sorter.setRowFilter(null);
            // Tải lại dữ liệu gốc
            loadDomainData();
        });

        // Hành động khi nhấn nút Xóa
        deleteButton.addActionListener(e -> {
            int selectedViewRow = fullDomainTable.getSelectedRow();
            if (selectedViewRow != -1) {
                // Chuyển đổi chỉ số hàng của view sang chỉ số hàng của model
                // Điều này quan trọng khi bảng đang được sắp xếp hoặc lọc
                int modelRow = fullDomainTable.convertRowIndexToModel(selectedViewRow);
                tableModel.removeRow(modelRow);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn một tên miền để xóa.",
                        "Chưa chọn tên miền",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // Hành động khi nhấn nút Tìm kiếm
        searchButton.addActionListener(e -> performSearch());

        // Hành động khi nhấn Enter trong trường tìm kiếm
        searchField.addActionListener(e -> performSearch());

    }

    //Thực hiện tìm kiếm/lọc bảng dựa trên nội dung của searchField.

    private void performSearch() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            // Nếu không có text, hiển thị tất cả (xóa bộ lọc)
            sorter.setRowFilter(null);
        } else {
            try {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(searchText), 1, 2));
            } catch (PatternSyntaxException pse) {
                System.err.println("Lỗi cú pháp Regex: " + pse.getMessage());
                // Có thể hiển thị thông báo lỗi cho người dùng nếu muốn
                JOptionPane.showMessageDialog(this,
                        "Lỗi trong biểu thức tìm kiếm.",
                        "Lỗi tìm kiếm",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadDomainData() {
        // Xóa dữ liệu cũ trước khi tải mới
        tableModel.setRowCount(0);

        // Dữ liệu mẫu mới phù hợp với cột
        Object[][] domainData = {
                {1, "google", ".com", "2025-09-14"},
                {2, "facebook", ".com", "2026-03-29"},
                {3, "vnexpress", ".vn", "2025-12-01"},
                {4, "youtube", ".com", "2027-02-15"},
                {5, "wikipedia", ".org", "2025-11-10"},
                {6, "zalo", ".vn", "2026-08-22"},
                {7, "openai", ".com", "2028-01-05"},
                {8, "github", ".com", "2026-04-11"},
                {9, "microsoft", ".com", "2029-07-19"},
                {10, "amazon", ".com", "2025-10-30"},
                {11, "dantri", ".com.vn", "2026-06-05"},
                {12, "tiktok", ".com", "2027-05-14"},
                {13, "shopee", ".vn", "2025-11-20"},
                {14, "education", ".edu.vn", "2026-09-01"},
                {15, "government", ".gov.vn", "2030-01-01"},
                {16, "myblog", ".net", "2025-07-07"},
                {17, "cooltech", ".io", "2026-10-18"},
                {18, "startup", ".ai", "2027-12-25"},
                {19, "mystore", ".shop", "2025-08-15"},
                {20, "devzone", ".dev", "2026-02-28"}
        };

        // Thêm từng dòng dữ liệu vào tableModel
        for (Object[] rowData : domainData) {
            tableModel.addRow(rowData);
        }
    }
}