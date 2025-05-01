package view.UserView;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.User;
import repository.DatabaseConnection;
import service.DomainExtensionService;
import utils.UserSession;
import view.UserView.panels.*;

public class UserDashboardView extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private User loggedInUser;
    private final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private List<JButton> menuButtons = new ArrayList<>();
    private DomainExtensionService domainExtensionService = new DomainExtensionService();

    // Card names for panels
    private static final String DASHBOARD_PANEL = "DASHBOARD_PANEL";
    private static final String SEARCH_DOMAIN_PANEL = "SEARCH_DOMAIN_PANEL";
    private static final String MY_DOMAINS_PANEL = "MY_DOMAINS_PANEL";
    private static final String ORDERS_PANEL = "ORDERS_PANEL";
    private static final String PROFILE_PANEL = "PROFILE_PANEL";
    private static final String SUPPORT_PANEL = "SUPPORT_PANEL";

    /**
     * Constructor for UserDashboardView.
     */
    public UserDashboardView() {
        this.loggedInUser = UserSession.getInstance().getCurrentUser();
    
        if (loggedInUser == null) {
            JOptionPane.showMessageDialog(this, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            dispose();
            new view.Login().setVisible(true);
            return;
        }
    
        initialize();
    }

    /**
     * Initialize the frame.
     */
    private void initialize() {
        setTitle("Hệ Thống Quản Lý Tên Miền - Trang Người Dùng");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1200, 700);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Sidebar Panel
        JPanel sidebarPanel = createSidebarPanel();
        contentPane.add(sidebarPanel, BorderLayout.WEST);

        // Main Content Panel with CardLayout
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        // Initialize MyDomainsPanel
        MyDomainsPanel myDomainsPanel = new MyDomainsPanel(loggedInUser);

        // Add panels to CardLayout
        mainContentPanel.add(new view.UserView.panels.HomePanel(cardLayout, mainContentPanel), DASHBOARD_PANEL);
        mainContentPanel.add(new SearchDomainPanel(domainExtensionService, myDomainsPanel), SEARCH_DOMAIN_PANEL);
        mainContentPanel.add(myDomainsPanel, MY_DOMAINS_PANEL);
        mainContentPanel.add(new OrdersPanel(), ORDERS_PANEL);
        mainContentPanel.add(new view.UserView.panels.ProfilePanel(loggedInUser, this), PROFILE_PANEL);
        mainContentPanel.add(new view.UserView.panels.SupportPanel(), SUPPORT_PANEL);

        contentPane.add(mainContentPanel, BorderLayout.CENTER);

        // Show the default panel (Dashboard)
        switchPanel(DASHBOARD_PANEL);

        // Highlight the first menu button (Dashboard)
        if (!menuButtons.isEmpty()) {
            updateMenuButtonColors(menuButtons.get(0));
        }
    }

    /**
     * Create the header panel.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(1200, 60));
        headerPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Hệ Thống Quản Lý Tên Miền");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(new EmptyBorder(0, 20, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userInfoPanel.setOpaque(false);

        String username = loggedInUser != null ? loggedInUser.getUsername() : "Người dùng";
        JLabel userLabel = new JLabel(username);
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> logout());

        userInfoPanel.add(userLabel);
        userInfoPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        userInfoPanel.add(logoutButton);
        userInfoPanel.setBorder(new EmptyBorder(0, 0, 0, 20));

        headerPanel.add(userInfoPanel, BorderLayout.EAST);

        return headerPanel;
    }

    /**
     * Create the sidebar panel.
     */
    private JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setBackground(new Color(240, 240, 240));
        sidebarPanel.setPreferredSize(new Dimension(200, 600));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));

        // Menu items
        String[] menuItems = { "Trang chính", "Tìm kiếm tên miền", "Tên miền của tôi", "Đơn hàng", "Thông tin cá nhân",
                "Hỗ trợ" };

        for (String item : menuItems) {
            JButton menuButton = createMenuButton(item);
            sidebarPanel.add(menuButton);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        return sidebarPanel;
    }

    /**
     * Create a menu button.
     */
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(200, 40));
        button.setMaximumSize(new Dimension(200, 40));
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBackground(new Color(240, 240, 240)); // Default background color
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        // Add button to the list
        menuButtons.add(button);

        button.addActionListener(e -> {
            // Update button colors
            updateMenuButtonColors(button);

            switch (text) {
                case "Trang chính":
                    switchPanel(DASHBOARD_PANEL);
                    break;
                case "Tìm kiếm tên miền":
                    switchPanel(SEARCH_DOMAIN_PANEL);
                    break;
                case "Tên miền của tôi":
                    MyDomainsPanel myDomainsPanel = (MyDomainsPanel) mainContentPanel.getComponent(2); // Lấy MyDomainsPanel
                    myDomainsPanel.loadDomainsFromDatabase(); // Tải dữ liệu
                    switchPanel(MY_DOMAINS_PANEL);
                    break;
                case "Đơn hàng":
                    OrdersPanel ordersPanel = (OrdersPanel) mainContentPanel.getComponent(3); // Lấy OrdersPanel
                    updateOrdersPanel(ordersPanel); // Tải dữ liệu
                    switchPanel(ORDERS_PANEL);
                    break;
                case "Thông tin cá nhân":
                    switchPanel(PROFILE_PANEL);
                    break;
                case "Hỗ trợ":
                    switchPanel(SUPPORT_PANEL);
                    break;
            }
            });

        return button;
    }

    /**
     * Log out the user.
     */
    private void logout() {
        // Clear the current user session
        UserSession.getInstance().clearSession();

        // Return to the login screen
        dispose();
        new view.Login().setVisible(true);
    }

    /**
     * Switch the displayed panel.
     */
    private void switchPanel(String cardName) {
        cardLayout.show(mainContentPanel, cardName);
    }

    /**
     * Update the colors of the menu buttons.
     */
    private void updateMenuButtonColors(JButton selectedButton) {
        for (JButton button : menuButtons) {
            if (button == selectedButton) {
                button.setBackground(new Color(200, 200, 255)); // Selected background color
            } else {
                button.setBackground(new Color(240, 240, 240)); // Default background color
            }
        }
    }

    private void updateOrdersPanel(OrdersPanel ordersPanel) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Sử dụng JOIN để lấy thông tin tên miền từ bảng domains
            String query = "SELECT d.name + d.extension AS domain_name, o.total_price, o.created_at, o.status " +
                        "FROM orders o " +
                        "JOIN domains d ON o.domain_id = d.id " +
                        "WHERE o.buyer_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, loggedInUser.getId()); // Lấy ID người dùng hiện tại
                ResultSet rs = stmt.executeQuery();

                ordersPanel.clearTable(); // Xóa dữ liệu cũ

                while (rs.next()) {
                    String domainName = rs.getString("domain_name");
                    double totalPrice = rs.getDouble("total_price");
                    Timestamp paymentDate = rs.getTimestamp("created_at");
                    String status = rs.getString("status");

                    // Thêm dữ liệu vào OrdersPanel
                    ordersPanel.addOrder(domainName, totalPrice, paymentDate, status);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải thông tin đơn hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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