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
    // Enhanced modern color palette
    private static final Color BG_COLOR = new Color(243, 246, 249);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(79, 70, 229); // Indigo
    private static final Color SECONDARY_COLOR = new Color(99, 102, 241); // Light indigo
    private static final Color ACCENT_COLOR = new Color(16, 185, 129); // Emerald
    private static final Color WARNING_COLOR = new Color(245, 158, 11); // Amber
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color TEXT_MUTED = new Color(156, 163, 175);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 8);

    // Enhanced typography system
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 13);

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
        setBorder(new EmptyBorder(30, 30, 30, 30));

        initComponents();
    }

    private void initComponents() {
        // Create page header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 30, 0));

        JLabel titleLabel = new JLabel("Hồ sơ cá nhân");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Quản lý thông tin tài khoản và cài đặt bảo mật");
        subtitleLabel.setFont(FONT_REGULAR);
        subtitleLabel.setForeground(TEXT_SECONDARY);

        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setOpaque(false);
        titleContainer.add(titleLabel);
        titleContainer.add(Box.createRigidArea(new Dimension(0, 5)));
        titleContainer.add(subtitleLabel);

        headerPanel.add(titleContainer, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Create main content with cards
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        // Profile overview card
        JPanel profileCard = createProfileOverviewCard();
        
        // Personal information card  
        JPanel infoCard = createPersonalInfoCard();

        // Security card
        JPanel securityCard = createSecurityCard();

        mainContent.add(profileCard);
        mainContent.add(Box.createRigidArea(new Dimension(0, 20)));
        mainContent.add(infoCard);
        mainContent.add(Box.createRigidArea(new Dimension(0, 20)));
        mainContent.add(securityCard);

        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createCard(String title, String subtitle) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw shadow
                g2.setColor(SHADOW_COLOR);
                g2.fill(new RoundRectangle2D.Float(2, 2, getWidth() - 2, getHeight() - 2, 16, 16));

                // Draw card background
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 2, getHeight() - 2, 16, 16));

                // Draw subtle border
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 3, getHeight() - 3, 16, 16));

                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        if (title != null) {
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setOpaque(false);
            headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(FONT_SUBTITLE);
            titleLabel.setForeground(TEXT_PRIMARY);

            headerPanel.add(titleLabel, BorderLayout.WEST);

            if (subtitle != null) {
                JLabel subtitleLabel = new JLabel(subtitle);
                subtitleLabel.setFont(FONT_SMALL);
                subtitleLabel.setForeground(TEXT_MUTED);
                
                JPanel titleContainer = new JPanel();
                titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
                titleContainer.setOpaque(false);
                titleContainer.add(titleLabel);
                titleContainer.add(Box.createRigidArea(new Dimension(0, 3)));
                titleContainer.add(subtitleLabel);

                headerPanel.removeAll();
                headerPanel.add(titleContainer, BorderLayout.WEST);
            }

            card.add(headerPanel, BorderLayout.NORTH);
        }

        return card;
    }

    private JPanel createProfileOverviewCard() {
        JPanel card = createCard(null, null);
        
        // Custom layout for profile overview
        JPanel content = new JPanel(new BorderLayout(30, 0));
        content.setOpaque(false);

        // Left side - Avatar section
        JPanel avatarSection = createEnhancedAvatarSection();
        
        // Right side - Quick info
        JPanel quickInfo = createQuickInfoSection();

        content.add(avatarSection, BorderLayout.WEST);
        content.add(quickInfo, BorderLayout.CENTER);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createEnhancedAvatarSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setPreferredSize(new Dimension(200, 250));

        // Avatar container with enhanced styling
        JPanel avatarContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Outer shadow
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillOval(5, 5, getWidth() - 10, getHeight() - 10);

                // Avatar border
                g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, getWidth() - 5, getHeight() - 5);

                g2.dispose();
            }
        };
        avatarContainer.setLayout(new GridBagLayout());
        avatarContainer.setPreferredSize(new Dimension(160, 160));
        avatarContainer.setMaximumSize(new Dimension(160, 160));
        avatarContainer.setOpaque(false);

        // Avatar image
        avatarLabel = new JLabel();
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setIcon(loadEnhancedAvatarImage("src\\img\\user-icon.png"));
        avatarContainer.add(avatarLabel);

        // User name and username
        JLabel displayName = new JLabel(loggedInUser.getFullName());
        displayName.setFont(FONT_HEADING);
        displayName.setForeground(TEXT_PRIMARY);
        displayName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel usernameDisplay = new JLabel("@" + loggedInUser.getUsername());
        usernameDisplay.setFont(FONT_REGULAR);
        usernameDisplay.setForeground(TEXT_SECONDARY);
        usernameDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Change avatar button with modern styling
        JButton changeAvatarBtn = createModernButton("Thay đổi ảnh", SECONDARY_COLOR, false);
        changeAvatarBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        changeAvatarBtn.addActionListener(e -> changeAvatar());

        // Assembly
        section.add(avatarContainer);
        section.add(Box.createRigidArea(new Dimension(0, 15)));
        section.add(displayName);
        section.add(Box.createRigidArea(new Dimension(0, 5)));
        section.add(usernameDisplay);
        section.add(Box.createRigidArea(new Dimension(0, 20)));
        section.add(changeAvatarBtn);

        return section;
    }

    private JPanel createQuickInfoSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);

        // Status badge
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusPanel.setOpaque(false);
        
        JLabel statusBadge = new JLabel("Đang hoạt động") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(34, 197, 94, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                g2.setColor(new Color(34, 197, 94));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                
                super.paintComponent(g2);
            }
        };
        statusBadge.setFont(FONT_SMALL);
        statusBadge.setForeground(new Color(34, 197, 94));
        statusBadge.setBorder(new EmptyBorder(6, 12, 6, 12));
        statusBadge.setOpaque(false);
        
        statusPanel.add(statusBadge);

        // Quick stats
        JPanel statsGrid = new JPanel(new GridLayout(2, 2, 20, 15));
        statsGrid.setOpaque(false);
        statsGrid.setBorder(new EmptyBorder(20, 0, 0, 0));

        statsGrid.add(createStatItem("Vai trò", loggedInUser.getRole()));
        statsGrid.add(createStatItem("Tham gia", "Dec 2023"));
        statsGrid.add(createStatItem("Domain", "5 active"));
        statsGrid.add(createStatItem("Đăng nhập", "Hôm nay"));

        section.add(statusPanel);
        section.add(statsGrid);
        
        return section;
    }

    private JPanel createStatItem(String label, String value) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setOpaque(false);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FONT_HEADING);
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelText = new JLabel(label);
        labelText.setFont(FONT_SMALL);
        labelText.setForeground(TEXT_MUTED);
        labelText.setAlignmentX(Component.LEFT_ALIGNMENT);

        item.add(valueLabel);
        item.add(Box.createRigidArea(new Dimension(0, 2)));
        item.add(labelText);

        return item;
    }

    private JPanel createPersonalInfoCard() {
        JPanel card = createCard("Thông tin cá nhân", "Quản lý thông tin hiển thị của bạn");
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // Info fields with enhanced styling
        content.add(createEnhancedInfoField("Họ và tên", loggedInUser.getFullName()));
        content.add(Box.createRigidArea(new Dimension(0, 15)));
        content.add(createEnhancedInfoField("Email", loggedInUser.getEmail()));
        content.add(Box.createRigidArea(new Dimension(0, 15)));
        content.add(createEnhancedInfoField("Tên đăng nhập", loggedInUser.getUsername()));

        // Action button
        content.add(Box.createRigidArea(new Dimension(0, 25)));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setOpaque(false);
        
        JButton editBtn = createModernButton("Chỉnh sửa thông tin", PRIMARY_COLOR, true);
        editBtn.addActionListener(e -> openEditDialog());
        buttonPanel.add(editBtn);
        
        content.add(buttonPanel);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createSecurityCard() {
        JPanel card = createCard("Bảo mật tài khoản", "Quản lý mật khẩu và cài đặt bảo mật");
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // Security items
        content.add(createSecurityItem("Mật khẩu", "Được cập nhật 30 ngày trước", "Đổi mật khẩu"));
        content.add(Box.createRigidArea(new Dimension(0, 15)));
        content.add(createSecurityItem("Đăng nhập gần đây", "Hôm nay, 10:15 AM", "Xem lịch sử"));

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createSecurityItem(String title, String description, String actionText) {
        JPanel item = new JPanel(new BorderLayout());
        item.setOpaque(false);
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 0, 0, 5)),
            new EmptyBorder(15, 0, 15, 0)
        ));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_REGULAR);
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(FONT_SMALL);
        descLabel.setForeground(TEXT_MUTED);

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        infoPanel.add(descLabel);

        JButton actionBtn = createModernButton(actionText, 
            actionText.equals("Đổi mật khẩu") ? WARNING_COLOR : SECONDARY_COLOR, false);
        if (actionText.equals("Đổi mật khẩu")) {
            actionBtn.addActionListener(e -> openChangePasswordDialog());
        }

        item.add(infoPanel, BorderLayout.CENTER);
        item.add(actionBtn, BorderLayout.EAST);

        return item;
    }

    private JPanel createEnhancedInfoField(String label, String value) {
        JPanel field = new JPanel(new BorderLayout());
        field.setOpaque(false);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 0, 0, 5)),
            new EmptyBorder(12, 0, 12, 0)
        ));

        JLabel labelText = new JLabel(label);
        labelText.setFont(FONT_LABEL);
        labelText.setForeground(TEXT_SECONDARY);

        JLabel valueText = new JLabel(value);
        valueText.setFont(FONT_REGULAR);
        valueText.setForeground(TEXT_PRIMARY);

        field.add(labelText, BorderLayout.WEST);
        field.add(valueText, BorderLayout.EAST);

        return field;
    }

    private JButton createModernButton(String text, Color color, boolean isPrimary) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isPrimary) {
                    // Primary button with gradient
                    GradientPaint gradient = new GradientPaint(
                        0, 0, color,
                        getWidth(), getHeight(), color.darker());
                    g2.setPaint(gradient);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                } else {
                    // Secondary button with border
                    g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 10));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    
                    g2.setColor(color);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                }

                super.paintComponent(g2);
            }
        };

        button.setFont(FONT_REGULAR);
        button.setForeground(isPrimary ? Color.WHITE : color);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setOpaque(false);
                button.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setOpaque(false);
                button.repaint();
            }
        });

        return button;
    }

    private ImageIcon loadEnhancedAvatarImage(String path) {
        File file = new File(path);
        if (file.exists()) {
            Image image = new ImageIcon(path).getImage();
            // Create circular crop with enhanced styling
            BufferedImage circleBuffer = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = circleBuffer.createGraphics();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, 150, 150));
            g2.drawImage(image, 0, 0, 150, 150, null);

            g2.dispose();
            return new ImageIcon(circleBuffer);
        } else {
            // Create enhanced default avatar with initials
            BufferedImage avatar = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = avatar.createGraphics();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Create modern gradient background
            GradientPaint gradient = new GradientPaint(
                0, 0, PRIMARY_COLOR,
                150, 150, SECONDARY_COLOR
            );
            g2.setPaint(gradient);
            g2.fillOval(0, 0, 150, 150);

            // Add initials with better typography
            String initials = getInitials(loggedInUser.getFullName());
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 48));
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
            avatarLabel.setIcon(loadEnhancedAvatarImage(selectedFile.getAbsolutePath()));
            JOptionPane.showMessageDialog(this, "Đổi ảnh đại diện thành công!");
        }
    }

    private void openEditDialog() {
        // Create enhanced modern dialog
        JDialog editDialog = new JDialog(parentFrame, "Chỉnh sửa thông tin cá nhân", true);
        editDialog.setSize(500, 320);
        editDialog.setLocationRelativeTo(parentFrame);
        editDialog.setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(CARD_BG);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Dialog title with icon
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(0, 0, 25, 0));

        JLabel titleLabel = new JLabel("Chỉnh sửa thông tin");
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Cập nhật thông tin hiển thị của bạn");
        subtitleLabel.setFont(FONT_SMALL);
        subtitleLabel.setForeground(TEXT_MUTED);

        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setOpaque(false);
        titleContainer.add(titleLabel);
        titleContainer.add(Box.createRigidArea(new Dimension(0, 5)));
        titleContainer.add(subtitleLabel);

        titlePanel.add(titleContainer, BorderLayout.WEST);

        // Enhanced form fields
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(0, 0, 25, 0));

        // Name field with modern styling
        JPanel nameFieldPanel = createEnhancedFormField("Họ và tên", loggedInUser.getFullName());
        JTextField nameField = findTextField(nameFieldPanel);

        formPanel.add(nameFieldPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Email field with modern styling
        JPanel emailFieldPanel = createEnhancedFormField("Email", loggedInUser.getEmail());
        JTextField emailField = findTextField(emailFieldPanel);

        formPanel.add(emailFieldPanel);

        // Enhanced button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);

        JButton cancelBtn = createModernButton("Hủy", TEXT_SECONDARY, false);
        cancelBtn.addActionListener(e -> editDialog.dispose());

        JButton saveBtn = createModernButton("Lưu thay đổi", PRIMARY_COLOR, true);
        saveBtn.addActionListener(e -> {
            String newName = nameField.getText().trim();
            String newEmail = emailField.getText().trim();

            if (newName.isEmpty() || newEmail.isEmpty()) {
                showStyledMessage(editDialog, "Vui lòng điền đầy đủ thông tin!", "Thông tin không hợp lệ", false);
                return;
            }

            // Call UserService to update information
            UserService userService = new UserService();
            boolean success = userService.updateUserInfo(loggedInUser.getId(), newName, newEmail);

            if (success) {
                // Update displayed information in UI
                loggedInUser.setFullName(newName);
                loggedInUser.setEmail(newEmail);
                
                showStyledMessage(editDialog, "Thông tin đã được cập nhật thành công!", "Thành công", true);
                editDialog.dispose();
                
                // Refresh the panel
                revalidate();
                repaint();
            } else {
                showStyledMessage(editDialog, "Không thể cập nhật thông tin. Vui lòng thử lại sau.", "Lỗi", false);
            }
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        // Assembly
        mainPanel.add(titlePanel);
        mainPanel.add(formPanel);
        mainPanel.add(buttonPanel);

        editDialog.add(mainPanel);
        editDialog.setVisible(true);
    }

    private JPanel createEnhancedFormField(String labelText, String value) {
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(FONT_LABEL);
        label.setForeground(TEXT_SECONDARY);
        label.setBorder(new EmptyBorder(0, 0, 8, 0));

        JTextField textField = new JTextField(value) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                
                super.paintComponent(g2);
            }
        };
        textField.setFont(FONT_REGULAR);
        textField.setForeground(TEXT_PRIMARY);
        textField.setBackground(new Color(249, 250, 251));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

        // Focus styling
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                    BorderFactory.createEmptyBorder(11, 14, 11, 14)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(12, 15, 12, 15)
                ));
            }
        });

        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(textField, BorderLayout.CENTER);

        return fieldPanel;
    }

    private JTextField findTextField(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JTextField) {
                return (JTextField) comp;
            }
        }
        return null;
    }

    private void showStyledMessage(Component parent, String message, String title, boolean isSuccess) {
        JOptionPane.showMessageDialog(parent, message, title, 
            isSuccess ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
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