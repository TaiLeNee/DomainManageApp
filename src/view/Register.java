package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public class Register extends JFrame{
	private JTextField fullnameField;
	private JTextField emailField;
	private JTextField usernameField;
	private	JPasswordField passwordField;
	private JPasswordField confirmpasswordField;
	private JButton registerButton;
	
	public Register() {
		setTitle("Đăng ký - Quản lý tên miền");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 600);
		setLocationRelativeTo(null);
		setResizable(false);
		
		JPanel mainPanel = new JPanel(new GridLayout(1, 2));
		
		JPanel leftPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				ImageIcon icon = new ImageIcon("src/img/domain_banner.png"); // Bạn cần đặt hình ở đúng path này
				g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), this);
			}
		};
		mainPanel.add(leftPanel);
		
		JPanel rightPanel = new JPanel();
		rightPanel.setBackground(Color.lightGray);
		rightPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL; //Set layout cho các thành phần trong GridBagLayout !important
		
		JLabel titleLabel = new JLabel("Đăng ký");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setForeground(new Color(0, 102, 204));
		
		JLabel fullnameLabel = new JLabel("Ho và tên:");
		fullnameField = new JTextField(20);
		fullnameField.setMinimumSize(fullnameField.getPreferredSize()); // Đặt kích thước tối thiểu cho fullnameField !important

		JLabel emailLabel = new JLabel("Email:");
		emailField = new JTextField(20);
		
		JLabel usernameLabel = new JLabel("Tên Đăng nhập:");
		usernameField = new JTextField(20);
		
		JLabel passwordLabel = new JLabel("Mật khẩu:");
		passwordField = new JPasswordField(20);
		
		JLabel confirmpasswordLabel = new JLabel("Xác nhận mật khẩu:");
		confirmpasswordField = new JPasswordField(20);
		
		registerButton = new JButton("Đăng ký");
		JLabel textLabel = new JLabel("Bạn đã có tài khoản?");
        JLabel linkLabel = new JLabel("<html><a href='#'>Đăng nhập ngay</a></html>");
        linkLabel.setForeground(Color.BLUE);
        linkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Khi click vào đường link, chuyển sang form chính
        linkLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Chuyển sang form chính khi click vào đường link
            	Login loginForm = new Login();
            	loginForm.setVisible(true);
                setVisible(false); // Ẩn form đăng nhập
            }
        });
        rightPanel.add(linkLabel);
        
        ImageIcon eyeOpenIcon1 = new ImageIcon("src/img/eye.png");
        ImageIcon eyeClosedIcon1 = new ImageIcon("src/img/eye_off.png");
        
        JButton toggleButton1 = new JButton(eyeClosedIcon1);
        
        
        ImageIcon eyeOpenIcon2 = new ImageIcon("src/img/eye.png");
        ImageIcon eyeClosedIcon2 = new ImageIcon("src/img/eye_off.png");
        
        JButton toggleButton2 = new JButton(eyeClosedIcon2);
        
		
        // Style button
        registerButton.setBackground(new Color(0, 102, 204));
        registerButton.setFont(new Font("Arial", Font.BOLD, 12));
        registerButton.setPreferredSize(new Dimension(210, 50));
        registerButton.setForeground(Color.WHITE);
        
        toggleButton1.setBackground(Color.lightGray);
        toggleButton1.setPreferredSize(new Dimension(30, 30));
        toggleButton1.setContentAreaFilled(false);
        toggleButton1.setBorderPainted(false);
        toggleButton1.setFocusPainted(false);
        
        toggleButton2.setBackground(Color.lightGray);
        toggleButton2.setPreferredSize(new Dimension(30, 30));
        toggleButton2.setContentAreaFilled(false);
        toggleButton2.setBorderPainted(false);
        toggleButton2.setFocusPainted(false);
        
        // Add components to rightPanel
        gbc.insets = new Insets(10, 10, 10, 10);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 20;
		rightPanel.add(titleLabel, gbc);
		
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		rightPanel.add(fullnameLabel, gbc);
		gbc.gridx = 1;
		rightPanel.add(fullnameField, gbc);
		
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		rightPanel.add(emailLabel, gbc);
		gbc.gridx = 1;
		rightPanel.add(emailField, gbc);
		
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		rightPanel.add(usernameLabel, gbc);
		gbc.gridx = 1;
		rightPanel.add(usernameField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 4;
		rightPanel.add(passwordLabel, gbc);
		gbc.gridx = 1;
		rightPanel.add(passwordField, gbc);
		// Panel chứa passwordField + icon
		JPanel passwordPanel = new JPanel(new BorderLayout());
		passwordPanel.add(passwordField, BorderLayout.CENTER);
		passwordPanel.add(toggleButton1, BorderLayout.EAST);
		
		gbc.gridx = 1;
		gbc.gridy = 4;
		rightPanel.add(passwordPanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 5;
		rightPanel.add(confirmpasswordLabel, gbc);
		gbc.gridx = 1;
		rightPanel.add(confirmpasswordField, gbc);
		// Panel chứa confirmpasswordField + icon
		JPanel confirmpasswordPanel = new JPanel(new BorderLayout());
		confirmpasswordPanel.add(confirmpasswordField, BorderLayout.CENTER);
		confirmpasswordPanel.add(toggleButton2, BorderLayout.EAST);
		
		gbc.gridx = 1;
		gbc.gridy = 5;
		rightPanel.add(confirmpasswordPanel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 6;
		rightPanel.add(registerButton, gbc);
		
		gbc.insets = new Insets(10, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.gridwidth = 1; // Cả hai label sẽ chiếm cùng một hàng
		gbc.anchor = GridBagConstraints.WEST;
		rightPanel.add(textLabel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 7;
		gbc.anchor = GridBagConstraints.WEST; 
		rightPanel.add(linkLabel, gbc);

		mainPanel.add(rightPanel);

		add(mainPanel);
		
		registerButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
		
				
			}
		});
		toggleButton1.addActionListener(new ActionListener() {
		    private boolean isVisible = false;

		    public void actionPerformed(ActionEvent e) {
		        isVisible = !isVisible;
		        if (isVisible) {
		            passwordField.setEchoChar((char) 0); // Hiện
		            toggleButton1.setIcon(eyeOpenIcon1);
		        } else {
		            passwordField.setEchoChar('*'); // Ẩn
		            toggleButton1.setIcon(eyeClosedIcon1);
		        }
		    }
		});
		
		toggleButton2.addActionListener(new ActionListener() {
		    private boolean isVisible = false;

		    public void actionPerformed(ActionEvent e) {
		        isVisible = !isVisible;
		        if (isVisible) {
		        	confirmpasswordField.setEchoChar((char) 0); // Hiện
		            toggleButton2.setIcon(eyeOpenIcon2);
		        } else {
		            passwordField.setEchoChar('*'); // Ẩn
		            toggleButton2.setIcon(eyeClosedIcon2);
		        }
		    }
		});
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
           	Register registerForm = new Register();
           	registerForm.setVisible(true);
           }
       });
   }
}