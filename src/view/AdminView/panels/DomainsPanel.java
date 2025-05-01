package view.AdminView.panels;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Domain;
import repository.DomainRepository;

public class DomainsPanel extends JPanel {
    private static final Color BG_COLOR = new Color(245, 245, 245);
    private static final Color PRIMARY_COLOR = new Color(0, 102, 102);
    private static final Color ACCENT_COLOR = new Color(255, 153, 0);
    private DomainRepository domainRepository;
    private JTable domainsTable;
    private DefaultTableModel tableModel;
    private JFrame parentFrame;

    public DomainsPanel(DomainRepository domainRepository, JFrame parentFrame) {
        this.domainRepository = domainRepository;
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        initComponents();
    }

    private void initComponents() {
        // Panel công cụ
        JPanel toolPanel = new JPanel(new BorderLayout());
        toolPanel.setBackground(Color.WHITE);
        toolPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                new EmptyBorder(10, 20, 10, 20)));

        // Panel tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchPanel.setOpaque(false);

        JTextField searchField = new JTextField(25);
        searchField.setPreferredSize(new Dimension(300, 35));

        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.setBackground(PRIMARY_COLOR);
        searchButton.setForeground(Color.BLACK);
        searchButton.setFocusPainted(false);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.setPreferredSize(new Dimension(100, 35));

        searchButton.addActionListener(e -> searchDomains(searchField.getText()));

        searchPanel.add(searchField);
        searchPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        searchPanel.add(searchButton);

        // Panel nút thêm
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton addButton = new JButton("Thêm Tên miền");
        addButton.setBackground(ACCENT_COLOR);
        addButton.setForeground(Color.BLACK);
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.setPreferredSize(new Dimension(150, 35));

        addButton.addActionListener(e -> showAddDomainDialog());

        buttonPanel.add(addButton);

        toolPanel.add(searchPanel, BorderLayout.WEST);
        toolPanel.add(buttonPanel, BorderLayout.EAST);

        // Bảng domains
        String[] columnNames = { "ID", "Tên", "Phần mở rộng", "Giá", "Trạng thái", "Ngày hết hạn", "Thao tác" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Chỉ cho phép chỉnh sửa cột thao tác
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

        // Render cho cột status
        domainsTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if ("Đã thuê".equals(value)) {
                    c.setForeground(new Color(231, 76, 60)); // Red
                } else if ("Khả dụng".equals(value)) {
                    c.setForeground(new Color(39, 174, 96)); // Green
                } else {
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        });

        // Button renderer cho cột thao tác
        domainsTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                actionsPanel.setOpaque(false);

                JButton editButton = new JButton("Sửa");
                editButton.setPreferredSize(new Dimension(60, 30));
                editButton.setBackground(new Color(52, 152, 219));
                editButton.setForeground(Color.BLACK);
                editButton.setFocusPainted(false);

                JButton deleteButton = new JButton("Xóa");
                deleteButton.setPreferredSize(new Dimension(60, 30));
                deleteButton.setBackground(new Color(231, 76, 60));
                deleteButton.setForeground(Color.BLACK);
                deleteButton.setFocusPainted(false);

                actionsPanel.add(editButton);
                actionsPanel.add(deleteButton);

                return actionsPanel;
            }
        });

        // Mouse listener cho các nút trong bảng
        domainsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = domainsTable.getColumnModel().getColumnIndexAtX(e.getX());
                int row = e.getY() / domainsTable.getRowHeight();

                if (row < domainsTable.getRowCount() && row >= 0 && column == 6) {
                    // Lấy toạ độ của ô được click
                    Rectangle rect = domainsTable.getCellRect(row, column, false);

                    // Tính toán vị trí tương đối trong ô
                    int xInCell = e.getX() - rect.x;

                    // Nếu click gần phía trái (nút Edit)
                    if (xInCell <= 65) {
                        int domainId = Integer.parseInt(domainsTable.getValueAt(row, 0).toString());
                        editDomain(domainId);
                    }
                    // Nếu click gần phía phải (nút Delete)
                    else if (xInCell > 65) {
                        int domainId = Integer.parseInt(domainsTable.getValueAt(row, 0).toString());
                        deleteDomain(domainId);
                    }
                }
            }
        });

        // Scroll pane cho table
        JScrollPane scrollPane = new JScrollPane(domainsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Thêm vào panel chính
        add(toolPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Load dữ liệu
        loadDomainData();
    }

    public void loadDomainData() {
        // Xóa dữ liệu cũ
        tableModel.setRowCount(0);

        try {
            List<Domain> domains = domainRepository.getAllDomains();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            DecimalFormat priceFormat = new DecimalFormat("#,### VND");

            for (Domain domain : domains) {
                // Định dạng ngày hết hạn
                String expiryDateStr = "";
                if (domain.getExpiryDate() != null) {
                    Date expiryDate = Date.from(domain.getExpiryDate().atZone(ZoneId.systemDefault()).toInstant());
                    expiryDateStr = dateFormat.format(expiryDate);
                }

                // Định dạng giá
                String priceStr = priceFormat.format(domain.getPrice());

                tableModel.addRow(new Object[] {
                        domain.getId(),
                        domain.getName(),
                        domain.getExtension(),
                        priceStr,
                        domain.getStatus(),
                        expiryDateStr,
                        ""
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Không thể tải dữ liệu tên miền: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);

            // Nếu có lỗi, hiển thị thông báo trong bảng
            tableModel.addRow(new Object[] { "", "", "", "", "Không thể tải dữ liệu", "", "" });
        }
    }

    private void searchDomains(String searchTerm) {
        // Xóa dữ liệu cũ
        tableModel.setRowCount(0);

        try {
            List<Domain> domains = domainRepository.searchDomains(searchTerm);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            DecimalFormat priceFormat = new DecimalFormat("#,### VND");

            for (Domain domain : domains) {
                // Định dạng ngày hết hạn
                String expiryDateStr = "";
                if (domain.getExpiryDate() != null) {
                    Date expiryDate = Date.from(domain.getExpiryDate().atZone(ZoneId.systemDefault()).toInstant());
                    expiryDateStr = dateFormat.format(expiryDate);
                }

                // Định dạng giá
                String priceStr = priceFormat.format(domain.getPrice());

                tableModel.addRow(new Object[] {
                        domain.getId(),
                        domain.getName(),
                        domain.getExtension(),
                        priceStr,
                        domain.getStatus(),
                        expiryDateStr,
                        ""
                });
            }

            if (domains.isEmpty()) {
                tableModel.addRow(new Object[] { "", "", "", "", "Không tìm thấy kết quả", "", "" });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Lỗi tìm kiếm: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            tableModel.addRow(new Object[] { "", "", "", "", "Không thể tải dữ liệu", "", "" });
        }
    }

    private void showAddDomainDialog() {
        // TODO: Hiển thị form thêm mới tên miền
        JOptionPane.showMessageDialog(parentFrame,
                "Chức năng thêm tên miền mới sẽ được triển khai sau.",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void editDomain(int domainId) {
        try {
            Domain domain = domainRepository.getDomainById(domainId);
            if (domain != null) {
                // Tạo các trường nhập liệu
                JTextField nameField = new JTextField(domain.getName());
                JTextField extensionField = new JTextField(domain.getExtension());
                JTextField priceField = new JTextField(String.valueOf(domain.getPrice()));
                JComboBox<String> statusComboBox = new JComboBox<>(new String[] { "Available", "Rented", "Reserved" });
                statusComboBox.setSelectedItem(domain.getStatus());

                // Tạo trường chọn ngày hết hạn
                JSpinner expiryDateSpinner = new JSpinner(new SpinnerDateModel());
                JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(expiryDateSpinner, "dd/MM/yyyy");
                expiryDateSpinner.setEditor(dateEditor);
                expiryDateSpinner.setEnabled("Rented".equals(domain.getStatus()));

                // Đặt giá trị ngày hết hạn nếu có
                if (domain.getExpiryDate() != null) {
                    expiryDateSpinner.setValue(Date.from(domain.getExpiryDate().atZone(ZoneId.systemDefault()).toInstant()));
                }

                // Thêm sự kiện thay đổi trạng thái
                statusComboBox.addActionListener(e -> {
                    String selectedStatus = (String) statusComboBox.getSelectedItem();
                    expiryDateSpinner.setEnabled("Rented".equals(selectedStatus));
                });

                // Tạo panel chứa các trường
                JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
                panel.add(new JLabel("Tên:"));
                panel.add(nameField);
                panel.add(new JLabel("Phần mở rộng:"));
                panel.add(extensionField);
                panel.add(new JLabel("Giá:"));
                panel.add(priceField);
                panel.add(new JLabel("Trạng thái:"));
                panel.add(statusComboBox);
                panel.add(new JLabel("Ngày hết hạn:"));
                panel.add(expiryDateSpinner);

                // Hiển thị dialog
                int result = JOptionPane.showConfirmDialog(parentFrame, panel, "Chỉnh sửa tên miền", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    // Lấy dữ liệu từ form
                    String newName = nameField.getText().trim();
                    String newExtension = extensionField.getText().trim();
                    double newPrice;
                    try {
                        newPrice = Double.parseDouble(priceField.getText().trim());
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(parentFrame, "Giá phải là một số hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String newStatus = (String) statusComboBox.getSelectedItem();
                    Date expiryDate = null;

                    if ("Rented".equals(newStatus)) {
                        expiryDate = (Date) expiryDateSpinner.getValue();
                        if (expiryDate.before(new Date())) {
                            JOptionPane.showMessageDialog(parentFrame, "Ngày hết hạn phải lớn hơn ngày hiện tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    // Cập nhật domain
                    domain.setName(newName);
                    domain.setExtension(newExtension);
                    domain.setPrice(newPrice);
                    domain.setStatus(newStatus);
                    if (expiryDate != null) {
                        domain.setExpiryDate(expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay());                    } else {
                        domain.setExpiryDate(null);
                    }
                    try {
                        domainRepository.save(domain);
                        JOptionPane.showMessageDialog(parentFrame, "Cập nhật tên miền thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        loadDomainData(); // Refresh dữ liệu bảng
                    } catch (SQLException | IllegalArgumentException e) {
                        JOptionPane.showMessageDialog(parentFrame, "Lỗi khi cập nhật tên miền: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(parentFrame, "Không tìm thấy thông tin tên miền!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame, "Không thể tải thông tin tên miền: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDomain(int domainId) {
        try {
            int confirm = JOptionPane.showConfirmDialog(parentFrame,
                    "Bạn chắc chắn muốn xóa tên miền này?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION);
    
            if (confirm == JOptionPane.YES_OPTION) {
                // Gọi repository để xóa domain
                boolean result = domainRepository.deleteDomain(domainId);
                if (result) {
                    JOptionPane.showMessageDialog(parentFrame,
                            "Đã xóa tên miền thành công!",
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    
                    // Refresh data
                    loadDomainData();
                } else {
                    JOptionPane.showMessageDialog(parentFrame,
                            "Không thể xóa tên miền! Vui lòng kiểm tra lại dữ liệu.",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Lỗi khi xóa tên miền: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}