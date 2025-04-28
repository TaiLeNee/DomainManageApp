package view.AdminView.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ReportsPanel extends JPanel {
    private static final Color BG_COLOR = new Color(245, 245, 245);
    private static final Color PRIMARY_COLOR = new Color(0, 102, 102);

    private JComboBox<String> periodCombo;
    private JComboBox<String> reportTypeCombo;
    private JLabel chartLabel;
    private JPanel chartPanel;
    private JPanel dataPanel;

    public ReportsPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        initComponents();
    }

    private void initComponents() {
        // Panel chọn loại báo cáo
        JPanel filterPanel = new JPanel();
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                new EmptyBorder(15, 20, 15, 20)));

        JLabel periodLabel = new JLabel("Khoảng thời gian:");
        periodCombo = new JComboBox<>(
                new String[] { "7 ngày qua", "30 ngày qua", "3 tháng qua", "6 tháng qua", "1 năm qua" });

        JLabel reportTypeLabel = new JLabel("Loại báo cáo:");
        reportTypeCombo = new JComboBox<>(new String[] { "Doanh thu", "Đơn hàng", "Tên miền mới" });

        JButton generateButton = new JButton("Tạo báo cáo");
        generateButton.setBackground(PRIMARY_COLOR);
        generateButton.setForeground(Color.WHITE);

        generateButton.addActionListener(e -> generateReport());

        filterPanel.add(periodLabel);
        filterPanel.add(periodCombo);
        filterPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        filterPanel.add(reportTypeLabel);
        filterPanel.add(reportTypeCombo);
        filterPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        filterPanel.add(generateButton);

        // Panel báo cáo chính
        JPanel reportsContentPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        reportsContentPanel.setBackground(BG_COLOR);
        reportsContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Panel hiển thị biểu đồ
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));

        chartLabel = new JLabel("Biểu đồ doanh thu 7 ngày qua", JLabel.CENTER);
        chartLabel.setFont(new Font("Arial", Font.BOLD, 16));
        chartLabel.setBorder(new EmptyBorder(15, 0, 15, 0));

        JPanel chartPlaceholder = new JPanel();
        chartPlaceholder.setBackground(new Color(250, 250, 250));
        chartPlaceholder.setPreferredSize(new Dimension(0, 300));
        chartPlaceholder.add(new JLabel("(Biểu đồ sẽ được hiển thị tại đây)", JLabel.CENTER));

        chartPanel.add(chartLabel, BorderLayout.NORTH);
        chartPanel.add(chartPlaceholder, BorderLayout.CENTER);

        // Bảng dữ liệu mẫu - sẽ được thay thế bằng dữ liệu thực từ db
        dataPanel = createDataTable();

        reportsContentPanel.add(chartPanel);
        reportsContentPanel.add(dataPanel);

        add(filterPanel, BorderLayout.NORTH);
        add(new JScrollPane(reportsContentPanel), BorderLayout.CENTER);
    }

    private JPanel createDataTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));

        // Header with title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel("Dữ liệu chi tiết");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Table
        String[] columns = { "Ngày", "Số đơn hàng", "Doanh thu", "Tên miền mới", "Tỷ lệ" };
        Object[][] data = {
                { "15/04/2023", "8", "1.350.000 VND", "5", "62.5%" },
                { "16/04/2023", "5", "950.000 VND", "3", "60%" },
                { "17/04/2023", "10", "1.850.000 VND", "7", "70%" },
                { "18/04/2023", "6", "1.200.000 VND", "4", "66.7%" },
                { "19/04/2023", "9", "1.750.000 VND", "6", "66.7%" },
                { "20/04/2023", "7", "1.400.000 VND", "5", "71.4%" },
                { "21/04/2023", "12", "2.250.000 VND", "8", "66.7%" }
        };

        DefaultTableModel model = new DefaultTableModel(data, columns);
        JTable table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setRowHeight(35);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(240, 240, 240));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void generateReport() {
        String period = (String) periodCombo.getSelectedItem();
        String reportType = (String) reportTypeCombo.getSelectedItem();

        // Cập nhật title biểu đồ
        chartLabel.setText("Biểu đồ " + reportType.toLowerCase() + " " + period.toLowerCase());

        // TODO: Gọi service để lấy dữ liệu và vẽ biểu đồ

        JOptionPane.showMessageDialog(this,
                "Đã tạo báo cáo " + reportType + " cho " + period,
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
}