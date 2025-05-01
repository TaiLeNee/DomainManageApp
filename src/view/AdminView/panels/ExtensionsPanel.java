package view.AdminView.panels;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
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
        String[] columnNames = { "Extension", "Mô tả", "Giá mặc định" };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        // Lấy dữ liệu từ cơ sở dữ liệu
        try {
            List<String[]> extensions = extensionService.getAllDomainExtensions();
            DecimalFormat priceFormat = new DecimalFormat("#,###");
            for (String[] extension : extensions) {
                String formattedPrice = priceFormat.format(Double.parseDouble(extension[2].replace(" VND", "").replace(",", ""))) + " VND";
                model.addRow(new Object[] { extension[0], extension[1], formattedPrice });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu từ cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        JTable table = new JTable(model);
        table.setRowHeight(40);

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

        // Thêm sự kiện cho nút Tìm kiếm
        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim().toLowerCase();
            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập từ khóa để tìm kiếm.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Lọc dữ liệu trong bảng
            DefaultTableModel filteredModel = new DefaultTableModel(columnNames, 0);
            for (int i = 0; i < model.getRowCount(); i++) {
                String extension = model.getValueAt(i, 0).toString().toLowerCase();
                String description = model.getValueAt(i, 1).toString().toLowerCase();
                if (extension.contains(keyword) || description.contains(keyword)) {
                    filteredModel.addRow(new Object[] {
                        model.getValueAt(i, 0),
                        model.getValueAt(i, 1),
                        model.getValueAt(i, 2)
                    });
                }
            }

            // Cập nhật bảng với dữ liệu đã lọc
            table.setModel(filteredModel);
        });

        searchPanel.add(searchField);
        searchPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        searchPanel.add(searchButton);

        toolPanel.add(searchPanel, BorderLayout.WEST);

        // Panel chứa các nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton addButton = new JButton("Thêm");
        addButton.setBackground(ACCENT_COLOR);
        addButton.setForeground(Color.BLACK);
        addButton.setFocusPainted(false);
        addButton.setPreferredSize(new Dimension(100, 35));
        addButton.addActionListener(e -> showAddExtensionDialog());

        JButton editButton = new JButton("Sửa");
        editButton.setBackground(new Color(52, 152, 219));
        editButton.setForeground(Color.BLACK);
        editButton.setFocusPainted(false);
        editButton.setPreferredSize(new Dimension(100, 35));
        editButton.addActionListener(e -> handleEditAction(table, model));

        JButton deleteButton = new JButton("Xóa");
        deleteButton.setBackground(new Color(231, 76, 60));
        deleteButton.setForeground(Color.BLACK);
        deleteButton.setFocusPainted(false);
        deleteButton.setPreferredSize(new Dimension(100, 35));
        deleteButton.addActionListener(e -> handleDeleteAction(table, model));

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        toolPanel.add(buttonPanel, BorderLayout.EAST);

        add(toolPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void showAddExtensionDialog() {
        JTextField extensionField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField descriptionField = new JTextField();
    
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Extension:"));
        panel.add(extensionField);
        panel.add(new JLabel("Giá mặc định:"));
        panel.add(priceField);
        panel.add(new JLabel("Mô tả:"));
        panel.add(descriptionField);
    
        int result = JOptionPane.showConfirmDialog(parentFrame, panel, "Thêm phần mở rộng", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String extension = extensionField.getText();
            double price;
            try {
                price = Double.parseDouble(priceField.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parentFrame, "Giá phải là một số hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String description = descriptionField.getText();
    
            boolean success = extensionService.saveExtension(extension, price, description);
            if (success) {
                DefaultTableModel model = (DefaultTableModel) ((JTable) ((JScrollPane) getComponent(1)).getViewport().getView()).getModel();
                DecimalFormat priceFormat = new DecimalFormat("#,###");
                model.addRow(new Object[]{extension, description, priceFormat.format(price) + " VND"});
            } else {
                JOptionPane.showMessageDialog(parentFrame, "Lỗi khi thêm phần mở rộng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEditAction(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row != -1) {
            String extension = (String) model.getValueAt(row, 0);
            String description = (String) model.getValueAt(row, 1);
            String priceStr = (String) model.getValueAt(row, 2);
            double price = Double.parseDouble(priceStr.replace(" VND", "").replace(",", ""));
    
            JTextField descriptionField = new JTextField(description);
            JTextField priceField = new JTextField(String.valueOf(price));
    
            JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
            panel.add(new JLabel("Mô tả:"));
            panel.add(descriptionField);
            panel.add(new JLabel("Giá mặc định:"));
            panel.add(priceField);
    
            int result = JOptionPane.showConfirmDialog(parentFrame, panel, "Sửa phần mở rộng", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    double newPrice = Double.parseDouble(priceField.getText());
                    String newDescription = descriptionField.getText();
    
                    boolean success = extensionService.saveExtension(extension, newPrice, newDescription);
                    if (success) {
                        DecimalFormat priceFormat = new DecimalFormat("#,###");
                        model.setValueAt(newDescription, row, 1);
                        model.setValueAt(priceFormat.format(newPrice) + " VND", row, 2);
                    } else {
                        JOptionPane.showMessageDialog(parentFrame, "Lỗi khi sửa phần mở rộng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(parentFrame, "Giá phải là một số hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(parentFrame, "Vui lòng chọn một hàng để sửa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleDeleteAction(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row != -1) {
            String extension = (String) model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(parentFrame, "Bạn có chắc chắn muốn xóa phần mở rộng này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = extensionService.deleteExtension(extension);
                if (success) {
                    model.removeRow(row);
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "Lỗi khi xóa phần mở rộng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(parentFrame, "Vui lòng chọn một hàng để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }
}