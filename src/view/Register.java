package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import model.User;
import repository.DatabaseConnection;
import utils.ValidationUtils;

@SuppressWarnings("serial")
public class Register extends JFrame {
	// Khai báo các thành phần UI
	private JTextField fullnameField;
	private JTextField emailField;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JPasswordField confirmPasswordField;
	private JButton registerButton;

	// Màu sắc chủ đạo
	private final Color PRIMARY_COLOR = new Color(41, 128, 185);
	private final Color SECONDARY_COLOR = new Color(52, 152, 219);
	private final Color BACKGROUND_COLOR = new Color(245, 247, 250);
	private final Color TEXT_COLOR = new Color(44, 62, 80);
	private final Color FIELD_BACKGROUND = new Color(255, 255, 255);
	private final Color FIELD_BORDER = new Color(189, 195, 199);
	private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
	private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
	private final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
	private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 16);

	public Register() {
		setTitle("Đăng ký - Quản lý tên miền");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1000, 650);
		setLocationRelativeTo(null);
		setResizable(false);

		// Panel chính với BorderLayout
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(BACKGROUND_COLOR);

		// Tạo panel bên trái với gradient và hình ảnh
		JPanel leftPanel = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				// Vẽ gradient background
				GradientPaint gradient = new GradientPaint(0, 0, PRIMARY_COLOR, 0, getHeight(), SECONDARY_COLOR);
				g2d.setPaint(gradient);
				g2d.fillRect(0, 0, getWidth(), getHeight());

				// Vẽ hình ảnh
				ImageIcon icon = new ImageIcon("src/img/domain_banner.png");
				int imgWidth = Math.min(icon.getIconWidth(), getWidth() - 40);
				int imgHeight = (int) (imgWidth * ((double) icon.getIconHeight() / icon.getIconWidth()));
				g2d.drawImage(icon.getImage(), (getWidth() - imgWidth) / 2, (getHeight() - imgHeight) / 2,
						imgWidth, imgHeight, this);

				// Vẽ hiệu ứng overlay
				g2d.setColor(new Color(0, 0, 0, 50));
				g2d.fillRect(0, 0, getWidth(), getHeight());

				g2d.dispose();
			}
		};

		// Thêm nội dung bên trái
		JPanel leftContentPanel = new JPanel(new BorderLayout());
		leftContentPanel.setOpaque(false);
		leftContentPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

		JLabel welcomeLabel = new JLabel("Domain Manager", SwingConstants.CENTER);
		welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
		welcomeLabel.setForeground(Color.WHITE);

		JLabel sloganLabel = new JLabel("Quản lý tên miền một cách chuyên nghiệp", SwingConstants.CENTER);
		sloganLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
		sloganLabel.setForeground(Color.WHITE);

		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.setOpaque(false);
		northPanel.add(welcomeLabel, BorderLayout.CENTER);
		northPanel.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);

		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.setOpaque(false);
		southPanel.add(sloganLabel, BorderLayout.CENTER);

		leftContentPanel.add(northPanel, BorderLayout.NORTH);
		leftContentPanel.add(southPanel, BorderLayout.SOUTH);
		leftPanel.add(leftContentPanel);

		// Tạo panel bên phải
		JPanel rightPanel = new JPanel();
		rightPanel.setBackground(BACKGROUND_COLOR);
		rightPanel.setBorder(new EmptyBorder(40, 50, 40, 50));
		rightPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);

		// Tạo tiêu đề
		JLabel titleLabel = new JLabel("Đăng ký tài khoản", SwingConstants.CENTER);
		titleLabel.setFont(TITLE_FONT);
		titleLabel.setForeground(PRIMARY_COLOR);

		// Tạo panel chứa form
		JPanel formPanel = createFormPanel();

		// Thêm các thành phần vào panel phải
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.insets = new Insets(0, 0, 30, 0);
		rightPanel.add(titleLabel, gbc);

		gbc.gridy = 1;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(0, 0, 0, 0);
		rightPanel.add(formPanel, gbc);

		// Thêm panel trái và phải vào panel chính
		mainPanel.add(leftPanel, BorderLayout.WEST);
		mainPanel.add(rightPanel, BorderLayout.CENTER);

		// Đặt kích thước cho các panel
		leftPanel.setPreferredSize(new Dimension(450, 650));
		rightPanel.setPreferredSize(new Dimension(550, 650));

		setContentPane(mainPanel);

		// Thiết lập các sự kiện
		setupEventHandlers();
	}

	/**
	 * Tạo panel chứa form đăng ký
	 */
	private JPanel createFormPanel() {
		JPanel formPanel = new JPanel(new GridBagLayout());
		formPanel.setBackground(BACKGROUND_COLOR);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 0, 20, 0);

		// Tạo các trường nhập liệu
		fullnameField = createStyledTextField("Họ và tên");
		emailField = createStyledTextField("Email");
		usernameField = createStyledTextField("Tên đăng nhập");
		passwordField = createStyledPasswordField("Mật khẩu");
		confirmPasswordField = createStyledPasswordField("Xác nhận mật khẩu");

		// Tạo nút đăng ký
		registerButton = createStyledButton("ĐĂNG KÝ");

		// Thêm các trường vào form
		gbc.gridx = 0;
		gbc.gridy = 0;
		JLabel fullnameLabel = createStyledLabel("Họ và tên");
		formPanel.add(fullnameLabel, gbc);

		gbc.gridy = 1;
		gbc.insets = new Insets(0, 0, 15, 0);
		formPanel.add(fullnameField, gbc);

		gbc.gridy = 2;
		gbc.insets = new Insets(5, 0, 5, 0);
		JLabel emailLabel = createStyledLabel("Email");
		formPanel.add(emailLabel, gbc);

		gbc.gridy = 3;
		gbc.insets = new Insets(0, 0, 15, 0);
		formPanel.add(emailField, gbc);

		gbc.gridy = 4;
		gbc.insets = new Insets(5, 0, 5, 0);
		JLabel usernameLabel = createStyledLabel("Tên đăng nhập");
		formPanel.add(usernameLabel, gbc);

		gbc.gridy = 5;
		gbc.insets = new Insets(0, 0, 15, 0);
		formPanel.add(usernameField, gbc);

		gbc.gridy = 6;
		gbc.insets = new Insets(5, 0, 5, 0);
		JLabel passwordLabel = createStyledLabel("Mật khẩu");
		formPanel.add(passwordLabel, gbc);

		gbc.gridy = 7;
		gbc.insets = new Insets(0, 0, 15, 0);
		JPanel passwordPanel = createPasswordPanel(passwordField, "eye.png", "eye_off.png");
		formPanel.add(passwordPanel, gbc);

		gbc.gridy = 8;
		gbc.insets = new Insets(5, 0, 5, 0);
		JLabel confirmPasswordLabel = createStyledLabel("Xác nhận mật khẩu");
		formPanel.add(confirmPasswordLabel, gbc);

		gbc.gridy = 9;
		gbc.insets = new Insets(0, 0, 25, 0);
		JPanel confirmPasswordPanel = createPasswordPanel(confirmPasswordField, "eye.png", "eye_off.png");
		formPanel.add(confirmPasswordPanel, gbc);

		gbc.gridy = 10;
		gbc.insets = new Insets(10, 0, 20, 0);
		formPanel.add(registerButton, gbc);

		// Panel cho đường link đăng nhập
		JPanel loginLinkPanel = new JPanel(new CustomFlowLayout(FlowLayout.CENTER));
		loginLinkPanel.setOpaque(false);

		JLabel textLabel = new JLabel("Bạn đã có tài khoản? ");
		textLabel.setFont(LABEL_FONT);
		textLabel.setForeground(TEXT_COLOR);

		JLabel linkLabel = new JLabel("<html><u>Đăng nhập ngay</u></html>");
		linkLabel.setFont(LABEL_FONT);
		linkLabel.setForeground(PRIMARY_COLOR);
		linkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

		loginLinkPanel.add(textLabel);
		loginLinkPanel.add(linkLabel);

		gbc.gridy = 11;
		gbc.insets = new Insets(0, 0, 0, 0);
		formPanel.add(loginLinkPanel, gbc);

		// Sự kiện click vào đường link đăng nhập
		linkLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Login loginForm = new Login();
				loginForm.setVisible(true);
				setVisible(false);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				linkLabel.setText("<html><u style='color: #1A5276;'>Đăng nhập ngay</u></html>");
			}

			@Override
			public void mouseExited(MouseEvent e) {
				linkLabel.setText("<html><u>Đăng nhập ngay</u></html>");
			}
		});

		return formPanel;
	}

	/**
	 * Tạo JLabel có kiểu dáng thống nhất
	 */
	private JLabel createStyledLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(LABEL_FONT);
		label.setForeground(TEXT_COLOR);
		return label;
	}

	/**
	 * Tạo JTextField có kiểu dáng thống nhất
	 */
	private JTextField createStyledTextField(String placeholder) {
		JTextField textField = new JTextField(20);
		textField.setFont(INPUT_FONT);
		textField.setForeground(TEXT_COLOR);
		textField.setBackground(FIELD_BACKGROUND);
		textField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
				BorderFactory.createEmptyBorder(10, 15, 10, 15)));
		textField.setPreferredSize(new Dimension(textField.getPreferredSize().width, 45));

		// Thêm placeholder
		textField.setForeground(Color.GRAY);
		textField.setText(placeholder);

		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (textField.getText().equals(placeholder)) {
					textField.setText("");
					textField.setForeground(TEXT_COLOR);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (textField.getText().isEmpty()) {
					textField.setForeground(Color.GRAY);
					textField.setText(placeholder);
				}
			}
		});

		return textField;
	}

	/**
	 * Tạo JPasswordField có kiểu dáng thống nhất
	 */
	private JPasswordField createStyledPasswordField(String placeholder) {
		JPasswordField passwordField = new JPasswordField(20);
		passwordField.setFont(INPUT_FONT);
		passwordField.setForeground(TEXT_COLOR);
		passwordField.setBackground(FIELD_BACKGROUND);
		passwordField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
				BorderFactory.createEmptyBorder(10, 15, 10, 15)));
		passwordField.setPreferredSize(new Dimension(passwordField.getPreferredSize().width, 45));
		passwordField.setEchoChar((char) 0); // Hiện text để hiển thị placeholder

		// Thêm placeholder
		passwordField.setForeground(Color.GRAY);
		passwordField.setText(placeholder);

		passwordField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (String.valueOf(passwordField.getPassword()).equals(placeholder)) {
					passwordField.setText("");
					passwordField.setEchoChar('•');
					passwordField.setForeground(TEXT_COLOR);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (passwordField.getPassword().length == 0) {
					passwordField.setEchoChar((char) 0);
					passwordField.setForeground(Color.GRAY);
					passwordField.setText(placeholder);
				}
			}
		});

		return passwordField;
	}

	/**
	 * Tạo panel chứa trường mật khẩu và button hiện/ẩn mật khẩu
	 */
	private JPanel createPasswordPanel(JPasswordField passwordField, String eyeOpenIconPath, String eyeClosedIconPath) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(FIELD_BACKGROUND);
		panel.setBorder(BorderFactory.createLineBorder(FIELD_BORDER, 1, true));

		// Xóa border của password field và thêm vào panel
		passwordField.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
		panel.add(passwordField, BorderLayout.CENTER);

		// Thêm nút toggle password
		ImageIcon eyeOpenIcon = new ImageIcon("src/img/" + eyeOpenIconPath);
		ImageIcon eyeClosedIcon = new ImageIcon("src/img/" + eyeClosedIconPath);

		JButton toggleButton = new JButton(eyeClosedIcon);
		toggleButton.setBackground(FIELD_BACKGROUND);
		toggleButton.setPreferredSize(new Dimension(45, 45));
		toggleButton.setContentAreaFilled(false);
		toggleButton.setBorderPainted(false);
		toggleButton.setFocusPainted(false);
		toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

		toggleButton.addActionListener(new ActionListener() {
			private boolean isVisible = false;

			@Override
			public void actionPerformed(ActionEvent e) {
				isVisible = !isVisible;
				if (isVisible) {
					if (!String.valueOf(passwordField.getPassword()).equals(passwordField.getName())) {
						passwordField.setEchoChar((char) 0);
					}
					toggleButton.setIcon(eyeOpenIcon);
				} else {
					if (!String.valueOf(passwordField.getPassword()).equals(passwordField.getName())) {
						passwordField.setEchoChar('•');
					}
					toggleButton.setIcon(eyeClosedIcon);
				}
			}
		});

		panel.add(toggleButton, BorderLayout.EAST);

		return panel;
	}

	/**
	 * Tạo JButton có kiểu dáng thống nhất
	 */
	private JButton createStyledButton(String text) {
		JButton button = new JButton(text) {
			@Override
			protected void paintComponent(Graphics g) {
				if (getModel().isPressed()) {
					g.setColor(PRIMARY_COLOR);
				} else if (getModel().isRollover()) {
					g.setColor(SECONDARY_COLOR);
				} else {
					g.setColor(PRIMARY_COLOR);
				}
				g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
				super.paintComponent(g);
			}
		};

		button.setForeground(Color.WHITE);
		button.setFont(BUTTON_FONT);
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setOpaque(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setPreferredSize(new Dimension(button.getPreferredSize().width, 50));

		return button;
	}

	/**
	 * Thiết lập các sự kiện cho form
	 */
	private void setupEventHandlers() {
		registerButton.addActionListener(new ActionListener() {
			@SuppressWarnings("static-access")
			@Override
			public void actionPerformed(ActionEvent e) {
				// Lấy thông tin từ form
				String fullName = getFieldValue(fullnameField, "Họ và tên");
				String user = getFieldValue(usernameField, "Tên đăng nhập");
				String pass = getFieldValue(passwordField, "Mật khẩu");
				String confirmPass = getFieldValue(confirmPasswordField, "Xác nhận mật khẩu");
				String email = getFieldValue(emailField, "Email");
				String role = "user";

				// Kiểm tra thông tin
				if (new ValidationUtils().isNotEmpty(fullName)) {
					if (new ValidationUtils().isValidEmail(email)) {
						if (new ValidationUtils().isValidUsername(user)) {
							if (pass.equals(confirmPass)) {
								// Đăng ký tài khoản
								String query = "INSERT INTO users(fullName, username, password, email, role) VALUES(?, ?, ?, ?, ?) ";
								try (Connection conn = DatabaseConnection.getConnection();
										PreparedStatement stmt = conn.prepareStatement(query,
												Statement.RETURN_GENERATED_KEYS)) {

									stmt.setString(1, fullName);
									stmt.setString(2, user);
									stmt.setString(3, pass);
									stmt.setString(4, email);
									stmt.setString(5, role);
									stmt.executeUpdate();

									try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
										if (generatedKeys.next()) {
											int userId = generatedKeys.getInt(1);
											// Tạo đối tượng User mới
											User newUser = new User(userId, fullName, user, pass, email, role);
											showCustomMessage("Đăng ký thành công", "Tài khoản đã được tạo thành công!",
													JOptionPane.INFORMATION_MESSAGE);

											// Chuyển đến trang đăng nhập
											Login login = new Login();
											login.setVisible(true);
											setVisible(false);
										}
									}
								} catch (SQLException ex) {
									ex.printStackTrace();
									showCustomMessage("Lỗi", "Lỗi khi đăng ký: " + ex.getMessage(),
											JOptionPane.ERROR_MESSAGE);
								}
							} else {
								showCustomMessage("Thông báo", "Xác nhận mật khẩu không khớp!",
										JOptionPane.WARNING_MESSAGE);
							}
						} else {
							showCustomMessage("Thông báo",
									"Tên đăng nhập phải gồm 4-20 ký tự, chỉ chứa chữ cái, số và dấu gạch dưới!",
									JOptionPane.WARNING_MESSAGE);
						}
					} else {
						showCustomMessage("Thông báo", "Email không đúng định dạng!", JOptionPane.WARNING_MESSAGE);
					}
				} else {
					showCustomMessage("Thông báo", "Họ và tên không được để trống!", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
	}

	/**
	 * Lấy giá trị từ text field, loại bỏ placeholder
	 */
	private String getFieldValue(JTextField field, String placeholder) {
		String value = field.getText();
		return value.equals(placeholder) ? "" : value;
	}

	/**
	 * Hiển thị thông báo tùy chỉnh
	 */
	private void showCustomMessage(String title, String message, int messageType) {
		UIManager.put("OptionPane.background", BACKGROUND_COLOR);
		UIManager.put("Panel.background", BACKGROUND_COLOR);
		UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));
		UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.PLAIN, 14));
		UIManager.put("OptionPane.messageForeground", TEXT_COLOR);

		JOptionPane.showMessageDialog(this, message, title, messageType);
	}

	public static void main(String[] args) {
		// Thiết lập giao diện
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Tạo và hiển thị biểu mẫu đăng ký
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Register registerForm = new Register();
				registerForm.setVisible(true);
			}
		});
	}

	/**
	 * FlowLayout tùy chỉnh để tạo căn giữa cho panel liên kết
	 */
	private class CustomFlowLayout extends java.awt.FlowLayout {
		public CustomFlowLayout(int align) {
			super(align);
			setHgap(5);
			setVgap(0);
		}

		@Override
		public Dimension preferredLayoutSize(Container target) {
			return layoutSize(target, true);
		}

		private Dimension layoutSize(Container target, boolean preferred) {
			synchronized (target.getTreeLock()) {
				Dimension dim = new Dimension(0, 0);
				int nmembers = target.getComponentCount();
				boolean firstVisibleComponent = true;

				for (int i = 0; i < nmembers; i++) {
					Component m = target.getComponent(i);
					if (m.isVisible()) {
						Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
						dim.height = Math.max(dim.height, d.height);

						if (firstVisibleComponent) {
							firstVisibleComponent = false;
						} else {
							dim.width += getHgap();
						}

						dim.width += d.width;
					}
				}

				Insets insets = target.getInsets();
				dim.width += insets.left + insets.right + getHgap() * 2;
				dim.height += insets.top + insets.bottom + getVgap() * 2;
				return dim;
			}
		}
	}
}