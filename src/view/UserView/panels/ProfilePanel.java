package view.UserView.panels;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.User;
import service.UserService;
import java.awt.geom.RoundRectangle2D;

public class ProfilePanel extends JPanel {
    // Modern color palette - matching other panels
    private static final Color BG_COLOR = new Color(248, 250, 252);
    private static final Color PRIMARY_COLOR = new Color(41, 59, 95);
    private static final Color SECONDARY_COLOR = new Color(66, 91, 138);
    private static final Color ACCENT_COLOR = new Color(255, 111, 0);
    private static final Color TEXT_PRIMARY = new Color(34, 40, 49);
    private static final Color TEXT_SECONDARY = new Color(130, 139, 162);
    private static final Color BORDER_COLOR = new Color(230, 235, 241);

    // Modern fonts
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 14);

    private User loggedInUser;
    private JFrame parentFrame;

    private JLabel avatarLabel;
    private JLabel nameValue;
    private JLabel usernameValue;
    private JLabel emailValue;
    private JLabel roleValue;
    private JLabel lastLoginValue;

    public ProfilePanel(User loggedInUser, JFrame parentFrame) {
        this.loggedInUser = loggedInUser;
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents();
    }

    private void initComponents() {
        // Create page title
        JLabel titleLabel = new JLabel("Thông tin tài khoản");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        add(titleLabel, BorderLayout.NORTH);

        // Create profile container
        JPanel profileContainer = new JPanel(new BorderLayout(30, 0));
        profileContainer.setBackground(Color.WHITE);
        profileContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1, true),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)));

        // Left panel for avatar
        JPanel avatarPanel = createAvatarPanel();

        // Right panel for info
        JPanel infoPanel = createInfoPanel();

        profileContainer.add(avatarPanel, BorderLayout.WEST);
        profileContainer.add(infoPanel, BorderLayout.CENTER);

        // Add profile container to panel
        add(profileContainer, BorderLayout.CENTER);
    }

    private JPanel createAvatarPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(200, 300));

        // Avatar container with rounded corners
        JPanel avatarContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(245, 247, 250));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        avatarContainer.setLayout(new GridBagLayout());
        avatarContainer.setPreferredSize(new Dimension(180, 180));
        avatarContainer.setMaximumSize(new Dimension(180, 180));
        avatarContainer.setOpaque(false);

        // Avatar image
        avatarLabel = new JLabel();
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setIcon(loadAvatarImage("src\\img\\user-icon.png"));
        avatarContainer.add(avatarLabel);

        // Username display below avatar
        JLabel usernameDisplay = new JLabel("@" + loggedInUser.getUsername());
        usernameDisplay.setFont(FONT_REGULAR);
        usernameDisplay.setForeground(TEXT_SECONDARY);
        usernameDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Button to change avatar - with modern styling
        JButton changeAvatarButton = new JButton("Đổi ảnh đại diện") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw border only
                g2.setColor(SECONDARY_COLOR);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));

                super.paintComponent(g2);
            }
        };
        changeAvatarButton.setFont(FONT_REGULAR);
        changeAvatarButton.setForeground(SECONDARY_COLOR);
        changeAvatarButton.setBorderPainted(false);
        changeAvatarButton.setContentAreaFilled(false);
        changeAvatarButton.setFocusPainted(false);
        changeAvatarButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        changeAvatarButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        changeAvatarButton.setMaximumSize(new Dimension(200, 40));
        changeAvatarButton.addActionListener(e -> changeAvatar());

        // Add all components to panel with spacing
        panel.add(avatarContainer);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(usernameDisplay);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(changeAvatarButton);

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // Title section
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);

        JLabel infoTitle = new JLabel("Thông tin cá nhân");
        infoTitle.setFont(FONT_SUBTITLE);
        infoTitle.setForeground(TEXT_PRIMARY);

        titlePanel.add(infoTitle);

        // Info fields
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setOpaque(false);
        fieldsPanel.setBorder(new EmptyBorder(15, 0, 20, 0));

        // Name field
        JPanel namePanel = createInfoField("Họ và tên");
        nameValue = new JLabel(loggedInUser.getFullName());
        nameValue.setFont(FONT_REGULAR);
        nameValue.setForeground(TEXT_PRIMARY);
        namePanel.add(nameValue);

        // Email field
        JPanel emailPanel = createInfoField("Email");
        emailValue = new JLabel(loggedInUser.getEmail());
        emailValue.setFont(FONT_REGULAR);
        emailValue.setForeground(TEXT_PRIMARY);
        emailPanel.add(emailValue);

        // Username field
        JPanel usernamePanel = createInfoField("Tài khoản");
        usernameValue = new JLabel(loggedInUser.getUsername());
        usernameValue.setFont(FONT_REGULAR);
        usernameValue.setForeground(TEXT_PRIMARY);
        usernamePanel.add(usernameValue);

        // Role field
        JPanel rolePanel = createInfoField("Vai trò");
        roleValue = new JLabel(loggedInUser.getRole());
        roleValue.setFont(FONT_REGULAR);
        roleValue.setForeground(TEXT_PRIMARY);
        rolePanel.add(roleValue);

        // Last login field
        JPanel lastLoginPanel = createInfoField("Đăng nhập gần đây");
        lastLoginValue = new JLabel("Hôm nay, 10:15 AM");
        lastLoginValue.setFont(FONT_REGULAR);
        lastLoginValue.setForeground(TEXT_PRIMARY);
        lastLoginPanel.add(lastLoginValue);

        fieldsPanel.add(namePanel);
        fieldsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        fieldsPanel.add(emailPanel);
        fieldsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        fieldsPanel.add(usernamePanel);
        fieldsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        fieldsPanel.add(rolePanel);
        fieldsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        fieldsPanel.add(lastLoginPanel);

        // Button section
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        // Edit button with gradient
        JButton editButton = new JButton("Chỉnh sửa thông tin") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient
                GradientPaint gradient = new GradientPaint(
                        0, 0, PRIMARY_COLOR,
                        getWidth(), getHeight(), SECONDARY_COLOR);
                g2.setPaint(gradient);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

                super.paintComponent(g2);
            }
        };
        editButton.setFont(FONT_REGULAR);
        editButton.setForeground(Color.WHITE);
        editButton.setBorderPainted(false);
        editButton.setContentAreaFilled(false);
        editButton.setFocusPainted(false);
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editButton.addActionListener(e -> openEditDialog());

        // Password button
        JButton passwordButton = new JButton("Đổi mật khẩu") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient
                GradientPaint gradient = new GradientPaint(
                        0, 0, ACCENT_COLOR,
                        getWidth(), getHeight(), new Color(255, 132, 41));
                g2.setPaint(gradient);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

                super.paintComponent(g2);
            }
        };
        passwordButton.setFont(FONT_REGULAR);
        passwordButton.setForeground(Color.WHITE);
        passwordButton.setBorderPainted(false);
        passwordButton.setContentAreaFilled(false);
        passwordButton.setFocusPainted(false);
        passwordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        passwordButton.addActionListener(e -> openChangePasswordDialog());

        buttonPanel.add(editButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        buttonPanel.add(passwordButton);

        // Add all sections to main panel
        panel.add(titlePanel);
        panel.add(fieldsPanel);
        panel.add(buttonPanel);

        return panel;
    }

    private JPanel createInfoField(String labelText) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(500, 25));

        // Label with grid to ensure consistent width
        JPanel labelPanel = new JPanel(new GridLayout(1, 1));
        labelPanel.setOpaque(false);
        labelPanel.setPreferredSize(new Dimension(150, 20));

        JLabel label = new JLabel(labelText + ":");
        label.setFont(FONT_LABEL);
        label.setForeground(TEXT_SECONDARY);
        labelPanel.add(label);

        panel.add(labelPanel);

        return panel;
    }

    private ImageIcon loadAvatarImage(String path) {
        File file = new File(path);
        if (file.exists()) {
            Image image = new ImageIcon(path).getImage();
            // Create circular crop
            BufferedImage circleBuffer = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = circleBuffer.createGraphics();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, 150, 150));
            g2.drawImage(image, 0, 0, 150, 150, null);

            g2.dispose();
            return new ImageIcon(circleBuffer);
        } else {
            // Create default avatar with initials
            BufferedImage avatar = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = avatar.createGraphics();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, SECONDARY_COLOR, 150, 150, PRIMARY_COLOR));
            g2.fillOval(0, 0, 150, 150);

            // Add initials
            String initials = getInitials(loggedInUser.getFullName());
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 50));
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(initials);
            int textHeight = fm.getHeight();
            g2.drawString(initials, (150 - textWidth) / 2, ((150 - textHeight) / 2) + fm.getAscent());

            g2.dispose();
            return new ImageIcon(avatar);
        }
    }

    private String getInitials(String fullName) {
        StringBuilder initials = new StringBuilder();
        for (String name : fullName.split(" ")) {
            if (name.length() > 0) {
                initials.append(name.charAt(0));
                if (initials.length() >= 2)
                    break;
            }
        }
        return initials.toString().toUpperCase();
    }

    private void changeAvatar() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn ảnh đại diện");
        fileChooser
                .setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Hình ảnh", "jpg", "png", "jpeg"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            avatarLabel.setIcon(loadAvatarImage(selectedFile.getAbsolutePath()));
            JOptionPane.showMessageDialog(this, "Đổi ảnh đại diện thành công!");
        }
    }

    private void openEditDialog() {
        // Create modern styled dialog
        JDialog editDialog = new JDialog(parentFrame, "Chỉnh sửa thông tin", true);
        editDialog.setSize(450, 280);
        editDialog.setLocationRelativeTo(parentFrame);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Dialog title
        JLabel titleLabel = new JLabel("Chỉnh sửa thông tin cá nhân");
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Form fields
        JPanel formPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Name field
        JPanel nameFieldPanel = new JPanel(new BorderLayout());
        nameFieldPanel.setOpaque(false);

        JLabel nameLabel = new JLabel("Họ và tên");
        nameLabel.setFont(FONT_SMALL);
        nameLabel.setForeground(TEXT_SECONDARY);

        JTextField nameField = new JTextField(loggedInUser.getFullName());
        nameField.setFont(FONT_REGULAR);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 0, 5, 0)));

        nameFieldPanel.add(nameLabel, BorderLayout.NORTH);
        nameFieldPanel.add(nameField, BorderLayout.CENTER);

        // Email field
        JPanel emailFieldPanel = new JPanel(new BorderLayout());
        emailFieldPanel.setOpaque(false);

        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(FONT_SMALL);
        emailLabel.setForeground(TEXT_SECONDARY);

        JTextField emailField = new JTextField(loggedInUser.getEmail());
        emailField.setFont(FONT_REGULAR);
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 0, 5, 0)));

        emailFieldPanel.add(emailLabel, BorderLayout.NORTH);
        emailFieldPanel.add(emailField, BorderLayout.CENTER);

        formPanel.add(nameFieldPanel);
        formPanel.add(emailFieldPanel);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton cancelBtn = new JButton("Hủy");
        cancelBtn.setFont(FONT_REGULAR);
        cancelBtn.setForeground(TEXT_PRIMARY);
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> editDialog.dispose());

        JButton saveBtn = new JButton("Lưu thay đổi") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient
                GradientPaint gradient = new GradientPaint(
                        0, 0, PRIMARY_COLOR,
                        getWidth(), getHeight(), SECONDARY_COLOR);
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                super.paintComponent(g2);
            }
        };
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorderPainted(false);
        saveBtn.setContentAreaFilled(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> {
            String newName = nameField.getText().trim();
            String newEmail = emailField.getText().trim();

            if (newName.isEmpty() || newEmail.isEmpty()) {
                JOptionPane.showMessageDialog(editDialog,
                        "Vui lòng điền đầy đủ thông tin!",
                        "Thông tin không hợp lệ",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Call UserService to update information
            UserService userService = new UserService();
            boolean success = userService.updateUserInfo(loggedInUser.getId(), newName, newEmail);

            if (success) {
                // Update displayed information
                nameValue.setText(newName);
                emailValue.setText(newEmail);

                // Show success message with modern style
                JOptionPane.showMessageDialog(editDialog,
                        "Thông tin đã được cập nhật thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);

                editDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(editDialog,
                        "Không thể cập nhật thông tin. Vui lòng thử lại sau.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        // Add components to main panel
        mainPanel.add(titleLabel);
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(buttonPanel);

        editDialog.add(mainPanel);
        editDialog.setVisible(true);
    }

    private void openChangePasswordDialog() {
        // Create modern styled dialog
        JDialog passwordDialog = new JDialog(parentFrame, "Đổi mật khẩu", true);
        passwordDialog.setSize(450, 350);
        passwordDialog.setLocationRelativeTo(parentFrame);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Dialog title
        JLabel titleLabel = new JLabel("Đổi mật khẩu");
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Form fields
        JPanel formPanel = new JPanel(new GridLayout(3, 1, 0, 15));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Current password field
        JPanel currentPasswordPanel = new JPanel(new BorderLayout());
        currentPasswordPanel.setOpaque(false);

        JLabel currentPasswordLabel = new JLabel("Mật khẩu hiện tại");
        currentPasswordLabel.setFont(FONT_SMALL);
        currentPasswordLabel.setForeground(TEXT_SECONDARY);

        JPasswordField currentPasswordField = new JPasswordField();
        currentPasswordField.setFont(FONT_REGULAR);
        currentPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 0, 5, 0)));

        currentPasswordPanel.add(currentPasswordLabel, BorderLayout.NORTH);
        currentPasswordPanel.add(currentPasswordField, BorderLayout.CENTER);

        // New password field
        JPanel newPasswordPanel = new JPanel(new BorderLayout());
        newPasswordPanel.setOpaque(false);

        JLabel newPasswordLabel = new JLabel("Mật khẩu mới");
        newPasswordLabel.setFont(FONT_SMALL);
        newPasswordLabel.setForeground(TEXT_SECONDARY);

        JPasswordField newPasswordField = new JPasswordField();
        newPasswordField.setFont(FONT_REGULAR);
        newPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 0, 5, 0)));

        newPasswordPanel.add(newPasswordLabel, BorderLayout.NORTH);
        newPasswordPanel.add(newPasswordField, BorderLayout.CENTER);

        // Confirm password field
        JPanel confirmPasswordPanel = new JPanel(new BorderLayout());
        confirmPasswordPanel.setOpaque(false);

        JLabel confirmPasswordLabel = new JLabel("Xác nhận mật khẩu mới");
        confirmPasswordLabel.setFont(FONT_SMALL);
        confirmPasswordLabel.setForeground(TEXT_SECONDARY);

        JPasswordField confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(FONT_REGULAR);
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 0, 5, 0)));

        confirmPasswordPanel.add(confirmPasswordLabel, BorderLayout.NORTH);
        confirmPasswordPanel.add(confirmPasswordField, BorderLayout.CENTER);

        formPanel.add(currentPasswordPanel);
        formPanel.add(newPasswordPanel);
        formPanel.add(confirmPasswordPanel);

        // Password requirements text
        JLabel requirementsLabel = new JLabel(
                "<html>Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số</html>");
        requirementsLabel.setFont(FONT_SMALL);
        requirementsLabel.setForeground(TEXT_SECONDARY);
        requirementsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton cancelBtn = new JButton("Hủy");
        cancelBtn.setFont(FONT_REGULAR);
        cancelBtn.setForeground(TEXT_PRIMARY);
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> passwordDialog.dispose());

        JButton saveBtn = new JButton("Cập nhật mật khẩu") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient
                GradientPaint gradient = new GradientPaint(
                        0, 0, ACCENT_COLOR,
                        getWidth(), getHeight(), new Color(255, 132, 41));
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                super.paintComponent(g2);
            }
        };
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorderPainted(false);
        saveBtn.setContentAreaFilled(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> {
            String currentPassword = new String(currentPasswordField.getPassword()).trim();
            String newPassword = new String(newPasswordField.getPassword()).trim();
            String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(passwordDialog,
                        "Vui lòng điền đầy đủ thông tin!",
                        "Thông tin không hợp lệ",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(passwordDialog,
                        "Mật khẩu mới và xác nhận mật khẩu không khớp!",
                        "Không khớp",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (newPassword.length() < 8) {
                JOptionPane.showMessageDialog(passwordDialog,
                        "Mật khẩu mới phải có ít nhất 8 ký tự!",
                        "Mật khẩu quá ngắn",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Call UserService to change password
            UserService userService = new UserService();
            boolean success = userService.changePassword(loggedInUser.getId(), currentPassword, newPassword);

            if (success) {
                // Show success message
                JOptionPane.showMessageDialog(passwordDialog,
                        "Mật khẩu đã được thay đổi thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);

                passwordDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(passwordDialog,
                        "Mật khẩu hiện tại không chính xác!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        // Add all components to main panel
        mainPanel.add(titleLabel);
        mainPanel.add(formPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(requirementsLabel);
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(buttonPanel);

        passwordDialog.add(mainPanel);
        passwordDialog.setVisible(true);
    }
}