package frontEnd.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import backEnd.DatabaseConnection;

public class TestLogin extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public TestLogin() {
        // Thiết lập frame
        setTitle("Hệ Thống Quản Lý Tên Miền - Đăng Nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        // Tạo panel chính với nền gradient
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                Color color1 = new Color(66, 139, 202);
                Color color2 = new Color(231, 233, 251);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(new BorderLayout());

        // Panel tiêu đề
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("ĐĂNG NHẬP HỆ THỐNG", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Panel biểu mẫu
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tên đăng nhập
        JLabel usernameLabel = new JLabel("Tên đăng nhập:");
        usernameLabel.setForeground(Color.WHITE);
        usernameField = new JTextField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(usernameField, gbc);

        // Mật khẩu
        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setForeground(Color.WHITE);
        passwordField = new JPasswordField(20);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(passwordField, gbc);

        // Panel nút bấm
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        loginButton = new JButton("Đăng nhập");
        registerButton = new JButton("Đăng ký");

        // Tạo kiểu cho các nút
        loginButton.setBackground(new Color(0, 102, 204));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);

        registerButton.setBackground(new Color(41, 128, 185));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 5, 0);
        formPanel.add(buttonPanel, gbc);

        // Thêm các panel vào panel chính
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Thêm panel chính vào frame
        add(mainPanel);

        // Thêm các xử lý sự kiện
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (authenticateUser(username, password)) {
                    JOptionPane.showMessageDialog(TestLogin.this,
                            "Đăng nhập thành công!",
                            "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE);
                    openMainApplication();
                } else {
                    JOptionPane.showMessageDialog(TestLogin.this,
                            "Tên đăng nhập hoặc mật khẩu không chính xác!",
                            "Lỗi đăng nhập",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRegistrationForm();
            }
        });
    }

    private boolean authenticateUser(String username, String password) {
        // Biến lưu kết quả xác thực
        boolean isAuthenticated = false;

        // Thực hiện kết nối và truy vấn cơ sở dữ liệu
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {

            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                // Nếu có dòng trả về, xác thực thành công
                if (resultSet.next()) {
                    isAuthenticated = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }

        return isAuthenticated;
    }

    private void openMainApplication() {
        // Mã để mở cửa sổ ứng dụng chính
        this.dispose(); // Đóng cửa sổ đăng nhập
        // Tạo và hiển thị cửa sổ ứng dụng chính
        JOptionPane.showMessageDialog(null,
                "Đăng nhập thành công! Chức năng chính đang được phát triển.",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void openRegistrationForm() {
        // Mã để mở biểu mẫu đăng ký
        // Ví dụ:
        // RegisterForm registerForm = new RegisterForm();
        // registerForm.setVisible(true);
        JOptionPane.showMessageDialog(this,
                "Chức năng đăng ký đang được phát triển.",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        // Thiết lập giao diện
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Tạo và hiển thị biểu mẫu đăng nhập
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TestLogin loginForm = new TestLogin();
                loginForm.setVisible(true);
            }
        });
    }
}