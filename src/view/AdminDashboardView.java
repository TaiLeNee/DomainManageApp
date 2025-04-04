package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AdminDashboardView extends JFrame {

    private JPanel left;
    private JPanel right;
    private JButton selectedButton = null;

    private final String EXTENSION_PANEL = "EXTENSION_PANEL";
    private final String DOMAIN_MNG_PANEL = "DOMAIN_MNG_PANEL";
    private final String ORDER_MNG_PANEL = "ORDER_MNG_PANEL";
    private final String REPORT_PANEL = "REPORT_PANEL";

    private CardLayout cardLayout;

    public AdminDashboardView() {
        left = new JPanel();
        right = new JPanel();
        cardLayout = new CardLayout(); // Tạo instance CardLayout
        right.setLayout(cardLayout); // Đặt layout cho right panel

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1920, 1080);
        this.setLayout(new BorderLayout());
        this.setTitle("Admin Dashboard");

        setupLeftPanel();
        setupRightPanel();

        this.add(left, BorderLayout.WEST);
        this.add(right, BorderLayout.EAST);

    }

    //Hàm thiết lập panel trái
    private void setupLeftPanel() {//Thiết lập panel trái
        left.setPreferredSize(new Dimension(366,1080));
        left.setBackground(new Color(0, 102,102));
        left.setLayout(new BorderLayout());

        //----TIÊU ĐỀ----
        // Panel chứa tiêu đề
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(0, 102, 102));
        titlePanel.setPreferredSize(new Dimension(336, 150));
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Tiêu đề ADMIN
        JLabel title = new JLabel("ADMIN");
        title.setFont(new Font("Segoe UI Black", Font.BOLD, 48));
        title.setForeground(Color.WHITE);

        //Thêm title vào panel
        titlePanel.add(title);
        left.add(titlePanel, BorderLayout.NORTH);

        //--4 BUTTON CHỨC NĂNG--
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 50));
        wrapper.setBackground(new Color(0, 102, 102));
        //Panel chứa nút bấm
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(336, 500));
        buttonPanel.setBackground(new Color(0, 102, 102));
        buttonPanel.setLayout(new GridLayout(4, 1, 20, 20));

        //Tạo 4 button
        JButton button1 = createFunctionButton("Phần mở rộng tên miền", EXTENSION_PANEL);
        JButton button2 = createFunctionButton("Quản lý tên miền", DOMAIN_MNG_PANEL);
        JButton button3 = createFunctionButton("Quản lý đơn hàng", ORDER_MNG_PANEL);
        JButton button4 = createFunctionButton("Báo cáo & thống kê", REPORT_PANEL);

        buttonPanel.add(button1);
        buttonPanel.add(button2);
        buttonPanel.add(button3);
        buttonPanel.add(button4);

        wrapper.add(buttonPanel);
        left.add(wrapper, BorderLayout.CENTER);

        //--BUTTON ĐĂNG XUẤT--

        //Tạo panel chứa button đăng xuất
        JPanel logoutPanel = new JPanel();
        logoutPanel.setBackground(new Color(0,102,102));
        logoutPanel.setPreferredSize(new Dimension(336, 80));
        logoutPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        //Tạo nút bấm đăng xuất (tương tự 4 button trên nhưng nhỏ hơn)
        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(0,102,102));
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(null);
        logoutButton.setPreferredSize(new Dimension(200, 40));

        logoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutButton.setForeground(Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutButton.setForeground(Color.WHITE);
            }
        });

        logoutPanel.add(logoutButton);
        left.add(logoutPanel, BorderLayout.SOUTH);

    }

    private JButton createFunctionButton(String text, String panelKey) {
        JButton button = new JButton(text);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 24);
        Color buttonbg = new Color(0,102,102);
        Color buttontext = Color.WHITE;
        Color selectedtext = Color.BLACK;
        button.setFont(buttonFont);
        button.setBackground(buttonbg);
        button.setForeground(buttontext);
        button.setBorder(null);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(320, 60));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Listener khi di chuột (chỉ thay đổi nếu nút không được chọn)
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button != selectedButton) { // Chỉ đổi màu nếu không phải nút đang chọn
                    button.setForeground(Color.BLACK);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button != selectedButton) { // Chỉ reset màu nếu không phải nút đang chọn
                    button.setForeground(buttontext);
                }
            }
        });

        // Listener khi nhấn nút (để chọn nút)
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 1. Reset nút được chọn trước đó (nếu có)
                if (selectedButton != null) {
                    selectedButton.setBackground(buttonbg);
                    selectedButton.setForeground(buttontext);
                }

                // 2. Cập nhật nút được chọn mới
                selectedButton = button; // Gán nút vừa nhấn là nút đang chọn
                selectedButton.setBackground(buttonbg);
                selectedButton.setForeground(Color.BLACK);
                cardLayout.show(right, panelKey);
                System.out.println("Đã chọn: " + selectedButton.getText()); // In ra console để kiểm tra
            }
        });
        return button;
    }

    //Hàm thiết lập cho panel phải
    private void setupRightPanel() {
        // Tạo các instance của panel con
        DomainExtensionPanel domainExtensionPanel = new DomainExtensionPanel();
        DomainManagementPanel domainManagementPanel = new DomainManagementPanel();
        OrderManagementPanel orderManagementPanel = new OrderManagementPanel();
        ReportingPanel reportingPanel = new ReportingPanel();

        // Thêm các panel con vào right panel với key tương ứng
        right.add(domainExtensionPanel, EXTENSION_PANEL);
        right.add(domainManagementPanel, DOMAIN_MNG_PANEL);
        right.add(orderManagementPanel, ORDER_MNG_PANEL);
        right.add(reportingPanel, REPORT_PANEL);

        // Không cần đặt preferredSize cho right nếu nó nằm ở BorderLayout.CENTER
        // Nó sẽ tự động chiếm không gian còn lại.
    }

    public static void main(String[] args) {
        // Chạy giao diện trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            AdminDashboardView adminDashboard = new AdminDashboardView();
            adminDashboard.setVisible(true);
        });
    }
}