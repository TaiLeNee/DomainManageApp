package view.UserView;

import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import model.User;
import repository.DatabaseConnection;
import service.DomainExtensionService;
import utils.UserSession;
import view.UserView.panels.*;

public class UserDashboardView extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private User loggedInUser;
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private List<JButton> menuButtons = new ArrayList<>();
    private DomainExtensionService domainExtensionService = new DomainExtensionService();
    private JLabel pageTitle;
    private JButton currentButton;

    // Card names for panels
    private static final String DASHBOARD_PANEL = "DASHBOARD_PANEL";
    private static final String SEARCH_DOMAIN_PANEL = "SEARCH_DOMAIN_PANEL";
    private static final String MY_DOMAINS_PANEL = "MY_DOMAINS_PANEL";
    private static final String ORDERS_PANEL = "ORDERS_PANEL";
    private static final String PROFILE_PANEL = "PROFILE_PANEL";
    private static final String SUPPORT_PANEL = "SUPPORT_PANEL";

    // Modern color scheme
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
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);

    /**
     * Constructor for UserDashboardView.
     */
    public UserDashboardView() {
        this.loggedInUser = UserSession.getInstance().getCurrentUser();

        if (loggedInUser == null) {
            JOptionPane.showMessageDialog(this, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            dispose();
            new view.Login().setVisible(true);
            return;
        }

        setupModernUI();
        initialize();
    }

    /**
     * Set up the modern UI look and feel.
     */
    private void setupModernUI() {
        // Set up global UI properties
        UIManager.put("Panel.background", new Color(BG_COLOR.getRGB()));
        UIManager.put("OptionPane.background", new Color(BG_COLOR.getRGB()));
        UIManager.put("Button.font", FONT_BUTTON);
        UIManager.put("Label.font", FONT_REGULAR);
        UIManager.put("TextField.font", FONT_REGULAR);
        UIManager.put("ComboBox.font", FONT_REGULAR);
        UIManager.put("Table.font", FONT_REGULAR);
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("TabbedPane.font", FONT_REGULAR);
    }

    /**
     * Initialize the frame.
     */
    private void initialize() {
        setTitle("Hệ Thống Quản Lý Tên Miền - Trang Người Dùng");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1280, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        contentPane = new JPanel();
        contentPane.setBackground(BG_COLOR);
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_COLOR);
        contentPane.add(mainPanel, BorderLayout.CENTER);

        // Create sidebar
        JPanel sidebarPanel = createSidebarPanel();
        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        // Create right panel that contains header and content
        JPanel rightPanel = new JPanel(new BorderLayout(0, 0));
        rightPanel.setBackground(BG_COLOR);

        // Create header
        JPanel headerPanel = createHeaderPanel();
        rightPanel.add(headerPanel, BorderLayout.NORTH);

        // Create main content with CardLayout
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(BG_COLOR);
        mainContentPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Initialize MyDomainsPanel
        MyDomainsPanel myDomainsPanel = new MyDomainsPanel(loggedInUser);

        // Add panels to CardLayout with content wrappers for consistent styling
        mainContentPanel.add(createContentWrapper(new HomePanel(loggedInUser)), DASHBOARD_PANEL);
        mainContentPanel.add(createContentWrapper(new SearchDomainPanel(domainExtensionService, myDomainsPanel)),
                SEARCH_DOMAIN_PANEL);
        mainContentPanel.add(createContentWrapper(myDomainsPanel), MY_DOMAINS_PANEL);
        mainContentPanel.add(createContentWrapper(new OrdersPanel()), ORDERS_PANEL);
        mainContentPanel.add(createContentWrapper(new ProfilePanel(loggedInUser, this)), PROFILE_PANEL);
        mainContentPanel.add(createContentWrapper(new SupportPanel()), SUPPORT_PANEL);

        rightPanel.add(mainContentPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        // Show the default panel (Dashboard)
        switchPanel(DASHBOARD_PANEL);

        // Set first button as selected
        if (!menuButtons.isEmpty()) {
            currentButton = menuButtons.get(0);
            currentButton.setBackground(SECONDARY_COLOR);
            currentButton.setForeground(Color.WHITE);
        }
    }

    /**
     * Create a content wrapper panel with consistent styling.
     */
    private JPanel createContentWrapper(JPanel panel) {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        wrapper.setBackground(new Color(0, 0, 0, 0)); // Transparent
        wrapper.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        wrapper.add(panel, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * Create the header panel with modern design.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 70));

        // Panel for title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(new EmptyBorder(0, 25, 0, 0));

        // Create title icon
        JLabel titleIcon = new JLabel(new ImageIcon("src/img/null.png"));
        titleIcon.setBorder(new EmptyBorder(0, 0, 0, 15));

        // Modern title
        pageTitle = new JLabel("Trang chính");
        pageTitle.setFont(FONT_TITLE);
        pageTitle.setForeground(TEXT_PRIMARY);

        titlePanel.add(titleIcon, BorderLayout.WEST);
        titlePanel.add(pageTitle, BorderLayout.CENTER);

        // Panel for user info and date
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new EmptyBorder(0, 0, 0, 25));

        // Modern date-time label
        JLabel dateLabel = new JLabel();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy");
        dateLabel.setText(sdf.format(new Date()));
        dateLabel.setFont(FONT_REGULAR);
        dateLabel.setForeground(TEXT_SECONDARY);
        dateLabel.setBorder(new EmptyBorder(0, 15, 0, 15));
        dateLabel.setIcon(new ImageIcon("src/img/calendar.png"));
        dateLabel.setIconTextGap(10);

        // User avatar and info
        JPanel userPanel = createUserInfoPanel();

        infoPanel.add(dateLabel);
        infoPanel.add(userPanel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(infoPanel, BorderLayout.EAST);

        return headerPanel;
    }

    /**
     * Create user info panel with avatar and dropdown menu.
     */
    private JPanel createUserInfoPanel() {
        JPanel userPanel = new JPanel(new BorderLayout(10, 0));
        userPanel.setOpaque(false);

        // Create avatar
        JLabel avatarLabel = new JLabel();
        try {
            ImageIcon originalIcon = new ImageIcon("src/img/user-icon.png");
            if (originalIcon.getIconWidth() > 0) {
                Image img = originalIcon.getImage().getScaledInstance(36, 36, Image.SCALE_SMOOTH);
                avatarLabel.setIcon(new ImageIcon(img));
            }
        } catch (Exception ex) {
            // Fallback if icon not found
            avatarLabel.setText(loggedInUser.getUsername().substring(0, 1).toUpperCase());
            avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            avatarLabel.setForeground(Color.WHITE);
            avatarLabel.setBackground(ACCENT_COLOR);
            avatarLabel.setOpaque(true);
        }

        // Create user name and role label
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(loggedInUser.getFullName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_PRIMARY);

        JLabel roleLabel = new JLabel("Customer");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(TEXT_SECONDARY);

        userInfoPanel.add(nameLabel);
        userInfoPanel.add(roleLabel);

        // Create dropdown menu
        JPopupMenu userMenu = new JPopupMenu();
        userMenu.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        JMenuItem profileMenuItem = new JMenuItem("Thông tin cá nhân");
        profileMenuItem.setFont(FONT_REGULAR);
        profileMenuItem.addActionListener(e -> switchPanel(PROFILE_PANEL));

        JMenuItem logoutMenuItem = new JMenuItem("Đăng xuất");
        logoutMenuItem.setFont(FONT_REGULAR);
        logoutMenuItem.addActionListener(e -> logout());

        userMenu.add(profileMenuItem);
        userMenu.addSeparator();
        userMenu.add(logoutMenuItem);

        // Create dropdown button
        JButton dropdownButton = new JButton();
        dropdownButton.setIcon(new ImageIcon("src/img/dropdown.png"));
        dropdownButton.setBorderPainted(false);
        dropdownButton.setContentAreaFilled(false);
        dropdownButton.setFocusPainted(false);
        dropdownButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        dropdownButton.addActionListener(e -> {
            userMenu.show(dropdownButton, 0, dropdownButton.getHeight());
        });

        // Add components to panel
        userPanel.add(avatarLabel, BorderLayout.WEST);
        userPanel.add(userInfoPanel, BorderLayout.CENTER);
        userPanel.add(dropdownButton, BorderLayout.EAST);

        return userPanel;
    }

    /**
     * Create the sidebar panel with modern design.
     */
    private JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel(new BorderLayout(0, 0));
        sidebarPanel.setBackground(PRIMARY_COLOR);
        sidebarPanel.setPreferredSize(new Dimension(270, getHeight()));

        // Logo panel
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(new Color(33, 47, 76)); // Slightly darker than PRIMARY_COLOR
        logoPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        logoPanel.setPreferredSize(new Dimension(270, 80));

        // Logo and system name
        JLabel logoIcon = new JLabel(new ImageIcon("src/img/domain_banner.png"));
        logoIcon.setBorder(new EmptyBorder(0, 25, 0, 15));

        JLabel logoLabel = new JLabel("DOMAIN MANAGER");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoLabel.setForeground(Color.WHITE);

        logoPanel.add(logoIcon, BorderLayout.WEST);
        logoPanel.add(logoLabel, BorderLayout.CENTER);

        // Menu panel
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(PRIMARY_COLOR);
        menuPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Add menu category label
        JLabel menuLabel = new JLabel("MENU CHÍNH");
        menuLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        menuLabel.setForeground(new Color(168, 183, 214));
        menuLabel.setBorder(new EmptyBorder(0, 10, 10, 0));
        menuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuPanel.add(menuLabel);

        // Create menu buttons with modern design
        String[][] menuItems = {
                { "Trang chính", "dashboard.png", DASHBOARD_PANEL },
                { "Tạo tên miền", "search.png", SEARCH_DOMAIN_PANEL },
                { "Tên miền đã thuê", "domain.png", ORDERS_PANEL },
                { "Giỏ hàng của tôi", "cart.png", MY_DOMAINS_PANEL }
        };

        for (String[] item : menuItems) {
            JButton menuButton = createMenuButton(item[0], item[1], item[2]);
            menuPanel.add(menuButton);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        // Add support category
        JLabel supportLabel = new JLabel("HỖ TRỢ");
        supportLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        supportLabel.setForeground(new Color(168, 183, 214));
        supportLabel.setBorder(new EmptyBorder(25, 10, 10, 0));
        supportLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuPanel.add(supportLabel);

        // Support button
        JButton supportButton = createMenuButton("Trợ giúp", "help.png", SUPPORT_PANEL);
        menuPanel.add(supportButton);

        // Add logo and menu to sidebar
        sidebarPanel.add(logoPanel, BorderLayout.NORTH);
        sidebarPanel.add(new JScrollPane(menuPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
                BorderLayout.CENTER);

        return sidebarPanel;
    }

    /**
     * Create a modern menu button.
     */
    private JButton createMenuButton(String text, String iconName, String panelName) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw background when selected or hovered
                if (this == currentButton) {
                    g2.setColor(SECONDARY_COLOR);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(54, 74, 111));
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(240, 50));
        button.setPreferredSize(new Dimension(240, 50));
        button.setBorder(new EmptyBorder(0, 15, 0, 0));

        // Add icon with appropriate size
        try {
            ImageIcon originalIcon = new ImageIcon("src/img/" + iconName);
            if (originalIcon.getIconWidth() > 0) {
                Image img = originalIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(img));
                button.setIconTextGap(12);
            }
        } catch (Exception ex) {
            // Create placeholder if icon not found
            JPanel iconPlaceholder = new JPanel();
            iconPlaceholder.setBackground(ACCENT_COLOR);
            iconPlaceholder.setPreferredSize(new Dimension(20, 20));
            button.setIcon(new ImageIcon());
        }

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });

        // Add to list and add action
        menuButtons.add(button);

        button.addActionListener(e -> {
            if (currentButton != null) {
                currentButton.setBackground(PRIMARY_COLOR);
                currentButton.repaint();
            }

            currentButton = button;
            button.setBackground(SECONDARY_COLOR);
            button.repaint();

            // Update page title
            pageTitle.setText(text);

            // Update icon
            try {
                JPanel titlePanel = (JPanel) ((JPanel) mainContentPanel.getParent()).getComponent(0);
                JLabel titleIcon = (JLabel) titlePanel.getComponent(0);
                titleIcon.setIcon(new ImageIcon("src/img/" + iconName));
            } catch (Exception ex) {
                // Ignore errors
            }

            // Handle special cases
            if (ORDERS_PANEL.equals(panelName)) {
                OrdersPanel ordersPanel = (OrdersPanel) ((JPanel) mainContentPanel.getComponent(3)).getComponent(0);
                updateOrdersPanel(ordersPanel); // Load data
            } else if (MY_DOMAINS_PANEL.equals(panelName)) {
                MyDomainsPanel myDomainsPanel = (MyDomainsPanel) ((JPanel) mainContentPanel.getComponent(2))
                        .getComponent(0);
                myDomainsPanel.loadDomainsFromDatabase(); // Load data
            }

            // Switch panel
            switchPanel(panelName);
        });

        return button;
    }

    /**
     * Log out the user.
     */
    private void logout() {
        // Modern confirmation dialog
        JPanel confirmPanel = new JPanel(new BorderLayout(15, 10));
        confirmPanel.setBackground(Color.WHITE);
        confirmPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel iconLabel = new JLabel(new ImageIcon("src/img/question.png"));
        JLabel messageLabel = new JLabel("Bạn chắc chắn muốn đăng xuất?");
        messageLabel.setFont(FONT_SUBTITLE);

        confirmPanel.add(iconLabel, BorderLayout.WEST);
        confirmPanel.add(messageLabel, BorderLayout.CENTER);

        // Customize dialog buttons
        UIManager.put("OptionPane.buttonFont", FONT_BUTTON);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                confirmPanel,
                "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Clear the current user session
            UserSession.getInstance().clearSession();

            // Return to the login screen
            dispose();
            new view.Login().setVisible(true);
        }
    }

    /**
     * Switch the displayed panel.
     */
    private void switchPanel(String cardName) {
        cardLayout.show(mainContentPanel, cardName);
    }

    /**
     * Update data for the orders panel.
     */
    private void updateOrdersPanel(OrdersPanel ordersPanel) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Sửa truy vấn để không sử dụng domain_id mà sử dụng JOIN qua order_details
            String query = "SELECT od.domain_name + od.domain_extension AS domain_name, o.total_price, o.created_at, o.status "
                    +
                    "FROM orders o " +
                    "JOIN order_details od ON o.id = od.order_id " +
                    "WHERE o.buyer_id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, loggedInUser.getId()); // Get current user ID
                ResultSet rs = stmt.executeQuery();

                ordersPanel.clearTable(); // Clear old data

                while (rs.next()) {
                    String domainName = rs.getString("domain_name");
                    double totalPrice = rs.getDouble("total_price");
                    Timestamp paymentDate = rs.getTimestamp("created_at");
                    String status = rs.getString("status");

                    String formattedPrice = String.format("%,.0f VND", totalPrice);

                    // Convert Timestamp to String using DateTimeFormatter
                    String formattedDate = "";
                    if (paymentDate != null) {
                        LocalDateTime dateTime = paymentDate.toLocalDateTime();
                        formattedDate = dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    }

                    // Add data to OrdersPanel
                    ordersPanel.addDomainOrder(domainName, formattedPrice, formattedDate, status);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải thông tin đơn hàng: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Main method to run the application.
     */
    public static void main(String[] args) {
        // Ensure the application uses the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Run the application on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            UserDashboardView dashboard = new UserDashboardView();
            dashboard.setVisible(true);
        });
    }
}