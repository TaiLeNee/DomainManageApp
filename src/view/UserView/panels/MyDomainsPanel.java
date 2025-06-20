package view.UserView.panels;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import model.Domain;
import model.RentalPeriod;
import model.User;
import repository.DatabaseConnection;
import repository.DomainRepository;
import repository.RentalPeriodRepository;
import service.DomainService;

public class MyDomainsPanel extends JPanel {
    // Modern color palette
    private static final Color BG_COLOR = new Color(248, 250, 252);
    private static final Color PRIMARY_COLOR = new Color(41, 59, 95);
    private static final Color SECONDARY_COLOR = new Color(66, 91, 138);
    private static final Color ACCENT_COLOR = new Color(255, 111, 0);
    private static final Color TEXT_PRIMARY = new Color(34, 40, 49);
    private static final Color TEXT_SECONDARY = new Color(130, 139, 162);
    private static final Color BORDER_COLOR = new Color(230, 235, 241);

    // Modern fonts
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    private User currentUser;
    private DomainService domainService;
    private DomainRepository domainRepository;
    private RentalPeriodRepository rentalPeriodRepository;

    // UI components
    private JTable domainsTable;
    private DefaultTableModel tableModel;
    private JLabel totalPriceLabel;
    private JPanel emptyStatePanel;
    private JPanel tablePanel;
    private JLabel emptyStateLabel;
    private double totalPrice = 0.0;
    private List<Domain> cartDomains = new ArrayList<>();
    private JComboBox<String> rentalPeriodComboBox;

    /**
     * Constructor
     */
    public MyDomainsPanel(User currentUser) {
        this.currentUser = currentUser;
        this.domainService = new DomainService();
        this.domainRepository = new DomainRepository();
        this.rentalPeriodRepository = new RentalPeriodRepository();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Create content panel with CardLayout
        JPanel contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(Color.WHITE);

        // Create table panel
        tablePanel = createTablePanel();

        // Create empty state panel
        emptyStatePanel = createEmptyStatePanel();

        // Add both panels to card layout
        contentPanel.add(tablePanel, "TABLE");
        contentPanel.add(emptyStatePanel, "EMPTY");

        add(contentPanel, BorderLayout.CENTER);

        // Create footer panel with checkout button
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        // Load domains from database
        loadDomainsFromDatabase();
    }

    /**
     * Creates a modern header panel with title and actions
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        // Title with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);

        JLabel iconLabel = new JLabel(new ImageIcon("src/img/cart.png"));

        JLabel titleLabel = new JLabel("Giỏ hàng tên miền");
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setForeground(TEXT_PRIMARY);

        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);

        // Refresh button
        JButton refreshButton = new JButton("Làm mới");
        refreshButton.setFont(FONT_REGULAR);
        refreshButton.setForeground(SECONDARY_COLOR);
        refreshButton.setBorderPainted(false);
        refreshButton.setContentAreaFilled(false);
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.setIcon(new ImageIcon("src/img/refresh.png"));

        refreshButton.addActionListener(e -> loadDomainsFromDatabase());

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(refreshButton, BorderLayout.EAST);

        return headerPanel;
    }

    /**
     * Creates the table panel with modern styling
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Create column names with modern naming
        String[] columnNames = { "STT", "Tên miền", "Phần mở rộng", "Giá", "Kỳ hạn", "Thao tác" };

        // Create table model that is not editable except for the actions column
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only action column is editable
            }
        };

        // Create modern styled table
        domainsTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                // Alternate row colors for better readability
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 253));
                }

                return c;
            }
        };

        // Table appearance
        domainsTable.setRowHeight(50);
        domainsTable.setShowVerticalLines(false);
        domainsTable.setGridColor(BORDER_COLOR);
        domainsTable.setSelectionBackground(new Color(232, 240, 254));
        domainsTable.setSelectionForeground(TEXT_PRIMARY);
        domainsTable.setFont(FONT_REGULAR);

        // Table header styling
        JTableHeader header = domainsTable.getTableHeader();
        header.setBackground(SECONDARY_COLOR);
        header.setForeground(Color.BLUE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBorder(null);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        // Custom renderers for columns
        // ID Column (Centered)
        domainsTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        });

        // Domain Name Column
        domainsTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.LEFT);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                return c;
            }
        });

        // Extension column
        domainsTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setForeground(isSelected ? TEXT_PRIMARY : ACCENT_COLOR);
                return c;
            }
        });

        // Price column
        domainsTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.RIGHT);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                setForeground(isSelected ? TEXT_PRIMARY : ACCENT_COLOR);
                return c;
            }
        });

        // Rental Period Column with combo box
        domainsTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.LEFT);
                return c;
            }
        });

        // Action Column
        domainsTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        domainsTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));

        // Set column widths
        domainsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        domainsTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        domainsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        domainsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        domainsTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        domainsTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        // Set up a table model for combo box in rental period column
        domainsTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(createRentalPeriodComboBox()));

        // Create modern scrollpane
        JScrollPane scrollPane = new JScrollPane(domainsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Add shadow border
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1, true),
                        BorderFactory.createEmptyBorder(0, 0, 0, 0))));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates a combo box for rental periods
     */
    private JComboBox<String> createRentalPeriodComboBox() {
        rentalPeriodComboBox = new JComboBox<>();

        // Fetch rental periods from database
        try {
            List<RentalPeriod> rentalPeriods = rentalPeriodRepository.getAllRentalPeriods();
            for (RentalPeriod period : rentalPeriods) {
                rentalPeriodComboBox.addItem(period.getDescription());
            }
        } catch (Exception e) {
            e.printStackTrace();
            rentalPeriodComboBox.addItem("1 năm");
            rentalPeriodComboBox.addItem("2 năm");
            rentalPeriodComboBox.addItem("3 năm");
        }

        // Add change listener to update price
        rentalPeriodComboBox.addActionListener(e -> {
            updateTotalPrice();
        });

        rentalPeriodComboBox.setFont(FONT_REGULAR);
        rentalPeriodComboBox.setBackground(Color.WHITE);

        return rentalPeriodComboBox;
    }

    /**
     * Creates an empty state panel shown when cart is empty
     */
    private JPanel createEmptyStatePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1, true),
                        BorderFactory.createEmptyBorder(30, 0, 30, 0))));

        // Empty state icon
        JLabel iconLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon("src/img/empty_cart.png");
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) {
            // Fallback if image not found
            iconLabel.setText("🛒");
            iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 72));
        }
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Empty state message
        emptyStateLabel = new JLabel("Giỏ hàng của bạn đang trống");
        emptyStateLabel.setFont(FONT_SUBTITLE);
        emptyStateLabel.setForeground(TEXT_SECONDARY);
        emptyStateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Empty state description
        JLabel descLabel = new JLabel("Tìm kiếm và thêm tên miền vào giỏ hàng của bạn");
        descLabel.setFont(FONT_REGULAR);
        descLabel.setForeground(TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Action button
        JButton actionButton = new JButton("Tìm tên miền ngay") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient
                GradientPaint gradient = new GradientPaint(
                        0, 0, PRIMARY_COLOR,
                        getWidth(), getHeight(), SECONDARY_COLOR);
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                super.paintComponent(g2);
            }
        };
        actionButton.setFont(FONT_REGULAR);
        actionButton.setForeground(Color.WHITE);
        actionButton.setBorderPainted(false);
        actionButton.setContentAreaFilled(false);
        actionButton.setFocusPainted(false);
        actionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        actionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionButton.setMaximumSize(new Dimension(200, 40));
        actionButton.addActionListener(e -> {
            // Navigate to search domain panel
            Container parent = MyDomainsPanel.this.getParent();
            while (parent != null && !(parent.getLayout() instanceof CardLayout)) {
                parent = parent.getParent();
            }

            if (parent != null) {
                CardLayout layout = (CardLayout) parent.getLayout();
                layout.show(parent, "SEARCH_DOMAIN_PANEL");
            }
        });

        // Add components with spacing
        panel.add(Box.createVerticalGlue());
        panel.add(iconLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(emptyStateLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(descLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(actionButton);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * Creates the footer panel with checkout button and total price
     */
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 5, 5, 5));

        // Total price panel
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pricePanel.setOpaque(false);

        JLabel priceTextLabel = new JLabel("Tổng tiền: ");
        priceTextLabel.setFont(FONT_REGULAR);
        priceTextLabel.setForeground(TEXT_PRIMARY);

        totalPriceLabel = new JLabel("0 VND");
        totalPriceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalPriceLabel.setForeground(ACCENT_COLOR);

        pricePanel.add(priceTextLabel);
        pricePanel.add(totalPriceLabel);

        // Checkout button with gradient
        JButton checkoutButton = new JButton("Tiến hành thanh toán") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient
                GradientPaint gradient = new GradientPaint(
                        0, 0, ACCENT_COLOR,
                        getWidth(), getHeight(), new Color(255, 132, 41));
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                super.paintComponent(g2);
            }
        };
        checkoutButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setBorderPainted(false);
        checkoutButton.setContentAreaFilled(false);
        checkoutButton.setFocusPainted(false);
        checkoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkoutButton.addActionListener(e -> checkoutDomains());

        // Actions panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionsPanel.setOpaque(false);

        // Clear cart button
        JButton clearButton = new JButton("Xóa giỏ hàng");
        clearButton.setFont(FONT_REGULAR);
        clearButton.setForeground(TEXT_SECONDARY);
        clearButton.setBorderPainted(false);
        clearButton.setContentAreaFilled(false);
        clearButton.setFocusPainted(false);
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.addActionListener(e -> clearCart());

        actionsPanel.add(clearButton);
        actionsPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        actionsPanel.add(checkoutButton);

        panel.add(pricePanel, BorderLayout.WEST);
        panel.add(actionsPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Load domains from database
     */
    public void loadDomainsFromDatabase() {
        // Clear existing data
        cartDomains.clear();
        tableModel.setRowCount(0);
        totalPrice = 0.0;

        // Show appropriate panel based on whether cart has items
        Container container = (Container) getComponent(1);
        CardLayout cl = (CardLayout) container.getLayout();

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Sửa truy vấn để lấy thêm thông tin chi tiết về rental_period
            String query = "SELECT d.*, c.rental_period_id, rp.months, rp.discount FROM domains d " +
                    "JOIN cart c ON d.id = c.domain_id " +
                    "JOIN rental_periods rp ON c.rental_period_id = rp.id " +
                    "WHERE c.user_id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, currentUser.getId());
                ResultSet rs = stmt.executeQuery();

                int counter = 0;

                // Load domain data into table
                while (rs.next()) {
                    counter++;

                    // Create domain object
                    Domain domain = new Domain();
                    domain.setId(rs.getInt("id"));
                    domain.setName(rs.getString("name"));
                    domain.setExtension(rs.getString("extension"));
                    domain.setPrice(rs.getDouble("price"));
                    domain.setStatus(rs.getString("status"));

                    // Add domain to list
                    cartDomains.add(domain);

                    // Lấy thông tin kỳ hạn thuê
                    int rentalPeriodId = rs.getInt("rental_period_id");
                    int months = rs.getInt("months");
                    double discount = rs.getDouble("discount");

                    // Tính giá theo kỳ hạn và giảm giá
                    double basePrice = domain.getPrice();
                    double originalPrice = basePrice * months; // Giá gốc theo số tháng
                    double finalPrice = originalPrice * (1 - discount); // Giá đã giảm

                    // Format price
                    DecimalFormat formatter = new DecimalFormat("#,### VND");
                    String formattedPrice = formatter.format(finalPrice);

                    // Get rental period description
                    String rentalPeriodDesc = getRentalPeriodDescription(rentalPeriodId);

                    // Add row to table
                    tableModel.addRow(new Object[] {
                            counter,
                            domain.getName(),
                            domain.getExtension(),
                            formattedPrice,
                            rentalPeriodDesc,
                            "Xóa"
                    });

                    // Add to total price
                    totalPrice += finalPrice;
                }

                // Update total price display
                updateTotalPriceDisplay();

                // Show appropriate panel
                if (counter == 0) {
                    cl.show((Container) getComponent(1), "EMPTY");
                } else {
                    cl.show((Container) getComponent(1), "TABLE");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Không thể tải dữ liệu từ giỏ hàng: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);

            // Show empty state with error
            cl.show((Container) getComponent(1), "EMPTY");
            emptyStateLabel.setText("Không thể tải dữ liệu. Vui lòng thử lại sau.");
        }
    }

    /**
     * Get rental period description by id
     */
    private String getRentalPeriodDescription(int id) {
        try {
            RentalPeriod period = rentalPeriodRepository.getRentalPeriodById(id);
            if (period != null) {
                return period.getDescription();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "1 năm"; // Default
    }

    /**
     * Update total price based on domains and rental periods
     */
    private void updateTotalPrice() {
        totalPrice = 0.0;

        // Tính lại tổng tiền từ các giá trị hiển thị trong bảng
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String priceStr = (String) tableModel.getValueAt(i, 3);
            priceStr = priceStr.replace(" VND", "").replace(",", "");
            double price = Double.parseDouble(priceStr);

            // Cộng dồn giá trị mà không cần áp dụng thêm hệ số nhân
            // vì giá hiển thị đã được tính dựa trên kỳ hạn và chiết khấu
            totalPrice += price;
        }

        updateTotalPriceDisplay();
    }

    /**
     * Update the total price display
     */
    private void updateTotalPriceDisplay() {
        DecimalFormat formatter = new DecimalFormat("#,### VND");
        totalPriceLabel.setText(formatter.format(totalPrice));
    }

    /**
     * Clear all items from cart
     */
    private void clearCart() {
        if (cartDomains.isEmpty()) {
            return;
        }

        // Confirm dialog
        int option = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa tất cả tên miền khỏi giỏ hàng?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "DELETE FROM cart WHERE user_id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, currentUser.getId());
                int result = stmt.executeUpdate();

                if (result > 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Đã xóa tất cả tên miền khỏi giỏ hàng.",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Reload data
                    loadDomainsFromDatabase();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể xóa giỏ hàng: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Process checkout of domains in cart
     */
    private void checkoutDomains() {
        if (cartDomains.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Giỏ hàng của bạn đang trống.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Add checkout confirmation and processing logic here
        int option = JOptionPane.showConfirmDialog(
                this,
                "Tiến hành thanh toán " + cartDomains.size() + " tên miền với tổng số tiền " + totalPriceLabel.getText()
                        + "?",
                "Xác nhận thanh toán",
                JOptionPane.YES_NO_OPTION);

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        // Show checkout dialog
        JDialog checkoutDialog = createCheckoutDialog();
        checkoutDialog.setVisible(true);
    }

    /**
     * Create a modern checkout dialog
     */
    private JDialog createCheckoutDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thanh toán", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Title section
        JLabel titleLabel = new JLabel("Thông tin thanh toán");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
        separator.setForeground(BORDER_COLOR);
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Order summary section
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBackground(new Color(250, 250, 252));
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(15, 15, 15, 15)));
        summaryPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 200));
        summaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel summaryTitleLabel = new JLabel("Tóm tắt đơn hàng");
        summaryTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        summaryTitleLabel.setForeground(TEXT_PRIMARY);
        summaryTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Domain list
        JPanel domainListPanel = new JPanel();
        domainListPanel.setLayout(new BoxLayout(domainListPanel, BoxLayout.Y_AXIS));
        domainListPanel.setOpaque(false);
        domainListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < Math.min(cartDomains.size(), 5); i++) {
            Domain domain = cartDomains.get(i);
            String rentalPeriod = (String) tableModel.getValueAt(i, 4);
            String price = (String) tableModel.getValueAt(i, 3);

            JPanel domainRow = new JPanel(new BorderLayout());
            domainRow.setOpaque(false);
            domainRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));

            JLabel nameLabel = new JLabel(domain.getName() + domain.getExtension());
            nameLabel.setFont(FONT_REGULAR);

            JLabel periodLabel = new JLabel(rentalPeriod);
            periodLabel.setFont(FONT_SMALL);
            periodLabel.setForeground(TEXT_SECONDARY);

            JPanel leftPanel = new JPanel(new BorderLayout());
            leftPanel.setOpaque(false);
            leftPanel.add(nameLabel, BorderLayout.NORTH);
            leftPanel.add(periodLabel, BorderLayout.SOUTH);

            JLabel priceLabel = new JLabel(price);
            priceLabel.setFont(FONT_REGULAR);
            priceLabel.setHorizontalAlignment(JLabel.RIGHT);

            domainRow.add(leftPanel, BorderLayout.WEST);
            domainRow.add(priceLabel, BorderLayout.EAST);

            domainListPanel.add(domainRow);
            domainListPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // Show "and X more" if there are more domains
        if (cartDomains.size() > 5) {
            JLabel moreLabel = new JLabel("và " + (cartDomains.size() - 5) + " tên miền khác");
            moreLabel.setFont(FONT_SMALL);
            moreLabel.setForeground(TEXT_SECONDARY);
            moreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            domainListPanel.add(moreLabel);
        }

        // Total section
        JSeparator totalSeparator = new JSeparator();
        totalSeparator.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
        totalSeparator.setForeground(BORDER_COLOR);

        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setOpaque(false);
        totalPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));

        JLabel totalLabel = new JLabel("Tổng cộng");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel totalValueLabel = new JLabel(totalPriceLabel.getText());
        totalValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalValueLabel.setForeground(ACCENT_COLOR);
        totalValueLabel.setHorizontalAlignment(JLabel.RIGHT);

        totalPanel.add(totalLabel, BorderLayout.WEST);
        totalPanel.add(totalValueLabel, BorderLayout.EAST);

        // Add components to summary panel
        summaryPanel.add(summaryTitleLabel);
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        summaryPanel.add(domainListPanel);
        summaryPanel.add(totalSeparator);
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        summaryPanel.add(totalPanel);

        // Payment method section
        JLabel paymentLabel = new JLabel("Phương thức thanh toán");
        paymentLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        paymentLabel.setForeground(TEXT_PRIMARY);
        paymentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel paymentMethodsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        paymentMethodsPanel.setBackground(Color.WHITE);
        paymentMethodsPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
        paymentMethodsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Payment method buttons
        String[] methods = { "Thẻ tín dụng", "Chuyển khoản", "Ví điện tử" };
        ButtonGroup methodGroup = new ButtonGroup();

        for (String method : methods) {
            JRadioButton radioButton = new JRadioButton(method);
            radioButton.setFont(FONT_REGULAR);
            radioButton.setBackground(Color.WHITE);
            methodGroup.add(radioButton);
            paymentMethodsPanel.add(radioButton);

            // Select first method by default
            if (method.equals(methods[0])) {
                radioButton.setSelected(true);
            }
        }

        // Action buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton cancelButton = new JButton("Hủy");
        cancelButton.setFont(FONT_REGULAR);
        cancelButton.setForeground(TEXT_PRIMARY);
        cancelButton.setBackground(Color.WHITE);
        cancelButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton confirmButton = new JButton("Xác nhận thanh toán") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient
                GradientPaint gradient = new GradientPaint(
                        0, 0, ACCENT_COLOR,
                        getWidth(), getHeight(), new Color(255, 132, 41));
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                super.paintComponent(g2);
            }
        };
        confirmButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setBorderPainted(false);
        confirmButton.setContentAreaFilled(false);
        confirmButton.setFocusPainted(false);
        confirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmButton.addActionListener(e -> {
            processPayment();
            dialog.dispose();
        });

        buttonsPanel.add(cancelButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonsPanel.add(confirmButton);

        // Add all components to main panel
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(separator);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(summaryPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(paymentLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(paymentMethodsPanel);
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(buttonsPanel);

        dialog.add(mainPanel);
        return dialog;
    }

    /**
     * Process domain payment and create orders
     */
    private void processPayment() {
        // Show loading indicator
        JOptionPane loadingPane = new JOptionPane("Đang xử lý thanh toán...", JOptionPane.INFORMATION_MESSAGE);
        JDialog loadingDialog = loadingPane.createDialog(this, "Xử lý");
        loadingDialog.setModal(false);
        loadingDialog.setVisible(true);

        try {
            // Simulate payment processing
            Thread.sleep(1500);

            // Create order in a database
            boolean success = domainService.createOrderForCart(currentUser.getId(), totalPrice, cartDomains);

            // Hide loading dialog
            loadingDialog.setVisible(false);

            if (success) {
                // Show a success message
                JOptionPane.showMessageDialog(
                        this,
                        "Thanh toán thành công! Đơn hàng của bạn đang được xử lý.",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);

                // Reload data
                loadDomainsFromDatabase();
            } else {
                // Show an error message
                JOptionPane.showMessageDialog(
                        this,
                        "Có lỗi xảy ra khi xử lý đơn hàng. Vui lòng thử lại sau.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadingDialog.setVisible(false);

            JOptionPane.showMessageDialog(
                    this,
                    "Có lỗi xảy ra: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Add a domain to cart
     */
    public void addDomain(Domain domain) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Check if domain already exists in cart
            String checkQuery = "SELECT COUNT(*) FROM cart WHERE user_id = ? AND domain_id = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, currentUser.getId());
                checkStmt.setInt(2, domain.getId());
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Tên miền này đã có trong giỏ hàng của bạn.",
                            "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }

            // Insert into cart
            String insertQuery = "INSERT INTO cart (user_id, domain_id, rental_period_id) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setInt(1, currentUser.getId());
                insertStmt.setInt(2, domain.getId());
                insertStmt.setInt(3, 1); // Default to 1 year rental period

                int result = insertStmt.executeUpdate();

                if (result > 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Đã thêm tên miền " + domain.getName() + domain.getExtension() + " vào giỏ hàng.",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Reload data
                    loadDomainsFromDatabase();
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Không thể thêm tên miền vào giỏ hàng.",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi khi thêm tên miền vào giỏ hàng: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Remove domain from cart
     */
    private void removeDomain(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= cartDomains.size()) {
            return;
        }

        Domain domain = cartDomains.get(rowIndex);

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "DELETE FROM cart WHERE user_id = ? AND domain_id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, currentUser.getId());
                stmt.setInt(2, domain.getId());

                int result = stmt.executeUpdate();

                if (result > 0) {
                    // Remove from data
                    tableModel.removeRow(rowIndex);
                    cartDomains.remove(rowIndex);

                    // Chỉ cập nhật lại STT nếu bảng còn dòng
                    int rowCount = tableModel.getRowCount();
                    if (rowCount > 0) {
                        for (int i = 0; i < rowCount; i++) {
                            tableModel.setValueAt(i + 1, i, 0);
                        }
                    }

                    // Cập nhật lại tổng tiền
                    updateTotalPrice();

                    // Nếu giỏ hàng trống, chuyển sang panel EMPTY
                    if (cartDomains.isEmpty()) {
                        Container container = (Container) getComponent(1);
                        CardLayout cl = (CardLayout) container.getLayout();
                        cl.show(container, "EMPTY");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể xóa tên miền khỏi giỏ hàng: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Button renderer for the Actions column
     */
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setForeground(new Color(231, 76, 60));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            setText(value.toString());
            return this;
        }
    }

    /**
     * Button editor for the Actions column
     */
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setForeground(new Color(231, 76, 60));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            label = value.toString();
            button.setText(label);
            currentRow = row;
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                removeDomain(currentRow);
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}