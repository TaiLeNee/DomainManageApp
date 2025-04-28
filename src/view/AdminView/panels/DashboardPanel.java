package view.AdminView.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import model.Domain;
import model.Order;
import model.Transaction;
import model.User;
import repository.DomainRepository;
import repository.OrderRepository;
import repository.TransactionRepository;
import repository.UserRepository;
import service.DomainService;

public class DashboardPanel extends JPanel {
    // Màu sắc hiện đại
    private static final Color BG_COLOR = new Color(248, 250, 252);
    private static final Color BORDER_COLOR = new Color(230, 235, 241);
    private static final Color TEXT_PRIMARY = new Color(34, 40, 49);
    private static final Color TEXT_SECONDARY = new Color(130, 139, 162);

    // Màu sắc cho các card
    private static final Color DOMAIN_COLOR = new Color(29, 98, 240);
    private static final Color AVAILABLE_COLOR = new Color(38, 180, 133);
    private static final Color ORDER_COLOR = new Color(246, 153, 35);
    private static final Color REVENUE_COLOR = new Color(230, 60, 80);

    // Font chữ
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_VALUE = new Font("Segoe UI", Font.BOLD, 24);

    private DomainRepository domainRepository;
    private OrderRepository orderRepository;
    private UserRepository userRepository;
    private TransactionRepository transactionRepository;
    private DomainService domainService;

    public DashboardPanel(DomainRepository domainRepository, OrderRepository orderRepository,
            UserRepository userRepository, TransactionRepository transactionRepository,
            DomainService domainService) {
        this.domainRepository = domainRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.domainService = domainService;

        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        initComponents();
    }

    private void initComponents() {
        // Panel tổng quan với Grid Bag Layout để linh hoạt hơn
        JPanel overviewPanel = new JPanel(new GridBagLayout());
        overviewPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        // Thêm tiêu đề trang
        JLabel welcomeLabel = new JLabel("Tổng Quan Hệ Thống");
        welcomeLabel.setFont(FONT_TITLE);
        welcomeLabel.setForeground(TEXT_PRIMARY);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 20, 0);
        overviewPanel.add(welcomeLabel, gbc);

        // Lấy dữ liệu thống kê từ database
        int totalDomains = 0;
        int availableDomains = 0;
        int totalOrders = 0;
        double totalRevenue = 0;

        try {
            // Lấy tổng số tên miền và số lượng miền khả dụng
            List<Domain> domains = domainRepository.getAllDomains();
            totalDomains = domains.size();
            availableDomains = (int) domains.stream()
                    .filter(domain -> "Khả dụng".equals(domain.getStatus()))
                    .count();

            // Lấy tổng số đơn hàng
            List<Order> orders = orderRepository.getAllOrders();
            totalOrders = orders.size();

            // Tính tổng doanh thu
            List<Transaction> transactions = transactionRepository.getAllTransactions();
            totalRevenue = transactions.stream()
                    .mapToDouble(Transaction::getAmount)
                    .sum();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải dữ liệu thống kê: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        // Định dạng tiền tệ
        DecimalFormat currencyFormat = new DecimalFormat("#,### VND");
        String formattedRevenue = currencyFormat.format(totalRevenue);

        // Panel chứa các thẻ thống kê với FlowLayout để thích ứng kích thước
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setOpaque(false);

        // Các thẻ thống kê hiện đại
        JPanel totalDomainsCard = createModernStatCard("Tổng số tên miền", String.valueOf(totalDomains), "domain.png",
                DOMAIN_COLOR);
        JPanel availableDomainsCard = createModernStatCard("Tên miền khả dụng", String.valueOf(availableDomains),
                "available.png", AVAILABLE_COLOR);
        JPanel ordersCard = createModernStatCard("Đơn hàng", String.valueOf(totalOrders), "order.png", ORDER_COLOR);
        JPanel revenueCard = createModernStatCard("Doanh thu", formattedRevenue, "money.png", REVENUE_COLOR);

        statsPanel.add(totalDomainsCard);
        statsPanel.add(availableDomainsCard);
        statsPanel.add(ordersCard);
        statsPanel.add(revenueCard);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 25, 0);
        overviewPanel.add(statsPanel, gbc);

        // Tiêu đề khu vực bảng dữ liệu
        JLabel dataLabel = new JLabel("Dữ Liệu Cần Chú Ý");
        dataLabel.setFont(FONT_SUBTITLE);
        dataLabel.setForeground(TEXT_PRIMARY);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 15, 0);
        overviewPanel.add(dataLabel, gbc);

        // Panel chứa bảng đơn hàng gần đây và domain sắp hết hạn
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        tablesPanel.setOpaque(false);

        // Bảng đơn hàng gần đây
        JPanel recentOrdersPanel = createRecentOrdersPanel(currencyFormat);
        // Bảng domain sắp hết hạn
        JPanel expiringDomainsPanel = createExpiringDomainsPanel();

        tablesPanel.add(recentOrdersPanel);
        tablesPanel.add(expiringDomainsPanel);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        overviewPanel.add(tablesPanel, gbc);

        add(new JScrollPane(overviewPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
    }

    private JPanel createRecentOrdersPanel(DecimalFormat currencyFormat) {
        JPanel recentOrdersPanel;
        try {
            // Lấy 5 đơn hàng gần đây nhất
            List<Order> recentOrders = orderRepository.getRecentOrders(5);
            Object[][] orderData = new Object[recentOrders.size()][6];

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            for (int i = 0; i < recentOrders.size(); i++) {
                Order order = recentOrders.get(i);
                User user = userRepository.getUserById(order.getUserId());
                Domain domain = domainRepository.getDomainById(order.getDomainId());

                orderData[i][0] = "#" + order.getId();
                orderData[i][1] = domain != null ? domain.getName() + domain.getExtension() : "N/A";
                orderData[i][2] = user != null ? user.getEmail() : "N/A";
                orderData[i][3] = currencyFormat.format(order.getTotalPrice());
                orderData[i][4] = order.getStatus();
                orderData[i][5] = dateFormat.format(order.getOrderDate());
            }

            recentOrdersPanel = createModernTablePanel(
                    "Đơn hàng gần đây",
                    new String[] { "ID", "Tên miền", "Người mua", "Giá", "Trạng thái", "Ngày" },
                    orderData);
        } catch (Exception e) {
            recentOrdersPanel = createModernTablePanel(
                    "Đơn hàng gần đây (Lỗi tải dữ liệu)",
                    new String[] { "ID", "Tên miền", "Người mua", "Giá", "Trạng thái", "Ngày" },
                    new Object[][] {});
        }
        return recentOrdersPanel;
    }

    private JPanel createExpiringDomainsPanel() {
        JPanel expiringDomainsPanel;
        try {
            // Lấy các tên miền sắp hết hạn trong 30 ngày tới, giới hạn 5 bản ghi
            List<Domain> expiringDomains = domainService.getExpiringDomains(30, 5);
            Object[][] domainData = new Object[expiringDomains.size()][5];

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            for (int i = 0; i < expiringDomains.size(); i++) {
                Domain domain = expiringDomains.get(i);

                // Tìm order gần nhất liên quan đến domain này để lấy thông tin người mua
                Order latestOrder = orderRepository.findLatestOrderByDomainId(domain.getId());
                User owner = latestOrder != null ? userRepository.getUserById(latestOrder.getUserId()) : null;

                // Tính số ngày còn lại - chuyển đổi LocalDateTime sang Date để tính toán
                long daysLeft = 0;
                if (domain.getExpiryDate() != null) {
                    Date expiryDate = Date.from(domain.getExpiryDate().atZone(ZoneId.systemDefault()).toInstant());
                    daysLeft = (expiryDate.getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24);
                }

                domainData[i][0] = domain.getId();
                domainData[i][1] = domain.getName() + domain.getExtension();
                domainData[i][2] = domain.getExpiryDate() != null ? dateFormat
                        .format(Date.from(domain.getExpiryDate().atZone(ZoneId.systemDefault()).toInstant())) : "N/A";
                domainData[i][3] = owner != null ? owner.getEmail() : "N/A";
                domainData[i][4] = daysLeft + " ngày";
            }

            expiringDomainsPanel = createModernTablePanel(
                    "Tên miền sắp hết hạn",
                    new String[] { "ID", "Tên miền", "Ngày hết hạn", "Người sở hữu", "Tình trạng" },
                    domainData);
        } catch (Exception e) {
            expiringDomainsPanel = createModernTablePanel(
                    "Tên miền sắp hết hạn (Lỗi tải dữ liệu)",
                    new String[] { "ID", "Tên miền", "Ngày hết hạn", "Người sở hữu", "Tình trạng" },
                    new Object[][] {});
        }
        return expiringDomainsPanel;
    }

    private JPanel createModernStatCard(String title, String value, String iconName, Color color) {
        // Card với hiệu ứng bo tròn góc
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2d.dispose();
            }
        };

        card.setLayout(new BorderLayout());
        card.setBackground(new Color(0, 0, 0, 0)); // Transparent
        card.setBorder(new EmptyBorder(15, 0, 15, 0));

        // Panel bên trái chứa icon với màu nền
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Tạo gradient cho icon background
                GradientPaint gradient = new GradientPaint(
                        0, 0, color,
                        0, getHeight(), new Color(color.getRed(), color.getGreen(), color.getBlue(), 200));
                g2d.setPaint(gradient);
                g2d.fillRoundRect(10, 0, getWidth() - 10, getHeight(), 12, 12);
                g2d.dispose();
            }
        };

        iconPanel.setLayout(new BorderLayout());
        iconPanel.setPreferredSize(new Dimension(100, 0));
        iconPanel.setBackground(new Color(0, 0, 0, 0)); // Transparent

        JLabel iconLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon("src/img/" + iconName);
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(36, 36, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) {
            // Tạo icon mặc định nếu không tìm thấy
            iconLabel.setText("•");
            iconLabel.setFont(new Font("Arial", Font.BOLD, 36));
        }
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setHorizontalAlignment(JLabel.CENTER);
        iconPanel.add(iconLabel, BorderLayout.CENTER);

        // Panel bên phải chứa thông tin
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new EmptyBorder(0, 25, 0, 15));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setFont(FONT_REGULAR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setFont(FONT_VALUE);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        infoPanel.add(valueLabel);

        // Thêm vào card
        card.add(iconPanel, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);

        // Thêm shadow effect (hiệu ứng đổ bóng) bằng border
        card = createPanelWithShadow(card);

        return card;
    }

    private JPanel createPanelWithShadow(JPanel content) {
        JPanel shadowPanel = new JPanel(new BorderLayout());
        shadowPanel.setBackground(new Color(0, 0, 0, 0));
        shadowPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(2, 2, 5, 5), // Space for shadow
                null));

        // Thêm shadow effect
        JPanel shadowEffect = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(210, 210, 210, 90));
                g2d.fillRoundRect(3, 3, getWidth() - 4, getHeight() - 4, 12, 12);
                g2d.dispose();
            }
        };
        shadowEffect.setOpaque(false);

        shadowPanel.add(shadowEffect, BorderLayout.CENTER);
        shadowPanel.add(content, BorderLayout.CENTER);

        return shadowPanel;
    }

    private JPanel createModernTablePanel(String title, String[] columns, Object[][] data) {
        // Main panel với bo góc
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2d.dispose();
            }
        };

        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(0, 0, 0, 0)); // Transparent

        // Header with title and icon
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(15, 20, 15, 20)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setForeground(TEXT_PRIMARY);

        // Thêm icon phù hợp với tiêu đề
        if (title.contains("Đơn hàng")) {
            titleLabel.setIcon(new ImageIcon("src/img/order_small.png"));
        } else if (title.contains("Tên miền")) {
            titleLabel.setIcon(new ImageIcon("src/img/domain_small.png"));
        }
        titleLabel.setIconTextGap(10);

        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Button refresh
        JButton refreshBtn = new JButton();
        refreshBtn.setIcon(new ImageIcon("src/img/refresh.png"));
        refreshBtn.setBorderPainted(false);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setContentAreaFilled(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        // Table with modern style
        DefaultTableModel model = new DefaultTableModel(data, columns);
        JTable table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 252));
                }
                return c;
            }
        };

        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER_COLOR);
        table.setBorder(null);
        table.setFont(FONT_SMALL);

        // Thiết lập header của bảng
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(248, 249, 250));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Center align text in cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Renderer cho cột trạng thái
        if (title.contains("Đơn hàng")) {
            table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                    String status = (String) value;
                    if ("Hoàn thành".equals(status)) {
                        c.setForeground(new Color(38, 180, 133));
                    } else if ("Đang xử lý".equals(status)) {
                        c.setForeground(new Color(246, 153, 35));
                    } else if ("Hủy".equals(status)) {
                        c.setForeground(new Color(230, 60, 80));
                    } else {
                        c.setForeground(TEXT_PRIMARY);
                    }
                    setHorizontalAlignment(JLabel.CENTER);
                    return c;
                }
            });
        }

        // Renderer cho cột tình trạng
        if (title.contains("Tên miền")) {
            table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                    String daysStr = (String) value;
                    try {
                        int days = Integer.parseInt(daysStr.split(" ")[0]);
                        if (days < 7) {
                            c.setForeground(new Color(230, 60, 80)); // Red for critical
                        } else if (days < 15) {
                            c.setForeground(new Color(246, 153, 35)); // Orange for warning
                        } else {
                            c.setForeground(new Color(38, 180, 133)); // Green for ok
                        }
                    } catch (Exception e) {
                        c.setForeground(TEXT_PRIMARY);
                    }
                    setHorizontalAlignment(JLabel.CENTER);
                    return c;
                }
            });
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Empty data message
        if (data.length == 0) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(Color.WHITE);
            JLabel emptyLabel = new JLabel("Không có dữ liệu", JLabel.CENTER);
            emptyLabel.setForeground(TEXT_SECONDARY);
            emptyLabel.setFont(FONT_REGULAR);
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            panel.add(emptyPanel, BorderLayout.CENTER);
        } else {
            panel.add(scrollPane, BorderLayout.CENTER);
        }

        panel.add(headerPanel, BorderLayout.NORTH);

        // Thêm shadow effect
        panel = createPanelWithShadow(panel);

        return panel;
    }
}


