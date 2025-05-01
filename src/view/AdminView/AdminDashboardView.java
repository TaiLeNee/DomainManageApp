package view.AdminView;

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;
import model.User;
import repository.DomainRepository;
import repository.OrderRepository;
import repository.RentalPeriodRepository;
import repository.TransactionRepository;
import repository.UserRepository;
import service.DomainExtensionService;
import service.DomainService;
import service.UserService;
import utils.UserSession;
import view.AdminView.panels.*;
import view.Login;

public class AdminDashboardView extends JFrame {
    // Panels chính
    private JPanel mainPanel;
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private JPanel headerPanel;

    // Components cho navigation
    private JButton currentButton;
    private JLabel pageTitle;

    // CardLayout cho nội dung chính
    private CardLayout cardLayout;

    // Repository để lấy dữ liệu
    private DomainRepository domainRepository;
    private OrderRepository orderRepository;
    private UserRepository userRepository;
    private TransactionRepository transactionRepository;
    private RentalPeriodRepository rentalPeriodRepository;

    // Service
    private DomainService domainService;
    private UserService userService;
    private DomainExtensionService extensionService;

    // Constants cho card names
    private static final String DASHBOARD_PANEL = "DASHBOARD_PANEL";
    private static final String DOMAINS_PANEL = "DOMAINS_PANEL";
    private static final String EXTENSIONS_PANEL = "EXTENSIONS_PANEL";
    private static final String ORDERS_PANEL = "ORDERS_PANEL";
    private static final String REPORTS_PANEL = "REPORTS_PANEL";
    private static final String USERS_PANEL = "USERS_PANEL";

    // Màu sắc chủ đạo - Palette mới hiện đại hơn
    private static final Color PRIMARY_COLOR = new Color(41, 59, 95); // Xanh navy đậm
    private static final Color SECONDARY_COLOR = new Color(66, 91, 138); // Xanh navy nhạt hơn
    private static final Color ACCENT_COLOR = new Color(255, 111, 0); // Cam nổi bật
    private static final Color BG_COLOR = new Color(248, 250, 252); // Xám nhẹ
    private static final Color TEXT_PRIMARY = new Color(34, 40, 49); // Màu chữ chính
    private static final Color TEXT_SECONDARY = new Color(130, 139, 162); // Màu chữ phụ
    private static final Color BORDER_COLOR = new Color(230, 235, 241); // Màu đường viền

    // Font chữ
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);

    // Thông tin người dùng
    private User loggedInUser;

    // Các panel chức năng
    private DashboardPanel dashboardPanel;
    private DomainsPanel domainsPanel;
    private OrdersPanel ordersPanel;
    private UsersPanel usersPanel;
    private ExtensionsPanel extensionsPanel;
    private ReportsPanel reportsPanel;

    public AdminDashboardView() {
        // Lấy thông tin người dùng từ session
        this.loggedInUser = UserSession.getInstance().getCurrentUser();

        // Khởi tạo repository và service
        domainRepository = new DomainRepository();
        orderRepository = new OrderRepository();
        userRepository = new UserRepository();
        transactionRepository = new TransactionRepository();
        rentalPeriodRepository = new RentalPeriodRepository();

        domainService = new DomainService();
        userService = new UserService();
        extensionService = new DomainExtensionService();

        // Thiết lập giao diện hiện đại
        setupModernUI();

        // Thiết lập cơ bản cho frame
        setTitle("Hệ thống Quản lý Tên miền - Trang Quản trị");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 800));

        // Thiết lập layout chính
        mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_COLOR);
        setContentPane(mainPanel);

        // Khởi tạo các thành phần UI
        initComponents();

        // Mở rộng cửa sổ
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public AdminDashboardView(String username, String role) {
        this(); // Gọi constructor mặc định để đảm bảo tính tương thích ngược
    }

    private void setupModernUI() {
        // Thiết lập UI toàn cục
        UIManager.put("Panel.background", new ColorUIResource(BG_COLOR));
        UIManager.put("OptionPane.background", new ColorUIResource(BG_COLOR));
        UIManager.put("Button.font", FONT_BUTTON);
        UIManager.put("Label.font", FONT_REGULAR);
        UIManager.put("TextField.font", FONT_REGULAR);
        UIManager.put("ComboBox.font", FONT_REGULAR);
        UIManager.put("Table.font", FONT_REGULAR);
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("TabbedPane.font", FONT_REGULAR);

        // Button style hiện đại
        UIManager.put("Button.border", new LineBorder(BORDER_COLOR, 1, true));
        UIManager.put("Button.select", SECONDARY_COLOR);
        UIManager.put("Button.focus", SECONDARY_COLOR);
    }

    private void initComponents() {
        // Tạo sidebar
        createSidebar();

        // Tạo header
        createHeader();

        // Tạo content panel với CardLayout
        createContentPanel();

        // Thêm vào panel chính
        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        // Panel chứa header và content
        JPanel rightPanel = new JPanel(new BorderLayout(0, 0));
        rightPanel.setBackground(BG_COLOR);
        rightPanel.add(headerPanel, BorderLayout.NORTH);
        rightPanel.add(contentPanel, BorderLayout.CENTER);

        mainPanel.add(rightPanel, BorderLayout.CENTER);

        // Mặc định hiển thị dashboard
        showDashboard();
    }

    private void createSidebar() {
        sidebarPanel = new JPanel(new BorderLayout(0, 0));
        sidebarPanel.setBackground(PRIMARY_COLOR);
        sidebarPanel.setPreferredSize(new Dimension(270, getHeight()));

        // Logo panel
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(new Color(33, 47, 76)); // Tối hơn một chút so với PRIMARY_COLOR
        logoPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        logoPanel.setPreferredSize(new Dimension(270, 80));

        // Logo và tên hệ thống
        JLabel logoIcon = new JLabel(new ImageIcon("src/img/logo_small.png"));
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

        // Thêm label phân loại menu
        JLabel menuLabel = new JLabel("MENU CHÍNH");
        menuLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        menuLabel.setForeground(new Color(168, 183, 214));
        menuLabel.setBorder(new EmptyBorder(0, 10, 10, 0));
        menuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuPanel.add(menuLabel);

        // Tạo các nút menu với thiết kế mới
        JButton dashboardBtn = createMenuButton("Dashboard", "dashboard.png");
        JButton domainsBtn = createMenuButton("Quản lý Tên miền", "domain.png");
        JButton extensionsBtn = createMenuButton("Phần mở rộng", "extension.png");
        JButton ordersBtn = createMenuButton("Quản lý Đơn hàng", "order.png");
        JButton reportsBtn = createMenuButton("Báo cáo & Thống kê", "report.png");
        JButton usersBtn = createMenuButton("Quản lý Người dùng", "user.png");

        // Thêm action listeners
        dashboardBtn.addActionListener(e -> showDashboard());
        domainsBtn.addActionListener(e -> showDomainsPanel());
        extensionsBtn.addActionListener(e -> showExtensionsPanel());
        ordersBtn.addActionListener(e -> showOrdersPanel());
        reportsBtn.addActionListener(e -> showReportsPanel());
        usersBtn.addActionListener(e -> showUsersPanel());

        // Thêm các nút vào menu với khoảng cách đồng nhất
        menuPanel.add(dashboardBtn);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        menuPanel.add(domainsBtn);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        menuPanel.add(extensionsBtn);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        menuPanel.add(ordersBtn);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Thêm label phân loại menu thứ hai
        JLabel reportMenuLabel = new JLabel("BÁO CÁO");
        reportMenuLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        reportMenuLabel.setForeground(new Color(168, 183, 214));
        reportMenuLabel.setBorder(new EmptyBorder(25, 10, 10, 0));
        reportMenuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuPanel.add(reportMenuLabel);

        menuPanel.add(reportsBtn);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Thêm label phân loại menu thứ ba
        JLabel adminMenuLabel = new JLabel("QUẢN TRỊ");
        adminMenuLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        adminMenuLabel.setForeground(new Color(168, 183, 214));
        adminMenuLabel.setBorder(new EmptyBorder(25, 10, 10, 0));
        adminMenuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuPanel.add(adminMenuLabel);

        menuPanel.add(usersBtn);

        // User info panel ở dưới cùng - thiết kế mới
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(33, 47, 76)); // Tối hơn một chút
        userPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(66, 81, 114)),
                new EmptyBorder(15, 20, 15, 20)));
        userPanel.setPreferredSize(new Dimension(270, 140)); // Tăng chiều cao để chứa avatar lớn hơn

        // Sử dụng user-icon với kích thước lớn hơn
        JPanel avatarPanel = new JPanel(new BorderLayout());
        avatarPanel.setOpaque(false);
        avatarPanel.setPreferredSize(new Dimension(60, 60));
        avatarPanel.setMinimumSize(new Dimension(60, 60));
        avatarPanel.setMaximumSize(new Dimension(60, 60));

        JLabel avatarLabel = new JLabel();
        try {
            ImageIcon originalIcon = new ImageIcon("src/img/user-icon.png");
            if (originalIcon.getIconWidth() > 0) {
                Image img = originalIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                avatarLabel.setIcon(new ImageIcon(img));
            }
        } catch (Exception ex) {
            // Fallback nếu không tìm thấy icon
            avatarLabel.setText(loggedInUser.getUsername().substring(0, 1).toUpperCase());
            avatarLabel.setHorizontalAlignment(JLabel.CENTER);
            avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 40)); // Tăng kích thước font cho phù hợp
            avatarLabel.setForeground(Color.WHITE);
        }
        avatarPanel.add(avatarLabel, BorderLayout.CENTER);

        // Thay đổi bố cục panel chứa thông tin người dùng
        JPanel rightPanel = new JPanel(new BorderLayout(20, 10));
        rightPanel.setOpaque(false);

        // Nút đăng xuất với thiết kế hiện đại hơn
        JButton logoutBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 77, 77, 80));
                    g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        // Tạo icon đăng xuất với kích thước phù hợp
        try {
            ImageIcon originalIcon = new ImageIcon("src/img/logout.png");
            if (originalIcon.getIconWidth() > 0) {
                Image img = originalIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                logoutBtn.setIcon(new ImageIcon(img));
            }
        } catch (Exception ex) {
            logoutBtn.setText("Logout");
        }

        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setToolTipText("Đăng xuất");
        logoutBtn.setPreferredSize(new Dimension(40, 40));

        // Thêm hiệu ứng hover
        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutBtn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutBtn.repaint();
            }
        });

        logoutBtn.addActionListener(e -> logout());

        // Cải thiện hiển thị thông tin người dùng với bố cục phù hợp hơn
        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setOpaque(false);
        userInfo.setBorder(new EmptyBorder(30, 10, 5, 0)); // Tăng padding top từ 30px lên 40px để di chuyển xuống 10px
        userInfo.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Tạo nameLabel hiển thị tên người dùng rõ ràng hơn
        JLabel nameLabel = new JLabel(loggedInUser.getFullName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Tạo một panel có background để làm nổi bật vai trò người dùng
        JPanel roleLabelPanel = new JPanel();
        roleLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        roleLabelPanel.setOpaque(false);
        roleLabelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleLabelPanel.setBorder(new EmptyBorder(4, 0, 0, 0));

        // Cải thiện roleLabel với thiết kế "badge" hiện đại
        JLabel roleLabel = new JLabel(loggedInUser.getRole()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Vẽ background cho badge
                g2.setColor(new Color(78, 115, 223, 80));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                super.paintComponent(g2);
                g2.dispose();
            }
        };
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        roleLabel.setForeground(new Color(208, 223, 255));
        roleLabel.setBorder(new EmptyBorder(4, 8, 4, 8));

        roleLabelPanel.add(roleLabel);

        // Thêm các thành phần vào userInfo panel
        userInfo.add(nameLabel);
        userInfo.add(Box.createRigidArea(new Dimension(0, 2)));
        userInfo.add(roleLabelPanel);

        rightPanel.add(userInfo, BorderLayout.CENTER);
        rightPanel.add(logoutBtn, BorderLayout.EAST);

        // Bố cục mới: avatar bên trái, panel thông tin bên phải
        userPanel.add(avatarPanel, BorderLayout.WEST);
        userPanel.add(rightPanel, BorderLayout.CENTER);

        // Thêm vào sidebar
        sidebarPanel.add(logoPanel, BorderLayout.NORTH);
        sidebarPanel.add(new JScrollPane(menuPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
                BorderLayout.CENTER);
        sidebarPanel.add(userPanel, BorderLayout.SOUTH);

        // Mặc định chọn Dashboard
        currentButton = dashboardBtn;
        updateButtonState(dashboardBtn);
    }

    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 70));

        // Panel bên trái chứa tiêu đề
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(new EmptyBorder(0, 25, 0, 0));

        // Icon tiêu đề
        JLabel titleIcon = new JLabel(new ImageIcon("src/img/dashboard.png"));
        titleIcon.setBorder(new EmptyBorder(0, 0, 0, 15));

        // Title với thiết kế mới
        pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(FONT_TITLE);
        pageTitle.setForeground(TEXT_PRIMARY);

        titlePanel.add(titleIcon, BorderLayout.WEST);
        titlePanel.add(pageTitle, BorderLayout.CENTER);

        // Panel bên phải chứa thông tin bổ sung và các nút chức năng
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new EmptyBorder(0, 0, 0, 25));

        // Date-time label hiện đại
        JLabel dateLabel = new JLabel();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy");
        dateLabel.setText(sdf.format(new Date()));
        dateLabel.setFont(FONT_REGULAR);
        dateLabel.setForeground(TEXT_SECONDARY);
        dateLabel.setBorder(new EmptyBorder(0, 15, 0, 0));
        dateLabel.setIcon(new ImageIcon("src/img/calendar.png"));
        dateLabel.setIconTextGap(10);

        // Thêm các thành phần vào panel thông tin
        // infoPanel.add(searchField);
        // infoPanel.add(searchButton);
        infoPanel.add(dateLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(infoPanel, BorderLayout.EAST);
    }

    private void createContentPanel() {
        // Khởi tạo các panel con
        dashboardPanel = new DashboardPanel(domainRepository, orderRepository, userRepository, transactionRepository,
                domainService);
        domainsPanel = new DomainsPanel(domainRepository, this);
        extensionsPanel = new ExtensionsPanel(extensionService, this);
        ordersPanel = new OrdersPanel(orderRepository, domainRepository, userRepository, this);
        reportsPanel = new ReportsPanel();
        usersPanel = new UsersPanel(userRepository, this);

        // Tạo panel với CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_COLOR);

        // Padding cho content panel
        contentPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Thêm các panel vào cardLayout
        contentPanel.add(createContentWrapper(dashboardPanel), DASHBOARD_PANEL);
        contentPanel.add(createContentWrapper(domainsPanel), DOMAINS_PANEL);
        contentPanel.add(createContentWrapper(extensionsPanel), EXTENSIONS_PANEL);
        contentPanel.add(createContentWrapper(ordersPanel), ORDERS_PANEL);
        contentPanel.add(createContentWrapper(reportsPanel), REPORTS_PANEL);
        contentPanel.add(createContentWrapper(usersPanel), USERS_PANEL);
    }

    // Wrapper để thêm padding và border mềm mại cho mỗi panel
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

    private JButton createMenuButton(String text, String iconFile) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Nếu là nút đang chọn thì vẽ background
                if (this == currentButton) {
                    g2.setColor(SECONDARY_COLOR);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                } else if (getModel().isRollover()) {
                    // Hiệu ứng hover
                    g2.setColor(new Color(54, 74, 111));
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        // Giữ màu chữ trắng cho menu sidebar vì nền sidebar là màu tối
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(240, 50));
        button.setPreferredSize(new Dimension(240, 50));
        button.setBorder(new EmptyBorder(0, 15, 0, 0));

        // Icon với kích thước phù hợp
        try {
            ImageIcon originalIcon = new ImageIcon("src/img/" + iconFile);
            if (originalIcon.getIconWidth() > 0) {
                Image img = originalIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(img));
                button.setIconTextGap(12);
            }
        } catch (Exception ex) {
            // Tạo icon mặc định nếu không tìm thấy file
            JPanel iconPlaceholder = new JPanel();
            iconPlaceholder.setBackground(ACCENT_COLOR);
            iconPlaceholder.setPreferredSize(new Dimension(20, 20));
            button.setIcon(new ImageIcon());
        }

        // Hiệu ứng hover
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

        return button;
    }

    private void updateButtonState(JButton selectedButton) {
        if (currentButton != null) {
            currentButton.repaint();
        }
        currentButton = selectedButton;
        selectedButton.repaint();

        // Cập nhật icon cho tiêu đề
        String iconName = "";
        if (selectedButton.getText().contains("Dashboard")) {
            iconName = "dashboard.png";
        } else if (selectedButton.getText().contains("Tên miền")) {
            iconName = "domain.png";
        } else if (selectedButton.getText().contains("Phần mở rộng")) {
            iconName = "extension.png";
        } else if (selectedButton.getText().contains("Đơn hàng")) {
            iconName = "order.png";
        } else if (selectedButton.getText().contains("Báo cáo")) {
            iconName = "report.png";
        } else if (selectedButton.getText().contains("Người dùng")) {
            iconName = "user.png";
        }

        try {
            JPanel titlePanel = (JPanel) headerPanel.getComponent(0);
            JLabel titleIcon = (JLabel) titlePanel.getComponent(0);
            titleIcon.setIcon(new ImageIcon("src/img/" + iconName));
        } catch (Exception e) {
            // Ignore errors
        }
    }

    // Các phương thức chuyển panel
    private void showDashboard() {
        pageTitle.setText("Dashboard");
        cardLayout.show(contentPanel, DASHBOARD_PANEL);
    }

    private void showDomainsPanel() {
        pageTitle.setText("Quản lý Tên miền");
        // Refresh dữ liệu
        domainsPanel.loadDomainData();
        cardLayout.show(contentPanel, DOMAINS_PANEL);
    }

    private void showExtensionsPanel() {
        pageTitle.setText("Phần mở rộng");
        cardLayout.show(contentPanel, EXTENSIONS_PANEL);
    }

    private void showOrdersPanel() {
        pageTitle.setText("Quản lý Đơn hàng");
        // Refresh dữ liệu
        ordersPanel.loadOrderData();
        cardLayout.show(contentPanel, ORDERS_PANEL);
    }

    private void showReportsPanel() {
        pageTitle.setText("Báo cáo & Thống kê");
        cardLayout.show(contentPanel, REPORTS_PANEL);
    }

    private void showUsersPanel() {
        pageTitle.setText("Quản lý Người dùng");
        // Refresh dữ liệu
        usersPanel.loadUserData();
        cardLayout.show(contentPanel, USERS_PANEL);
    }

    private void logout() {
        // Dialog xác nhận đăng xuất với thiết kế mới
        JPanel confirmPanel = new JPanel(new BorderLayout(15, 10));
        confirmPanel.setBackground(Color.WHITE);
        confirmPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel iconLabel = new JLabel(new ImageIcon("src/img/question.png"));
        JLabel messageLabel = new JLabel("Bạn chắc chắn muốn đăng xuất?");
        messageLabel.setFont(FONT_SUBTITLE);

        confirmPanel.add(iconLabel, BorderLayout.WEST);
        confirmPanel.add(messageLabel, BorderLayout.CENTER);

        // Tùy chỉnh các nút của dialog
        UIManager.put("OptionPane.buttonFont", FONT_BUTTON);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                confirmPanel,
                "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Xóa thông tin đăng nhập hiện tại
            UserSession.getInstance().clearSession();

            this.dispose();
            Login loginForm = new Login();
            loginForm.setVisible(true);
        }
    }

    public static void main(String[] args) {
        // Đảm bảo sử dụng giao diện look and feel của hệ thống
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Chạy giao diện trên EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            // Tạo AdminDashboardView với session hiện tại
            AdminDashboardView dashboard = new AdminDashboardView();
            dashboard.setVisible(true);
        });
    }
}