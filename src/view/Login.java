package view;

import javax.swing.*;

import backEnd.DatabaseConnection;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings("serial")
public class Login extends JFrame{
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JButton loginButton;

	public Login() {
		setTitle("Đăng nhập - Quản lý tên miền");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 600);
		setLocationRelativeTo(null);
		setResizable(false);

		// Panel chính chia làm 2 cột
		JPanel mainPanel = new JPanel(new GridLayout(1, 2));

		// Panel bên trái chứa hình ảnh
		JPanel leftPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				ImageIcon icon = new ImageIcon("src/img/domain_banner.png"); // Bạn cần đặt hình ở đúng path này
				g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), this);
			}
		};
		mainPanel.add(leftPanel);

		// Panel bên phải chứa form đăng nhập
		JPanel rightPanel = new JPanel();
		rightPanel.setBackground(Color.lightGray);
		rightPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel titleLabel = new JLabel("Đăng Nhập");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setForeground(new Color(0, 102, 204));

		JLabel usernameLabel = new JLabel("Tên đăng nhập:");
		usernameField = new JTextField(20);
		usernameField.setMinimumSize(usernameField.getPreferredSize());

		JLabel passwordLabel = new JLabel("Mật khẩu:");
		passwordField = new JPasswordField(20);

		loginButton = new JButton("Đăng nhập");
		 // Đường link chuyển đến form chính
		JLabel textLabel = new JLabel("Bạn chưa có tài khoản?");
        JLabel linkLabel = new JLabel("<html><a href='#'>Đăng ký ngay</a></html>");
        linkLabel.setForeground(Color.BLUE);
        linkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Khi click vào đường link, chuyển sang form chính
        linkLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Chuyển sang form chính khi click vào đường link
            	Register registerForm = new Register();
            	registerForm.setVisible(true);
                setVisible(false); // Ẩn form đăng nhập
            }
        });
        
        ImageIcon eyeOpenIcon = new ImageIcon("src/img/eye.png");
        ImageIcon eyeClosedIcon = new ImageIcon("src/img/eye_off.png");
        
        JButton toggleButton = new JButton(eyeClosedIcon);
        
        rightPanel.add(linkLabel);

		// Style button
		loginButton.setBackground(new Color(0, 102, 204));
		loginButton.setFont(new Font("Arial", Font.BOLD, 12));
		loginButton.setPreferredSize(new Dimension(210, 50));
		loginButton.setForeground(Color.WHITE);
		
		toggleButton.setBackground(Color.lightGray);
        toggleButton.setPreferredSize(new Dimension(30, 30));
        toggleButton.setContentAreaFilled(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setFocusPainted(false);

		

		// Add components to rightPanel
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		rightPanel.add(titleLabel, gbc);

		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		rightPanel.add(usernameLabel, gbc);
		gbc.gridx = 1;
		rightPanel.add(usernameField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		rightPanel.add(passwordLabel, gbc);
		gbc.gridx = 1;
		rightPanel.add(passwordField, gbc);
		
		// Panel chứa passwordField + icon
		JPanel passwordPanel = new JPanel(new BorderLayout());
		passwordPanel.add(passwordField, BorderLayout.CENTER);
		passwordPanel.add(toggleButton, BorderLayout.EAST);

		gbc.gridx = 1;
		gbc.gridy = 2;
		rightPanel.add(passwordPanel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 4;
		rightPanel.add(loginButton, gbc);
		
		gbc.insets = new Insets(10, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 1; // Cả hai label sẽ chiếm cùng một hàng
		gbc.anchor = GridBagConstraints.WEST;
		rightPanel.add(textLabel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.WEST; 
		rightPanel.add(linkLabel, gbc);
		
		mainPanel.add(rightPanel);

		add(mainPanel);

		// Sự kiện nút
		loginButton.addActionListener(new ActionListener() {
			@SuppressWarnings("unused")
			public void actionPerformed(ActionEvent e) {
				String user = usernameField.getText();
				String pass = new String(passwordField.getPassword());

				if(true) {
					 JOptionPane.showMessageDialog(Login.this,
	                            "Đăng nhập thành công!",
	                            "Thông báo",
	                            JOptionPane.INFORMATION_MESSAGE);
					 AdminDashboardView admin = new AdminDashboardView();
					 admin.setVisible(true);
					 setVisible(false);
				}else {
					JOptionPane.showMessageDialog(Login.this,
                            "Sai tên đăng nhập hoặc mật khẩu!",
                            "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		toggleButton.addActionListener(new ActionListener() {
		    private boolean isVisible = false;

		    public void actionPerformed(ActionEvent e) {
		        isVisible = !isVisible;
		        if (isVisible) {
		            passwordField.setEchoChar((char) 0); // Hiện
		            toggleButton.setIcon(eyeOpenIcon);
		        } else {
		            passwordField.setEchoChar('*'); // Ẩn
		            toggleButton.setIcon(eyeClosedIcon);
		        }
		    }
		});


	}
	
	@SuppressWarnings("unused")
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

	public static void main(String[] args) {
		 // Thiết lập giao diện
		try {
		    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
		    e.printStackTrace();
		}

        // Tạo và hiển thị biểu mẫu đăng nhập
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	Login loginForm = new Login();
                loginForm.setVisible(true);
            }
        });
    }
}