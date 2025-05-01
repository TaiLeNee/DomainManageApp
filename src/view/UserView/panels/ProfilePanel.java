package view.UserView.panels;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.User;
import service.UserService;

public class ProfilePanel extends JPanel {
    private static final Color BG_COLOR = new Color(245, 245, 245);
    private static final Color PRIMARY_COLOR = new Color(0, 102, 102);
    private static final Color ACCENT_COLOR = new Color(255, 153, 0);

    private User loggedInUser;
    private JFrame parentFrame;

    private JLabel avatarLabel;
    private JLabel nameLabel;
    private JLabel usernameLabel;
    private JLabel emailLabel;

    public ProfilePanel(User loggedInUser, JFrame parentFrame) {
        this.loggedInUser = loggedInUser;
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        initComponents();
    }

    private void initComponents() {
        // Panel chính chứa thông tin người dùng
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Panel bên trái chứa hình ảnh đại diện
        JPanel avatarPanel = new JPanel();
        avatarPanel.setBackground(BG_COLOR);
        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));

        avatarLabel = new JLabel();
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setIcon(loadAvatarImage("src\\img\\user-icon.png")); // Đường dẫn mặc định
        avatarLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        avatarLabel.setPreferredSize(new Dimension(150, 150));
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton changeAvatarButton = new JButton("Đổi ảnh đại diện");
        changeAvatarButton.setFont(new Font("Arial", Font.PLAIN, 12));
        changeAvatarButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        changeAvatarButton.addActionListener(e -> changeAvatar());

        avatarPanel.add(avatarLabel);
        avatarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        avatarPanel.add(changeAvatarButton);

        // Panel bên phải chứa thông tin người dùng
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(BG_COLOR);

        nameLabel = new JLabel("Họ và tên: " + loggedInUser.getFullName());
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameLabel = new JLabel("Username: " + loggedInUser.getUsername());
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        emailLabel = new JLabel("Email: " + loggedInUser.getEmail());
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(usernameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(emailLabel);

        // Panel chứa các nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(BG_COLOR);

        JButton editButton = new JButton("Chỉnh sửa thông tin");
        editButton.setFont(new Font("Arial", Font.BOLD, 14));
        editButton.setBackground(PRIMARY_COLOR);
        editButton.setForeground(Color.BLACK);
        editButton.setFocusPainted(false);
        editButton.addActionListener(e -> openEditDialog());

        JButton changePasswordButton = new JButton("Đổi mật khẩu");
        changePasswordButton.setFont(new Font("Arial", Font.BOLD, 14));
        changePasswordButton.setBackground(ACCENT_COLOR);
        changePasswordButton.setForeground(Color.BLACK);
        changePasswordButton.setFocusPainted(false);
        changePasswordButton.addActionListener(e -> openChangePasswordDialog());

        buttonPanel.add(editButton);
        buttonPanel.add(changePasswordButton);

        // Thêm các panel vào mainPanel
        mainPanel.add(avatarPanel, BorderLayout.WEST);
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Thêm mainPanel vào ProfilePanel
        add(mainPanel, BorderLayout.CENTER);
    }

    private ImageIcon loadAvatarImage(String path) {
        File file = new File(path);
        if (file.exists()) {
            return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH));
        } else {
            return new ImageIcon(new ImageIcon("path/to/default-avatar.png").getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH));
        }
    }

    private void changeAvatar() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn ảnh đại diện");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Hình ảnh", "jpg", "png", "jpeg"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            avatarLabel.setIcon(loadAvatarImage(selectedFile.getAbsolutePath()));
            JOptionPane.showMessageDialog(this, "Đổi ảnh đại diện thành công!");
        }
    }

    private void openEditDialog() {
        JDialog editDialog = new JDialog(parentFrame, "Chỉnh sửa thông tin", true);
        editDialog.setSize(400, 250);
        editDialog.setLocationRelativeTo(parentFrame);
        editDialog.setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(new EmptyBorder(20, 20, 0, 20));
        inputPanel.add(new JLabel("Họ và tên:"));
        JTextField nameField = new JTextField(loggedInUser.getFullName());
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField(loggedInUser.getEmail());
        inputPanel.add(emailField);

        JPanel buttonPanel = new JPanel();
        JButton saveBtn = new JButton("Lưu");
        JButton cancelBtn = new JButton("Hủy");
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        editDialog.add(inputPanel, BorderLayout.CENTER);
        editDialog.add(buttonPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(ev -> {
            String newName = nameField.getText().trim();
            String newEmail = emailField.getText().trim();

            if (newName.isEmpty() || newEmail.isEmpty()) {
                JOptionPane.showMessageDialog(editDialog, "Không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Gọi UserService để cập nhật thông tin
            UserService userService = new UserService();
            boolean success = userService.updateUserInfo(loggedInUser.getId(), newName, newEmail);

            if (success) {
                JOptionPane.showMessageDialog(editDialog, "Cập nhật thành công!");
                editDialog.dispose();
                // Cập nhật lại thông tin hiển thị
                nameLabel.setText("Họ và tên: " + newName);
                emailLabel.setText("Email: " + newEmail);
            } else {
                JOptionPane.showMessageDialog(editDialog, "Không thể cập nhật!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(ev -> editDialog.dispose());

        editDialog.setVisible(true);
    }

    private void openChangePasswordDialog() {
        JDialog changePasswordDialog = new JDialog(parentFrame, "Đổi mật khẩu", true);
        changePasswordDialog.setSize(400, 250);
        changePasswordDialog.setLocationRelativeTo(parentFrame);
        changePasswordDialog.setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(new EmptyBorder(20, 20, 0, 20));
        inputPanel.add(new JLabel("Mật khẩu cũ:"));
        JPasswordField oldPasswordField = new JPasswordField();
        inputPanel.add(oldPasswordField);
        inputPanel.add(new JLabel("Mật khẩu mới:"));
        JPasswordField newPasswordField = new JPasswordField();
        inputPanel.add(newPasswordField);
        inputPanel.add(new JLabel("Xác nhận mật khẩu:"));
        JPasswordField confirmPasswordField = new JPasswordField();
        inputPanel.add(confirmPasswordField);

        JPanel buttonPanel = new JPanel();
        JButton saveBtn = new JButton("Lưu");
        JButton cancelBtn = new JButton("Hủy");
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        changePasswordDialog.add(inputPanel, BorderLayout.CENTER);
        changePasswordDialog.add(buttonPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(ev -> {
            String oldPassword = new String(oldPasswordField.getPassword()).trim();
            String newPassword = new String(newPasswordField.getPassword()).trim();
            String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(changePasswordDialog, "Không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(changePasswordDialog, "Mật khẩu mới không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Gọi UserService để đổi mật khẩu
            UserService userService = new UserService();
            boolean success = userService.changePassword(loggedInUser.getId(), oldPassword, newPassword);

            if (success) {
                JOptionPane.showMessageDialog(changePasswordDialog, "Đổi mật khẩu thành công!");
                changePasswordDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(changePasswordDialog, "Mật khẩu cũ không đúng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(ev -> changePasswordDialog.dispose());

        changePasswordDialog.setVisible(true);
    }
}