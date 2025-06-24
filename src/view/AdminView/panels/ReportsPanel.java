package view.AdminView.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;
import java.util.List;
import java.util.Map;
import service.ReportService;

public class ReportsPanel extends JPanel {
    // Enhanced modern color palette
    private static final Color BG_COLOR = new Color(243, 246, 249);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(79, 70, 229); // Indigo
    private static final Color SECONDARY_COLOR = new Color(99, 102, 241); // Light indigo
    private static final Color SUCCESS_COLOR = new Color(16, 185, 129); // Emerald
    private static final Color WARNING_COLOR = new Color(245, 158, 11); // Amber
    private static final Color DANGER_COLOR = new Color(239, 68, 68); // Red
    private static final Color INFO_COLOR = new Color(59, 130, 246); // Blue
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color TEXT_MUTED = new Color(156, 163, 175);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 8);

    // Enhanced typography
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 13);

    private JComboBox<String> periodCombo;
    private JComboBox<String> reportTypeCombo;
    private JPanel metricsPanel;
    private JPanel chartPanel;
    private JPanel dataPanel;
    private NumberFormat currencyFormat;
    private Random random;
    private ReportService reportService;
    private Map<String, Object> currentChartData;

    public ReportsPanel() {
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.random = new Random();
        this.reportService = new ReportService();
        
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        
        initComponents();
        generateReport(); // Load initial data
    }

    private void initComponents() {
        // Create header
        JPanel headerPanel = createHeaderPanel();
        
        // Create main content với spacing tốt hơn
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(0, 0, 30, 0)); // Thêm bottom padding

        // Metrics cards với wrapper để control spacing
        JPanel metricsWrapper = new JPanel(new BorderLayout());
        metricsWrapper.setOpaque(false);
        metricsWrapper.setBorder(new EmptyBorder(0, 0, 35, 0)); // Tăng bottom spacing
        
        metricsPanel = createMetricsPanel();
        metricsWrapper.add(metricsPanel, BorderLayout.CENTER);
        
        // Charts and data panels với spacing tốt hơn
        JPanel chartsDataPanel = new JPanel(new GridLayout(1, 2, 35, 0)); // Tăng spacing
        chartsDataPanel.setOpaque(false);
        
        chartPanel = createChartPanel();
        dataPanel = createEnhancedDataPanel();
        
        chartsDataPanel.add(chartPanel);
        chartsDataPanel.add(dataPanel);

        mainContent.add(metricsWrapper);
        mainContent.add(chartsDataPanel);

        // Wrapper tổng thể
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setOpaque(false);
        contentWrapper.add(headerPanel, BorderLayout.NORTH);
        contentWrapper.add(mainContent, BorderLayout.CENTER);

        // Scroll pane để tránh bị cắt content
        JScrollPane scrollPane = new JScrollPane(contentWrapper);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(BG_COLOR);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 30, 0));

        // Title section
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Báo cáo & Thống kê");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Phân tích dữ liệu kinh doanh và hiệu suất hệ thống");
        subtitleLabel.setFont(FONT_REGULAR);
        subtitleLabel.setForeground(TEXT_SECONDARY);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        titlePanel.add(subtitleLabel);

        // Filter controls
        JPanel filtersPanel = createFiltersPanel();

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(filtersPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createFiltersPanel() {
        JPanel filtersPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));

                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));

                g2.dispose();
            }
        };
        filtersPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        filtersPanel.setOpaque(false);

        // Period selector
        JLabel periodLabel = new JLabel("Thời gian:");
        periodLabel.setFont(FONT_LABEL);
        periodLabel.setForeground(TEXT_SECONDARY);

        periodCombo = new JComboBox<>(new String[] { 
            "7 ngày qua", "30 ngày qua", "3 tháng qua", "6 tháng qua", "1 năm qua" 
        });
        styleComboBox(periodCombo);

        // Report type selector
        JLabel reportTypeLabel = new JLabel("Loại báo cáo:");
        reportTypeLabel.setFont(FONT_LABEL);
        reportTypeLabel.setForeground(TEXT_SECONDARY);

        reportTypeCombo = new JComboBox<>(new String[] { 
            "Doanh thu", "Đơn hàng", "Tên miền", "Khách hàng" 
        });
        styleComboBox(reportTypeCombo);

        // Generate button
        JButton generateBtn = createModernButton("Tạo báo cáo", PRIMARY_COLOR, true);
        generateBtn.addActionListener(e -> generateReport());

        // Export button
        JButton exportBtn = createModernButton("Xuất báo cáo", SECONDARY_COLOR, false);
        exportBtn.addActionListener(e -> exportReport());

        filtersPanel.add(periodLabel);
        filtersPanel.add(periodCombo);
        filtersPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        filtersPanel.add(reportTypeLabel);
        filtersPanel.add(reportTypeCombo);
        filtersPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        filtersPanel.add(generateBtn);
        filtersPanel.add(exportBtn);

        return filtersPanel;
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(FONT_REGULAR);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        comboBox.setPreferredSize(new Dimension(130, 35));
    }

    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 25, 0)); // Tăng spacing giữa các cards
        panel.setOpaque(false);

        // Get real metrics data from database
        int days = getPeriodDays((String) periodCombo.getSelectedItem());
        ReportService.MetricData metrics = reportService.getMetricsData(days);

        panel.add(createMetricCard("Tổng doanh thu", 
            formatCurrency(metrics.getTotalRevenue()), 
            formatPercentageChange(metrics.getRevenueChange()), 
            SUCCESS_COLOR, "💰"));
            
        panel.add(createMetricCard("Đơn hàng", 
            String.valueOf(metrics.getTotalOrders()), 
            formatPercentageChange(metrics.getOrderChange()), 
            INFO_COLOR, "📦"));
            
        panel.add(createMetricCard("Tên miền mới", 
            String.valueOf(metrics.getTotalDomains()), 
            formatPercentageChange(metrics.getDomainChange()), 
            WARNING_COLOR, "🌐"));
            
        panel.add(createMetricCard("Khách hàng", 
            String.valueOf(metrics.getTotalCustomers()), 
            formatPercentageChange(metrics.getCustomerChange()), 
            DANGER_COLOR, "👥"));

        return panel;
    }

    private JPanel createMetricCard(String title, String value, String change, Color accentColor, String icon) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Shadow
                g2.setColor(SHADOW_COLOR);
                g2.fill(new RoundRectangle2D.Float(2, 2, w - 2, h - 2, 16, 16));

                // Card background
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 2, h - 2, 16, 16));

                // Main border
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 3, h - 3, 16, 16));
                
                // Accent top border
                g2.setColor(accentColor);
                g2.setStroke(new BasicStroke(3));
                g2.drawLine(12, 2, w - 12, 2);

                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(25, 25, 25, 25)); // Tăng padding
        card.setPreferredSize(new Dimension(320, 160)); // Tăng thêm kích thước để chứa đầy đủ nội dung
        card.setMinimumSize(new Dimension(300, 160));

        // Header panel với layout tốt hơn
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28)); // Giảm một chút để cân bằng
        iconLabel.setBorder(new EmptyBorder(0, 0, 0, 12));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_REGULAR);
        titleLabel.setForeground(TEXT_SECONDARY);

        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);

        // Content section
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Truncate và format value để fit trong card
        String displayValue = value;
        if (value.length() > 16) {
            // Nếu là tiền tệ, format ngắn gọn hơn
            if (value.contains("₫")) {
                displayValue = formatShortCurrency(value);
            } else {
                displayValue = value.substring(0, 13) + "...";
            }
        }

        JLabel valueLabel = new JLabel(displayValue);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22)); // Giảm một chút để đảm bảo fit
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setToolTipText(value); // Hiển thị full value khi hover

        JLabel changeLabel = new JLabel(change);
        changeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Đảm bảo change text không bị cắt
        Color changeColor = change.startsWith("+") ? SUCCESS_COLOR : 
                           change.startsWith("-") ? DANGER_COLOR : TEXT_MUTED;
        changeLabel.setForeground(changeColor);
        changeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(valueLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 6))); // Giảm spacing để đảm bảo fit
        contentPanel.add(changeLabel);

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createChartPanel() {
        String period = (String) periodCombo.getSelectedItem();
        String reportType = (String) reportTypeCombo.getSelectedItem();
        
        // Get real chart data
        currentChartData = reportService.getChartData(period, reportType);
        String title = (String) currentChartData.getOrDefault("title", "Biểu đồ doanh thu");
        
        JPanel panel = createCard(title, period);
        
        JPanel chartContent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawChart(g);
            }
        };
        chartContent.setBackground(CARD_BG);
        
        // Calculate optimal width based on data points
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) currentChartData.get("labels");
        int dataPointCount = labels != null ? labels.size() : 0;
        
        // More reasonable sizing - không quá lớn
        int minWidth = 600; // Giảm minimum width
        int maxWidth = 1200; // Thêm maximum width để tránh quá lớn
        int dynamicWidth = Math.min(maxWidth, Math.max(minWidth, dataPointCount * 50)); // 50px per data point thay vì 80px
        
        chartContent.setPreferredSize(new Dimension(dynamicWidth, 280)); // Giảm height từ 300 xuống 280

        // Create scroll pane for chart if needed
        JScrollPane chartScrollPane = createChartScrollPane(chartContent);
        
        panel.add(chartScrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JScrollPane createChartScrollPane(JPanel chartContent) {
        JScrollPane scrollPane = new JScrollPane(chartContent);
        
        // Remove default border
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Set background for viewport
        scrollPane.getViewport().setBackground(CARD_BG);
        
        // Style scrollbars with our custom UI
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
        horizontalScrollBar.setUI(new ModernScrollBarUI());
        horizontalScrollBar.setPreferredSize(new Dimension(0, 12));
        horizontalScrollBar.setUnitIncrement(20);
        horizontalScrollBar.setBlockIncrement(100);
        
        // Hide vertical scrollbar for chart (not needed)
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Set preferred size
        scrollPane.setPreferredSize(new Dimension(0, 310)); // 280 + scrollbar height + padding
        
        return scrollPane;
    }

    private void drawChart(Graphics g) {
        if (currentChartData == null) return;
        
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth() - 80; // Tăng margin
        int height = getHeight() - 100; // Tăng margin cho labels
        int startX = 40; // Tăng left margin
        int startY = height + 40; // Tăng bottom margin

        // Get real data from chart data
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) currentChartData.get("labels");
        Object valuesObj = currentChartData.get("values");
        
        if (labels == null || valuesObj == null || labels.isEmpty()) {
            // Draw "No data" message
            g2.setColor(TEXT_MUTED);
            g2.setFont(FONT_REGULAR);
            FontMetrics fm = g2.getFontMetrics();
            String noDataMsg = "Không có dữ liệu";
            int msgX = (getWidth() - fm.stringWidth(noDataMsg)) / 2;
            int msgY = getHeight() / 2;
            g2.drawString(noDataMsg, msgX, msgY);
            g2.dispose();
            return;
        }

        // Convert values to double array
        double[] values = new double[labels.size()];
        if (valuesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Number> valuesList = (List<Number>) valuesObj;
            for (int i = 0; i < valuesList.size() && i < values.length; i++) {
                values[i] = valuesList.get(i).doubleValue();
            }
        }

        // Find max value for scaling
        double maxValue = 0;
        for (double value : values) {
            if (value > maxValue) maxValue = value;
        }
        if (maxValue == 0) maxValue = 1; // Avoid division by zero

        // Draw grid lines
        g2.setColor(new Color(0, 0, 0, 15));
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i <= 4; i++) { // Giảm từ 5 xuống 4 grid lines
            int y = startY - (i * height / 4);
            g2.drawLine(startX, y, startX + width - 20, y); // Không vẽ đến tận cùng
        }
        
        // Draw Y-axis labels
        g2.setColor(TEXT_MUTED);
        g2.setFont(FONT_SMALL);
        FontMetrics yAxisFm = g2.getFontMetrics();
        for (int i = 0; i <= 4; i++) {
            int y = startY - (i * height / 4);
            String label = formatChartValue(maxValue * i / 4);
            g2.drawString(label, startX - yAxisFm.stringWidth(label) - 8, y + 4);
        }

        // Draw chart bars with better sizing
        if (values.length > 0) {
            int availableWidth = width - 40; // Leave more margin
            int barSpacing = Math.max(8, availableWidth / (values.length * 4)); // Dynamic spacing
            int barWidth = Math.max(15, Math.min(60, (availableWidth - (barSpacing * (values.length - 1))) / values.length));
            
            for (int i = 0; i < values.length; i++) {
                int barHeight = Math.max(5, (int) ((values[i] * (height - 40)) / maxValue)); // Leave margin for labels
                int x = startX + 20 + (i * (barWidth + barSpacing));
                int y = startY - barHeight;

                // Gradient for bars
                GradientPaint gradient = new GradientPaint(
                    x, y, PRIMARY_COLOR,
                    x, startY, SECONDARY_COLOR
                );
                g2.setPaint(gradient);
                g2.fill(new RoundRectangle2D.Float(x, y, barWidth, barHeight, 6, 6));

                // Labels - cải thiện hiển thị
                g2.setColor(TEXT_SECONDARY);
                g2.setFont(FONT_SMALL);
                FontMetrics fm = g2.getFontMetrics();
                String label = labels.get(i);
                
                // Cắt label nếu quá dài
                String displayLabel = label;
                if (fm.stringWidth(label) > barWidth + 10) {
                    displayLabel = label.length() > 6 ? label.substring(0, 6) + "..." : label;
                }
                
                int labelX = x + (barWidth - fm.stringWidth(displayLabel)) / 2;
                g2.drawString(displayLabel, labelX, startY + 15);

                // Values on top of bars - chỉ hiển thị nếu bar đủ cao
                if (barHeight > 20) {
                    g2.setColor(TEXT_PRIMARY);
                    g2.setFont(FONT_SMALL);
                    String valueStr = formatChartValue(values[i]);
                    int valueX = x + (barWidth - fm.stringWidth(valueStr)) / 2;
                    g2.drawString(valueStr, valueX, y - 8);
                }
            }
        }

        g2.dispose();
    }

    private JPanel createEnhancedDataPanel() {
        JPanel panel = createCard("Dữ liệu chi tiết", "Thống kê theo ngày");

        // Create enhanced table with real data
        String[] columns = { "Ngày", "Đơn hàng", "Doanh thu", "Tên miền", "Tỷ lệ" };
        Object[][] data = getRealTableData();

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        styleTable(table);

        // Create search panel
        JPanel searchPanel = createSearchPanel(table);
        
        // Enhanced scroll pane with better styling
        JScrollPane scrollPane = createStyledScrollPane(table);
        
        // Container for search + table
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.add(searchPanel, BorderLayout.NORTH);
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(tableContainer, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createSearchPanel(JTable table) {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        searchPanel.setOpaque(false);
        
        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchLabel.setFont(FONT_REGULAR);
        searchLabel.setForeground(TEXT_SECONDARY);
        
        JTextField searchField = new JTextField(20);
        searchField.setFont(FONT_REGULAR);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        searchField.setBackground(Color.WHITE);
        searchField.setForeground(TEXT_PRIMARY);
        
        // Add search functionality
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }
            
            private void filterTable() {
                String text = searchField.getText().toLowerCase().trim();
                if (text.length() == 0) {
                    table.setRowSorter(null);
                } else {
                    try {
                        table.setRowSorter(new javax.swing.table.TableRowSorter<>(table.getModel()));
                        ((javax.swing.table.TableRowSorter<?>) table.getRowSorter()).setRowFilter(
                            javax.swing.RowFilter.regexFilter("(?i)" + text)
                        );
                    } catch (java.util.regex.PatternSyntaxException e) {
                        // If regex is invalid, just clear the filter
                        table.setRowSorter(null);
                    }
                }
            }
        });
        
        // Style search field with focus effects
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                searchField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                searchField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });
        
        searchPanel.add(searchLabel);
        searchPanel.add(Box.createHorizontalStrut(10));
        searchPanel.add(searchField);
        
        return searchPanel;
    }
    
    private JScrollPane createStyledScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Remove default border
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Set background for viewport
        scrollPane.getViewport().setBackground(CARD_BG);
        
        // Style the vertical scrollbar
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setBackground(new Color(248, 250, 252));
        verticalScrollBar.setUI(new ModernScrollBarUI());
        verticalScrollBar.setPreferredSize(new Dimension(12, 0));
        verticalScrollBar.setUnitIncrement(16);
        verticalScrollBar.setBlockIncrement(64);
        
        // Style the horizontal scrollbar
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
        horizontalScrollBar.setBackground(new Color(248, 250, 252));
        horizontalScrollBar.setUI(new ModernScrollBarUI());
        horizontalScrollBar.setPreferredSize(new Dimension(0, 12));
        horizontalScrollBar.setUnitIncrement(16);
        horizontalScrollBar.setBlockIncrement(64);
        
        // Set scroll policies
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Smooth scrolling
        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (!e.getValueIsAdjusting()) {
                scrollPane.repaint();
            }
        });
        
        // Set preferred size to show around 8-10 rows initially
        int preferredHeight = table.getRowHeight() * 8 + table.getTableHeader().getPreferredSize().height + 20;
        scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().width, preferredHeight));
        
        return scrollPane;
    }
    
    // Custom ScrollBar UI for modern look
    private class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(203, 213, 225);
            this.thumbDarkShadowColor = new Color(148, 163, 184);
            this.thumbHighlightColor = new Color(226, 232, 240);
            this.thumbLightShadowColor = new Color(148, 163, 184);
            this.trackColor = new Color(248, 250, 252);
            this.trackHighlightColor = new Color(241, 245, 249);
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }
        
        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
        
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            Color thumbColor = isDragging ? new Color(148, 163, 184) : new Color(203, 213, 225);
            g2.setColor(thumbColor);
            
            // Draw rounded rectangle thumb
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, 
                           thumbBounds.width - 4, thumbBounds.height - 4, 6, 6);
            
            g2.dispose();
        }
        
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(248, 250, 252));
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }
    }

    private void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(CARD_BG);
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(0, 0, 0, 10));
        table.setSelectionBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 20));
        table.setSelectionForeground(TEXT_PRIMARY);

        // Enable sorting
        table.setAutoCreateRowSorter(true);
        
        // Set column widths
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        if (table.getColumnModel().getColumnCount() >= 5) {
            table.getColumnModel().getColumn(0).setPreferredWidth(100); // Ngày
            table.getColumnModel().getColumn(1).setPreferredWidth(80);  // Đơn hàng
            table.getColumnModel().getColumn(2).setPreferredWidth(120); // Doanh thu
            table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Tên miền
            table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Tỷ lệ
        }

        // Header styling
        table.getTableHeader().setFont(FONT_LABEL);
        table.getTableHeader().setForeground(TEXT_SECONDARY);
        table.getTableHeader().setBackground(new Color(249, 250, 251));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        table.getTableHeader().setReorderingAllowed(false);

        // Custom cell renderer with alternating row colors
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                
                // Alternating row colors
                if (!isSelected) {
                    if (row % 2 == 0) {
                        setBackground(CARD_BG);
                    } else {
                        setBackground(new Color(249, 250, 251));
                    }
                }
                
                // Column-specific styling
                if (column == 2) { // Doanh thu column
                    setForeground(isSelected ? TEXT_PRIMARY : SUCCESS_COLOR);
                    setFont(FONT_LABEL);
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (column == 4) { // Tỷ lệ column
                    setFont(FONT_LABEL);
                    setHorizontalAlignment(SwingConstants.CENTER);
                    if (!isSelected) {
                        String percent = value.toString();
                        if (percent.contains("%")) {
                            try {
                                double val = Double.parseDouble(percent.replace("%", ""));
                                setForeground(val >= 70 ? SUCCESS_COLOR : val >= 50 ? WARNING_COLOR : DANGER_COLOR);
                            } catch (NumberFormatException e) {
                                setForeground(TEXT_PRIMARY);
                            }
                        }
                    }
                } else if (column == 1 || column == 3) { // Đơn hàng and Tên miền columns
                    setForeground(isSelected ? TEXT_PRIMARY : TEXT_PRIMARY);
                    setFont(FONT_REGULAR);
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else { // Ngày column
                    setForeground(isSelected ? TEXT_PRIMARY : TEXT_PRIMARY);
                    setFont(FONT_REGULAR);
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                
                return this;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        
        // Add hover effect
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                }
            }
        });
    }

    private Object[][] getRealTableData() {
        int days = getPeriodDays((String) periodCombo.getSelectedItem());
        List<Map<String, Object>> tableData = reportService.getDetailedReportData(days);
        
        Object[][] data = new Object[tableData.size()][5];
        
        for (int i = 0; i < tableData.size(); i++) {
            Map<String, Object> row = tableData.get(i);
            data[i][0] = row.get("date");
            data[i][1] = row.get("orders");
            data[i][2] = row.get("revenue");
            data[i][3] = row.get("domains");
            data[i][4] = row.get("rate");
        }
        
        return data;
    }

    private String formatCurrency(double amount) {
        return String.format("%,.0f ₫", amount);
    }
    
    private String formatShortCurrency(String fullCurrency) {
        // Extract number from currency string
        String numberStr = fullCurrency.replace("₫", "").replace(",", "").trim();
        try {
            double amount = Double.parseDouble(numberStr);
            if (amount >= 1000000000) {
                return String.format("%.1fB ₫", amount / 1000000000);
            } else if (amount >= 1000000) {
                return String.format("%.1fM ₫", amount / 1000000);
            } else if (amount >= 1000) {
                return String.format("%.0fK ₫", amount / 1000);
            } else {
                return String.format("%.0f ₫", amount);
            }
        } catch (NumberFormatException e) {
            return fullCurrency.length() > 15 ? fullCurrency.substring(0, 12) + "..." : fullCurrency;
        }
    }
    
    private String formatPercentageChange(double change) {
        String sign = change >= 0 ? "+" : "";
        return String.format("%s%.1f%%", sign, change);
    }
    
    private String formatChartValue(double value) {
        if (value >= 1000000) {
            return String.format("%.1fM", value / 1000000);
        } else if (value >= 1000) {
            return String.format("%.0fK", value / 1000);
        } else {
            return String.format("%.0f", value);
        }
    }

    private int getPeriodDays(String period) {
        if (period == null) return 7;
        switch (period) {
            case "7 ngày qua": return 7;
            case "30 ngày qua": return 30;
            case "3 tháng qua": return 90;
            case "6 tháng qua": return 180;
            case "1 năm qua": return 365;
            default: return 7;
        }
    }

    private JPanel createCard(String title, String subtitle) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow
                g2.setColor(SHADOW_COLOR);
                g2.fill(new RoundRectangle2D.Float(3, 3, getWidth() - 3, getHeight() - 3, 16, 16));

                // Card background
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 3, getHeight() - 3, 16, 16));

                // Border
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 4, 16, 16));

                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        if (title != null) {
            JPanel headerPanel = new JPanel();
            headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
            headerPanel.setOpaque(false);
            headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(FONT_SUBTITLE);
            titleLabel.setForeground(TEXT_PRIMARY);
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            if (subtitle != null) {
                JLabel subtitleLabel = new JLabel(subtitle);
                subtitleLabel.setFont(FONT_SMALL);
                subtitleLabel.setForeground(TEXT_MUTED);
                subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                headerPanel.add(titleLabel);
                headerPanel.add(Box.createRigidArea(new Dimension(0, 3)));
                headerPanel.add(subtitleLabel);
            } else {
                headerPanel.add(titleLabel);
            }

            card.add(headerPanel, BorderLayout.NORTH);
        }

        return card;
    }

    private JButton createModernButton(String text, Color color, boolean isPrimary) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isPrimary) {
                    GradientPaint gradient = new GradientPaint(
                        0, 0, color,
                        getWidth(), getHeight(), color.darker());
                    g2.setPaint(gradient);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                } else {
                    g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 15));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                    
                    g2.setColor(color);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                }

                super.paintComponent(g2);
            }
        };

        button.setFont(FONT_REGULAR);
        button.setForeground(isPrimary ? Color.WHITE : color);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));

        return button;
    }

    private void generateReport() {
        String period = (String) periodCombo.getSelectedItem();
        String reportType = (String) reportTypeCombo.getSelectedItem();

        // Update chart title
        updateChartTitle(reportType, period);
        
        // Refresh data
        refreshData();
        
        // Show success message
        // showStyledNotification("Báo cáo đã được tạo thành công!", true);
    }

    private void updateChartTitle(String reportType, String period) {
        // Find and update chart title (this would be more sophisticated in real implementation)
        repaint();
    }

    private void refreshData() {
        // Refresh metrics panel
        if (metricsPanel != null) {
            metricsPanel.removeAll();
            JPanel newMetrics = createMetricsPanel();
            metricsPanel.setLayout(new BorderLayout());
            metricsPanel.add(newMetrics, BorderLayout.CENTER);
        }
        
        // Refresh chart panel
        if (chartPanel != null) {
            chartPanel.removeAll();
            JPanel newChart = createChartPanel();
            chartPanel.setLayout(new BorderLayout());
            chartPanel.add(newChart, BorderLayout.CENTER);
        }
        
        // Refresh data table
        if (dataPanel != null) {
            dataPanel.removeAll();
            JPanel newData = createEnhancedDataPanel();
            dataPanel.setLayout(new BorderLayout());
            dataPanel.add(newData, BorderLayout.CENTER);
        }
        
        // Repaint the entire panel
        revalidate();
        repaint();
    }

    private void exportReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Xuất báo cáo");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Excel Files", "xlsx"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            showStyledNotification("Báo cáo đã được xuất thành công!", true);
        }
    }

    private void showStyledNotification(String message, boolean isSuccess) {
        JOptionPane.showMessageDialog(this, message, 
            isSuccess ? "Thành công" : "Lỗi",
            isSuccess ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }
}