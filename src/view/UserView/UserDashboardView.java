package view.UserView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import model.User;
import utils.UserSession;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserDashboardView extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private User loggedInUser;
    private final Color PRIMARY_COLOR = new Color(0, 102, 204);

    /**
     * Create the frame.
     */
    public UserDashboardView() {
        this.loggedInUser = UserSession.getInstance().getCurrentUser();
        initialize();
    }

    /**
     * Create the frame with user information.
     */
    public UserDashboardView(String name, String role) {
        initialize();
    }

    private void initialize() {
        setTitle("Hệ Thống Quản Lý Tên Miền - Trang Người Dùng");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1200, 700);
        setLocationRelativeTo(null);

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

        // Main Content Panel
        JPanel mainContentPanel = createMainContentPanel();
        contentPane.add(mainContentPanel, BorderLayout.CENTER);
    }

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
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });

        userInfoPanel.add(userLabel);
        userInfoPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        userInfoPanel.add(logoutButton);
        userInfoPanel.setBorder(new EmptyBorder(0, 0, 0, 20));

        headerPanel.add(userInfoPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setBackground(new Color(240, 240, 240));
        sidebarPanel.setPreferredSize(new Dimension(200, 600));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));

        // Các nút menu chính của người dùng
        String[] menuItems = { "Trang chính", "Tìm kiếm tên miền", "Tên miền của tôi", "Đơn hàng", "Thông tin cá nhân",
                "Hỗ trợ" };

        for (String item : menuItems) {
            JButton menuButton = createMenuButton(item);
            sidebarPanel.add(menuButton);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        return sidebarPanel;
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(200, 40));
        button.setMaximumSize(new Dimension(200, 40));
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBackground(new Color(240, 240, 240));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Xử lý khi nhấp vào các menu item
                // Implement sau
            }
        });

        return button;
    }

    private JPanel createMainContentPanel() {
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setBackground(Color.WHITE);
        mainContentPanel.setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Chào mừng đến với Hệ thống Quản lý Tên miền!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setBorder(new EmptyBorder(30, 0, 30, 0));

        mainContentPanel.add(welcomeLabel, BorderLayout.NORTH);

        // Nội dung chính
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Thêm các thành phần khác của dashboard tại đây

        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setBorder(null);
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);

        return mainContentPanel;
    }

    private void logout() {
        // Xóa thông tin người dùng hiện tại
        UserSession.getInstance().clearSession();

        // Quay lại màn hình đăng nhập
        dispose();
        new view.Login().setVisible(true);
    }
}