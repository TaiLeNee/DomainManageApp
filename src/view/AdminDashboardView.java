package view;

import view.adminDashboard.DomainExtensionPanel;
import view.adminDashboard.DomainManagementPanel;
import view.adminDashboard.OrderManagementPanel;
import view.adminDashboard.ReportingPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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
        cardLayout = new CardLayout();
        right.setLayout(cardLayout);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1920, 1080);
        this.setLayout(new BorderLayout());
        this.setTitle("Admin Dashboard");
        setLocationRelativeTo(null);
        setResizable(false);

        setupLeftPanel();
        setupRightPanel();

        this.add(left, BorderLayout.WEST);
        this.add(right, BorderLayout.CENTER);
    }

    private void setupLeftPanel() {
        left.setPreferredSize(new Dimension(366, 1080));
        left.setBackground(new Color(0, 102, 102));
        left.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 50, 0);

        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(0, 102, 102));
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel title = new JLabel("ADMIN");
        title.setFont(new Font("Segoe UI Black", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        titlePanel.add(title);
        left.add(titlePanel, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(0, 102, 102));
        buttonPanel.setLayout(new GridBagLayout());
        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.fill = GridBagConstraints.HORIZONTAL;
        buttonGbc.weightx = 1;
        buttonGbc.insets = new Insets(10, 0, 10, 0);

        JButton button1 = createFunctionButton("Phần mở rộng tên miền", EXTENSION_PANEL);
        JButton button2 = createFunctionButton("Quản lý tên miền", DOMAIN_MNG_PANEL);
        JButton button3 = createFunctionButton("Quản lý đơn hàng", ORDER_MNG_PANEL);
        JButton button4 = createFunctionButton("Báo cáo & thống kê", REPORT_PANEL);

        buttonGbc.gridy = 0;
        buttonPanel.add(button1, buttonGbc);
        buttonGbc.gridy = 1;
        buttonPanel.add(button2, buttonGbc);
        buttonGbc.gridy = 2;
        buttonPanel.add(button3, buttonGbc);
        buttonGbc.gridy = 3;
        buttonPanel.add(button4, buttonGbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 100, 0);
        left.add(buttonPanel, gbc);

        // Logout Panel
        JPanel logoutPanel = new JPanel();
        logoutPanel.setBackground(new Color(0, 102, 102));
        logoutPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(0, 102, 102));
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

        //Nhấn nút đăng xuất và quay trở lại cửa sổ đăng nhập
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int answer = JOptionPane.showConfirmDialog(null, "Bạn có chắc chắn muốn đăng xuất không?", "Đăng xuất", JOptionPane.YES_NO_OPTION);
                if(answer == 0) {
                    JOptionPane.showMessageDialog(null, "Bạn sẽ được quay lại cửa sổ đăng nhập", "Có", JOptionPane.PLAIN_MESSAGE);
                    // Create and show the Login window
                    SwingUtilities.invokeLater(() -> {
                        Login login = new Login();
                        login.setVisible(true);
                    });
                    dispose();
                }
            }
        });

        logoutPanel.add(logoutButton);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        left.add(logoutPanel, gbc);
    }

    private JButton createFunctionButton(String text, String panelKey) {
        JButton button = new JButton(text);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 24);
        Color buttonbg = new Color(0, 102, 102);
        Color buttontext = Color.WHITE;
        button.setFont(buttonFont);
        button.setBackground(buttonbg);
        button.setForeground(buttontext);
        button.setBorder(null);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(320, 60));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button != selectedButton) {
                    button.setForeground(Color.BLACK);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button != selectedButton) {
                    button.setForeground(buttontext);
                }
            }
        });

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedButton != null) {
                    selectedButton.setBackground(buttonbg);
                    selectedButton.setForeground(buttontext);
                }
                selectedButton = button;
                selectedButton.setBackground(buttonbg);
                selectedButton.setForeground(Color.BLACK);
                cardLayout.show(right, panelKey);
                System.out.println("Đã chọn: " + selectedButton.getText());
            }
        });
        return button;
    }

    private void setupRightPanel() {
        DomainExtensionPanel domainExtensionPanel = new DomainExtensionPanel();
        DomainManagementPanel domainManagementPanel = new DomainManagementPanel();
        OrderManagementPanel orderManagementPanel = new OrderManagementPanel();
        ReportingPanel reportingPanel = new ReportingPanel();

        right.add(domainExtensionPanel, EXTENSION_PANEL);
        right.add(domainManagementPanel, DOMAIN_MNG_PANEL);
        right.add(orderManagementPanel, ORDER_MNG_PANEL);
        right.add(reportingPanel, REPORT_PANEL);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                AdminDashboardView adminDashboardView = new AdminDashboardView();
                adminDashboardView.setVisible(true);
            }
        });
    }
}