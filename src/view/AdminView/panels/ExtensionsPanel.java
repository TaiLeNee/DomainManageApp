package view.AdminView.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import service.DomainExtensionService;

public class ExtensionsPanel extends JPanel {
    private static final Color BG_COLOR = new Color(245, 245, 245);
    private static final Color PRIMARY_COLOR = new Color(0, 102, 102);
    private static final Color ACCENT_COLOR = new Color(255, 153, 0);

    private DomainExtensionService extensionService;
    private JFrame parentFrame;

    public ExtensionsPanel(DomainExtensionService extensionService, JFrame parentFrame) {
        this.extensionService = extensionService;
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        initComponents();
    }

    private void initComponents() {
        // Bảng extensions
        String[] columnNames = { "ID", "Extension", "Giá mặc định", "Số lượng domain", "Thao tác" };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        // Hardcoded data for now - would be replaced with actual data from the service
        model.addRow(new Object[] { "1", ".com", "200.000 VND", "42", "" });
        model.addRow(new Object[] { "2", ".net", "180.000 VND", "28", "" });
        model.addRow(new Object[] { "3", ".org", "150.000 VND", "34", "" });
        model.addRow(new Object[] { "4", ".vn", "400.000 VND", "18", "" });
        model.addRow(new Object[] { "5", ".store", "300.000 VND", "12", "" });

        JTable table = new JTable(model);
        table.setRowHeight(40);

        // Button renderer cho cột thao tác
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
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

        JScrollPane scrollPane = new JScrollPane(table);

        // Panel công cụ
        JPanel toolPanel = new JPanel(new BorderLayout());
        toolPanel.setBackground(Color.WHITE);
        toolPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                new EmptyBorder(10, 20, 10, 20)));

        // Tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);

        JTextField searchField = new JTextField(25);
        searchField.setPreferredSize(new Dimension(300, 35));

        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.setBackground(PRIMARY_COLOR);
        searchButton.setForeground(Color.BLACK);
        searchButton.setFocusPainted(false);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.setPreferredSize(new Dimension(100, 35));

        searchPanel.add(searchField);
        searchPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        searchPanel.add(searchButton);

        JButton addButton = new JButton("Thêm phần mở rộng");
        addButton.setBackground(ACCENT_COLOR);
        addButton.setForeground(Color.BLACK);
        addButton.setFocusPainted(false);

        addButton.addActionListener(e -> showAddExtensionDialog());

        toolPanel.add(searchPanel, BorderLayout.WEST);
        toolPanel.add(addButton, BorderLayout.EAST);

        add(toolPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void showAddExtensionDialog() {
        // TODO: Hiển thị form thêm phần mở rộng mới
        JOptionPane.showMessageDialog(parentFrame,
                "Chức năng thêm phần mở rộng sẽ được triển khai sau.",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
}