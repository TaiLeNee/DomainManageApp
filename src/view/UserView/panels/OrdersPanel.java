package view.UserView.panels;

import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import model.OrderDetails;
import service.OrderDetailsService;
import utils.UserSession;

public class OrdersPanel extends JPanel {
    // Modern color palette
    private static final Color BG_COLOR = new Color(248, 250, 252);
    private static final Color PRIMARY_COLOR = new Color(41, 59, 95);
    private static final Color SECONDARY_COLOR = new Color(66, 91, 138);
    private static final Color ACCENT_COLOR = new Color(255, 111, 0);
    private static final Color TEXT_PRIMARY = new Color(34, 40, 49);
    private static final Color TEXT_SECONDARY = new Color(130, 139, 162);
    private static final Color BORDER_COLOR = new Color(230, 235, 241);

    // Status colors
    private static final Color STATUS_COMPLETE = new Color(39, 174, 96);
    private static final Color STATUS_PENDING = new Color(243, 156, 18);
    private static final Color STATUS_CANCELED = new Color(231, 76, 60);

    // Modern fonts
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private OrderDetailsService orderDetailsService;
    private JLabel emptyStateLabel;
    private JPanel tablePanel;
    private JPanel emptyStatePanel;

    public OrdersPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Initialize the service
        orderDetailsService = new OrderDetailsService();

        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Create content panel that holds table or empty state
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

        // Load user's domain orders
        loadUserDomainOrders();
    }

    /**
     * Creates the header panel with title and refresh button
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        // Title with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);

        JLabel iconLabel = new JLabel(new ImageIcon("src/img/domain.png"));

        JLabel titleLabel = new JLabel("Tên miền đã thuê");
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setForeground(TEXT_PRIMARY);

        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);

        // Refresh button
        JButton refreshButton = new JButton("Làm mới dữ liệu");
        refreshButton.setFont(FONT_REGULAR);
        refreshButton.setForeground(SECONDARY_COLOR);
        refreshButton.setBorderPainted(false);
        refreshButton.setContentAreaFilled(false);
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.setIcon(new ImageIcon("src/img/refresh.png"));

        refreshButton.addActionListener(e -> refresh());

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
        String[] columnNames = { "Tên miền", "Giá thuê", "Ngày đăng ký", "Trạng thái" };

        // Create table model that isn't editable
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        // Create modern styled table
        ordersTable = new JTable(tableModel) {
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
        ordersTable.setRowHeight(50);
        ordersTable.setShowVerticalLines(false);
        ordersTable.setGridColor(BORDER_COLOR);
        ordersTable.setSelectionBackground(new Color(232, 240, 254));
        ordersTable.setSelectionForeground(TEXT_PRIMARY);
        ordersTable.setFont(FONT_REGULAR);

        // Table header styling
        JTableHeader header = ordersTable.getTableHeader();
        header.setBackground(SECONDARY_COLOR);
        header.setForeground(Color.BLUE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBorder(null);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        // Enable table sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        ordersTable.setRowSorter(sorter);

        // Cell renderers for custom column appearance
        // Domain Name Column
        ordersTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.LEFT);
                setBorder(new EmptyBorder(0, 15, 0, 0));
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                return c;
            }
        });

        // Price Column
        ordersTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.RIGHT);
                setForeground(isSelected ? TEXT_PRIMARY : ACCENT_COLOR);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                return c;
            }
        });

        // Date Column
        ordersTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        });

        // Status Column
        ordersTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                // Create a custom panel for status with rounded corners
                JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        // Determine background color based on status
                        Color bgColor;
                        String status = value.toString();

                        switch (status.toLowerCase()) {
                            case "hoàn thành":
                            case "active":
                                bgColor = new Color(39, 174, 96, 40); // Light green
                                break;
                            case "đang xử lý":
                                bgColor = new Color(243, 156, 18, 40); // Light orange
                                break;
                            case "hủy":
                            case "cancelled":
                                bgColor = new Color(231, 76, 60, 40); // Light red
                                break;
                            default:
                                bgColor = new Color(149, 165, 166, 40); // Light gray
                        }

                        g2.setColor(bgColor);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                        g2.dispose();
                    }
                };
                statusPanel.setOpaque(false);

                // Create the status label
                JLabel statusLabel = new JLabel(value.toString());
                statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

                // Set text color based on status
                switch (value.toString().toLowerCase()) {
                    case "hoàn thành":
                    case "active":
                        statusLabel.setForeground(STATUS_COMPLETE);
                        break;
                    case "đang xử lý":
                        statusLabel.setForeground(STATUS_PENDING);
                        break;
                    case "hủy":
                    case "cancelled":
                        statusLabel.setForeground(STATUS_CANCELED);
                        break;
                    default:
                        statusLabel.setForeground(TEXT_SECONDARY);
                }

                statusPanel.add(statusLabel);
                statusPanel.setBorder(new EmptyBorder(5, 15, 5, 15));

                if (isSelected) {
                    statusPanel.setOpaque(true);
                    statusPanel.setBackground(table.getSelectionBackground());
                }

                return statusPanel;
            }
        });

        // Set column widths
        ordersTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        ordersTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        ordersTable.getColumnModel().getColumn(3).setPreferredWidth(120);

        // Add double-click listener for order details
        ordersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = ordersTable.convertRowIndexToModel(ordersTable.getSelectedRow());
                    if (row >= 0) {
                        showDomainDetails(row);
                    }
                }
            }
        });

        // Create modern scrollpane
        JScrollPane scrollPane = new JScrollPane(ordersTable);
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
     * Creates an empty state panel shown when no orders exist
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
            ImageIcon icon = new ImageIcon("src/img/empty_orders.png");
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) {
            // Fallback text if image not found
            iconLabel.setText("🌐");
            iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 72));
        }
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Empty state message
        emptyStateLabel = new JLabel("Bạn chưa thuê tên miền nào");
        emptyStateLabel.setFont(FONT_SUBTITLE);
        emptyStateLabel.setForeground(TEXT_SECONDARY);
        emptyStateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Empty state description
        JLabel descLabel = new JLabel("Tìm kiếm và thuê tên miền ngay để hiển thị tại đây");
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
            Container parent = OrdersPanel.this.getParent();
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
     * Shows order details in a modal dialog when an order is double-clicked
     */
    private void showDomainDetails(int rowIndex) {
        try {
            String domainName = (String) tableModel.getValueAt(rowIndex, 0);
            String price = (String) tableModel.getValueAt(rowIndex, 1);
            String date = (String) tableModel.getValueAt(rowIndex, 2);
            String status = (String) tableModel.getValueAt(rowIndex, 3);

            // Create modal dialog with modern styling
            JDialog dialog = new JDialog();
            dialog.setTitle("Chi tiết tên miền");
            dialog.setSize(450, 380);
            dialog.setLocationRelativeTo(this);
            dialog.setModal(true);

            // Main content panel
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBackground(Color.WHITE);
            mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

            // Header with domain name
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setOpaque(false);
            headerPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                    BorderFactory.createEmptyBorder(0, 0, 15, 0)));

            JLabel domainIcon = new JLabel(new ImageIcon("src/img/domain.png"));
            JLabel domainLabel = new JLabel(domainName);
            domainLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

            headerPanel.add(domainIcon, BorderLayout.WEST);
            headerPanel.add(domainLabel, BorderLayout.CENTER);

            // Create info rows
            JPanel detailsPanel = new JPanel();
            detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
            detailsPanel.setOpaque(false);
            detailsPanel.setBorder(new EmptyBorder(15, 0, 15, 0));

            // Status info
            JPanel statusPanel = createDetailRow("Trạng thái:", status);

            // Registration date info
            JPanel datePanel = createDetailRow("Ngày đăng ký:", date);

            // Price info
            JPanel pricePanel = createDetailRow("Giá thuê:", price);

            // Add details to panel
            detailsPanel.add(statusPanel);
            detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            detailsPanel.add(datePanel);
            detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            detailsPanel.add(pricePanel);

            // Bottom buttons
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonsPanel.setOpaque(false);

            JButton closeButton = new JButton("Đóng");
            closeButton.setFont(FONT_REGULAR);
            closeButton.setForeground(TEXT_PRIMARY);
            closeButton.setBackground(Color.WHITE);
            closeButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)));
            closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            closeButton.addActionListener(e -> dialog.dispose());

            buttonsPanel.add(closeButton);

            // Add all components to main panel
            mainPanel.add(headerPanel);
            mainPanel.add(detailsPanel);
            mainPanel.add(Box.createVerticalGlue());
            mainPanel.add(buttonsPanel);

            // Add to dialog
            dialog.add(mainPanel);
            dialog.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Không thể hiển thị chi tiết tên miền: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Creates a detail row for the order details dialog
     */
    private JPanel createDetailRow(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(FONT_REGULAR);
        labelComp.setForeground(TEXT_SECONDARY);

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueComp.setForeground(TEXT_PRIMARY);

        // Special case for status - apply color
        if (label.equals("Trạng thái:")) {
            switch (value.toLowerCase()) {
                case "hoàn thành":
                case "active":
                    valueComp.setForeground(STATUS_COMPLETE);
                    break;
                case "đang xử lý":
                    valueComp.setForeground(STATUS_PENDING);
                    break;
                case "hủy":
                case "cancelled":
                    valueComp.setForeground(STATUS_CANCELED);
                    break;
            }
        }

        panel.add(labelComp, BorderLayout.WEST);
        panel.add(valueComp, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Load the user's domain orders from the database
     */
    private void loadUserDomainOrders() {
        clearTable();

        // Get current user ID
        int userId = UserSession.getInstance().getCurrentUser().getId();

        try {
            // Get all order details for the current user
            List<OrderDetails> orderDetailsList = orderDetailsService.getOrderDetailsByUserId(userId);

            // Show appropriate panel based on whether orders exist
            Container container = (Container) getComponent(1);
            CardLayout cl = (CardLayout) container.getLayout();

            if (orderDetailsList.isEmpty()) {
                cl.show(container, "EMPTY");
                return;
            }

            cl.show(container, "TABLE");

            // Format for displaying dates
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // Add each domain to the table
            for (OrderDetails detail : orderDetailsList) {
                // Format price to include VND - Use original price or individual domain price,
                // not the total order price
                String formattedPrice = String.format("%,.0f VND",
                        detail.getOriginalPrice() > 0 ? detail.getOriginalPrice() : detail.getPrice());

                // Format date to be more readable
                String formattedDate = detail.getPurchaseDate().format(formatter);

                addDomainOrder(
                        detail.getDomainName() + detail.getDomainExtension(),
                        formattedPrice,
                        formattedDate,
                        detail.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Show empty state with error message
            Container container = (Container) getComponent(1);
            CardLayout cl = (CardLayout) container.getLayout();
            cl.show(container, "EMPTY");
            emptyStateLabel.setText("Không thể tải dữ liệu. Vui lòng thử lại sau.");
        }
    }

    /**
     * Add a domain order to the table
     */
    public void addDomainOrder(String domainName, String price, String paymentDate, String status) {
        tableModel.addRow(new Object[] {
                domainName,
                price,
                paymentDate,
                status
        });
    }

    /**
     * Legacy method for compatibility - uses the new method internally
     */
    public void addOrder(String domainName, double price, Timestamp paymentDate, String status) {
        String formattedPrice = String.format("%,.0f VND", price);

        // Convert Timestamp to LocalDateTime then format
        LocalDateTime localDateTime = paymentDate.toLocalDateTime();
        String formattedDate = localDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        addDomainOrder(domainName, formattedPrice, formattedDate, status);
    }

    /**
     * Clear all data from the table
     */
    public void clearTable() {
        tableModel.setRowCount(0);
    }

    /**
     * Refresh the order panel data
     */
    public void refresh() {
        loadUserDomainOrders();
    }
}