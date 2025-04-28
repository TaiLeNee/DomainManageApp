package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import repository.DatabaseConnection;
import repository.UserRepository;
import model.User;
import view.AdminView.AdminDashboardView;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

@SuppressWarnings("serial")
public class Login extends JFrame {
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JButton loginButton;
	private JCheckBox rememberMeCheckbox;

	// Màu sắc chủ đạo
	private final Color PRIMARY_COLOR = new Color(0, 102, 204);
	private final Color SECONDARY_COLOR = new Color(240, 240, 240);
	private final Color TEXT_COLOR = new Color(51, 51, 51);
	private final Color ERROR_COLOR = new Color(204, 0, 0);

	public Login() {
		// Thiết lập cơ bản cho cửa sổ
		setTitle("Đăng nhập - Hệ thống Quản lý Tên miền");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(900, 600);
		setLocationRelativeTo(null);
		setResizable(false);

		// Panel chính với BorderLayout
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(Color.WHITE);

		// Tạo các panel con
		JPanel leftPanel = createLeftPanel();
		JPanel rightPanel = createRightPanel();

		// Thêm panel con vào panel chính
		mainPanel.add(leftPanel, BorderLayout.WEST);
		mainPanel.add(rightPanel, BorderLayout.CENTER);

		// Thêm panel chính vào frame
		add(mainPanel);

		// Xử lý sự kiện đăng nhập
		setupEventHandlers();
	}

	private JPanel createLeftPanel() {
		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.setPreferredSize(new Dimension(400, getHeight()));
		leftPanel.setBackground(PRIMARY_COLOR);

		// Panel chứa hình ảnh
		JPanel imagePanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				ImageIcon icon = new ImageIcon("src/img/domain_banner.png");
				Image img = icon.getImage();

				// Kiểm tra và vẽ hình ảnh với kích thước phù hợp
				if (img != null) {
					g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
				} else {
					g.setColor(PRIMARY_COLOR);
					g.fillRect(0, 0, getWidth(), getHeight());
				}
			}
		};

		// Panel chứa thông tin ứng dụng
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		infoPanel.setOpaque(false);
		infoPanel.setBorder(new EmptyBorder(0, 20, 30, 20));

		JLabel appTitle = new JLabel("Hệ thống Quản lý Tên miền");
		appTitle.setForeground(Color.WHITE);
		appTitle.setFont(new Font("Arial", Font.BOLD, 24));
		appTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel appDescription = new JLabel("Giải pháp quản lý tên miền toàn diện");
		appDescription.setForeground(Color.WHITE);
		appDescription.setFont(new Font("Arial", Font.PLAIN, 16));
		appDescription.setAlignmentX(Component.CENTER_ALIGNMENT);

		infoPanel.add(Box.createVerticalGlue());
		infoPanel.add(appTitle);
		infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		infoPanel.add(appDescription);

		leftPanel.add(imagePanel, BorderLayout.CENTER);
		leftPanel.add(infoPanel, BorderLayout.SOUTH);

		return leftPanel;
	}

	private JPanel createRightPanel() {
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.setBackground(Color.WHITE);
		rightPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

		// Panel tiêu đề
		JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		headerPanel.setOpaque(false);

		JLabel titleLabel = new JLabel("Đăng Nhập");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
		titleLabel.setForeground(TEXT_COLOR);

		JLabel subtitleLabel = new JLabel("Vui lòng đăng nhập để tiếp tục");
		subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		subtitleLabel.setForeground(new Color(120, 120, 120));

		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		headerPanel.add(titleLabel);
		headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		headerPanel.add(subtitleLabel);

		// Panel chứa form đăng nhập
		JPanel formPanel = new JPanel();
		formPanel.setOpaque(false);
		formPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 0, 5, 0);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1.0;

		// Username field
		JLabel usernameLabel = new JLabel("Tên đăng nhập hoặc Email");
		usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
		usernameField = new JTextField();
		usernameField.setPreferredSize(new Dimension(300, 40));
		usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
		usernameField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 200, 200)),
				BorderFactory.createEmptyBorder(5, 10, 5, 10)
		));

		// Password field
		JLabel passwordLabel = new JLabel("Mật khẩu");
		passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));

		JPanel passwordPanel = new JPanel(new BorderLayout());
		passwordPanel.setOpaque(false);

		passwordField = new JPasswordField();
		passwordField.setPreferredSize(new Dimension(300, 40));
		passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
		passwordField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 200, 200)),
				BorderFactory.createEmptyBorder(5, 10, 5, 10)
		));

		// Button hiện/ẩn mật khẩu
		// Tạo và lưu các icon vào biến
		ImageIcon eyeClosedIcon = new ImageIcon("src/img/eye_off.png");
		ImageIcon eyeOpenIcon = new ImageIcon("src/img/eye.png");

		// Điều chỉnh kích thước icon và tạo các biến final để sử dụng trong inner class
		final ImageIcon finalEyeClosedIcon;
		final ImageIcon finalEyeOpenIcon;

		if (eyeClosedIcon.getIconWidth() > 0) {
			Image img = eyeClosedIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
			finalEyeClosedIcon = new ImageIcon(img);
		} else {
			finalEyeClosedIcon = eyeClosedIcon;
		}

		if (eyeOpenIcon.getIconWidth() > 0) {
			Image img = eyeOpenIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
			finalEyeOpenIcon = new ImageIcon(img);
		} else {
			finalEyeOpenIcon = eyeOpenIcon;
		}

		JButton toggleButton = new JButton(finalEyeClosedIcon);
		toggleButton.setContentAreaFilled(false);
		toggleButton.setBorderPainted(false);
		toggleButton.setFocusPainted(false);
		toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		toggleButton.addActionListener(new ActionListener() {
			private boolean isVisible = false;

			@Override
			public void actionPerformed(ActionEvent e) {
				isVisible = !isVisible;
				passwordField.setEchoChar(isVisible ? (char) 0 : '•');
				toggleButton.setIcon(isVisible ? finalEyeOpenIcon : finalEyeClosedIcon);
			}
		});

		passwordPanel.add(passwordField, BorderLayout.CENTER);
		passwordPanel.add(toggleButton, BorderLayout.EAST);

		// Checkbox "Ghi nhớ đăng nhập"
		JPanel optionsPanel = new JPanel(new BorderLayout());
		optionsPanel.setOpaque(false);

		rememberMeCheckbox = new JCheckBox("Ghi nhớ đăng nhập");
		rememberMeCheckbox.setFont(new Font("Arial", Font.PLAIN, 14));
		rememberMeCheckbox.setOpaque(false);

		JLabel forgotPasswordLink = new JLabel("<html><a href='#'>Quên mật khẩu?</a></html>");
		forgotPasswordLink.setFont(new Font("Arial", Font.PLAIN, 14));
		forgotPasswordLink.setCursor(new Cursor(Cursor.HAND_CURSOR));

		optionsPanel.add(rememberMeCheckbox, BorderLayout.WEST);
		optionsPanel.add(forgotPasswordLink, BorderLayout.EAST);

		// Login button
		loginButton = new JButton("Đăng nhập");
		loginButton.setFont(new Font("Arial", Font.BOLD, 16));
		loginButton.setForeground(Color.BLACK);
		loginButton.setBackground(PRIMARY_COLOR);
		loginButton.setFocusPainted(false);
		loginButton.setBorderPainted(false);
		loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		loginButton.setPreferredSize(new Dimension(300, 45));

		// Hiệu ứng hover cho nút đăng nhập
		// Sử dụng final COLOR để tránh lỗi tương tự
		final Color hoverColor = new Color(0, 82, 164);
		loginButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				loginButton.setBackground(hoverColor);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				loginButton.setBackground(PRIMARY_COLOR);
			}
		});

		// Panel đăng ký
		JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		registerPanel.setOpaque(false);

		JLabel textLabel = new JLabel("Bạn chưa có tài khoản?");
		textLabel.setFont(new Font("Arial", Font.PLAIN, 14));

		JLabel linkLabel = new JLabel("<html><a href='#'>Đăng ký ngay</a></html>");
		linkLabel.setFont(new Font("Arial", Font.BOLD, 14));
		linkLabel.setForeground(PRIMARY_COLOR);
		linkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

		// Sự kiện click vào đăng ký
		linkLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Register registerForm = new Register();
				registerForm.setVisible(true);
				setVisible(false);
			}
		});

		registerPanel.add(textLabel);
		registerPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		registerPanel.add(linkLabel);

		// Thêm các thành phần vào form
		gbc.anchor = GridBagConstraints.WEST;

		formPanel.add(usernameLabel, gbc);
		gbc.insets = new Insets(5, 0, 15, 0);
		formPanel.add(usernameField, gbc);

		gbc.insets = new Insets(10, 0, 5, 0);
		formPanel.add(passwordLabel, gbc);
		gbc.insets = new Insets(5, 0, 15, 0);
		formPanel.add(passwordPanel, gbc);

		gbc.insets = new Insets(0, 0, 20, 0);
		formPanel.add(optionsPanel, gbc);

		gbc.insets = new Insets(10, 0, 30, 0);
		formPanel.add(loginButton, gbc);

		// Layout tổng thể cho rightPanel
		rightPanel.add(headerPanel, BorderLayout.NORTH);
		rightPanel.add(formPanel, BorderLayout.CENTER);
		rightPanel.add(registerPanel, BorderLayout.SOUTH);

		return rightPanel;
	}

	private void setupEventHandlers() {
		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String usernameOrEmail = usernameField.getText().trim();
				String password = new String(passwordField.getPassword());

				if (usernameOrEmail.isEmpty() || password.isEmpty()) {
					JOptionPane.showMessageDialog(
							Login.this,
							"Vui lòng nhập đầy đủ thông tin đăng nhập!",
							"Lỗi đăng nhập",
							JOptionPane.ERROR_MESSAGE
					);
					return;
				}

				if (authenticateUser(usernameOrEmail, password)) {
					JOptionPane.showMessageDialog(
							Login.this,
							"Đăng nhập thành công!",
							"Thông báo",
							JOptionPane.INFORMATION_MESSAGE
					);

					// Chuyển đến trang dashboard
					AdminDashboardView admin = new AdminDashboardView("Admin", "admin");
					admin.setVisible(true);
					setVisible(false);
				} else {
					JOptionPane.showMessageDialog(
							Login.this,
							"Sai tên đăng nhập hoặc mật khẩu!",
							"Lỗi đăng nhập",
							JOptionPane.ERROR_MESSAGE
					);
				}
			}
		});
	}

	private boolean authenticateUser(String usernameOrEmail, String password) {
		try (Connection connection = DatabaseConnection.getConnection()) {
			UserRepository userRepository = new UserRepository(connection);
			Optional<User> user = userRepository.authenticate(usernameOrEmail, password);
			return user.isPresent();
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(
					this,
					"Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(),
					"Lỗi",
					JOptionPane.ERROR_MESSAGE
			);
			return false;
		}
	}

	public static void main(String[] args) {
		try {
			// Sử dụng giao diện hệ thống thay vì Nimbus
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Login loginForm = new Login();
				loginForm.setVisible(true);
			}
		});
	}
}