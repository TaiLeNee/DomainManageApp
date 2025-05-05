package view.UserView.panels;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import utils.ValidationUtils;

public class SupportPanel extends JPanel {
    // Modern color palette - matching other panels
    private static final Color BG_COLOR = new Color(248, 250, 252);
    private static final Color PRIMARY_COLOR = new Color(41, 59, 95);
    private static final Color SECONDARY_COLOR = new Color(66, 91, 138);
    private static final Color ACCENT_COLOR = new Color(255, 111, 0);
    private static final Color TEXT_PRIMARY = new Color(34, 40, 49);
    private static final Color TEXT_SECONDARY = new Color(130, 139, 162);
    private static final Color BORDER_COLOR = new Color(230, 235, 241);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);

    // Modern fonts
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);

    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField titleField;
    private JTextArea contentArea;
    private JButton sendButton;
    private JPanel formCard;
    private JPanel successCard;
    private CardLayout cardLayout;

    public SupportPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents();
    }

    private void initComponents() {
        // Create page title
        JLabel titleLabel = new JLabel("Hỗ trợ trực tuyến");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        add(titleLabel, BorderLayout.NORTH);

        // Create main content panel with CardLayout to switch between form and success
        // message
        JPanel contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setOpaque(false);

        // Create form card
        formCard = createFormPanel();

        // Create success card
        successCard = createSuccessPanel();

        // Add cards to content panel
        contentPanel.add(formCard, "FORM");
        contentPanel.add(successCard, "SUCCESS");

        // Show form card by default
        cardLayout.show(contentPanel, "FORM");

        // Add content panel to main panel
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        // Help text at top
        JPanel helpPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fill background with light color
                g2.setColor(new Color(237, 242, 247));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));

                // Add accent border on left
                g2.setColor(ACCENT_COLOR);
                g2.fillRect(0, 0, 5, getHeight());

                g2.dispose();
            }
        };
        helpPanel.setLayout(new BorderLayout());
        helpPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        helpPanel.setOpaque(false);

        JLabel helpIcon = new JLabel("ℹ️");
        helpIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24));

        JPanel helpTextPanel = new JPanel();
        helpTextPanel.setLayout(new BoxLayout(helpTextPanel, BoxLayout.Y_AXIS));
        helpTextPanel.setOpaque(false);
        helpTextPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        JLabel helpTitle = new JLabel("Cần hỗ trợ?");
        helpTitle.setFont(FONT_SUBTITLE);
        helpTitle.setForeground(TEXT_PRIMARY);

        JLabel helpDesc = new JLabel(
                "<html>Vui lòng điền đầy đủ thông tin bên dưới, chúng tôi sẽ liên hệ lại với bạn trong vòng 24 giờ.</html>");
        helpDesc.setFont(FONT_REGULAR);
        helpDesc.setForeground(TEXT_SECONDARY);

        helpTextPanel.add(helpTitle);
        helpTextPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        helpTextPanel.add(helpDesc);

        helpPanel.add(helpIcon, BorderLayout.WEST);
        helpPanel.add(helpTextPanel, BorderLayout.CENTER);

        // Create form container
        JPanel formContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));

                g2.dispose();
            }
        };
        formContainer.setLayout(new BorderLayout());
        formContainer.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        formContainer.setOpaque(false);

        // Create form fields
        JPanel formFields = new JPanel();
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));
        formFields.setOpaque(false);

        // Full name field
        JPanel nameFieldPanel = createFieldPanel("Họ tên*", "Nhập họ tên của bạn");
        fullNameField = (JTextField) nameFieldPanel.getClientProperty("field");

        // Email field
        JPanel emailFieldPanel = createFieldPanel("Email*", "Nhập địa chỉ email của bạn");
        emailField = (JTextField) emailFieldPanel.getClientProperty("field");

        // Subject field
        JPanel subjectFieldPanel = createFieldPanel("Tiêu đề", "Nhập tiêu đề yêu cầu hỗ trợ");
        titleField = (JTextField) subjectFieldPanel.getClientProperty("field");

        // Message area
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout(0, 5));
        messagePanel.setOpaque(false);
        messagePanel.setMaximumSize(new Dimension(2000, 150));

        JLabel messageLabel = new JLabel("Nội dung*");
        messageLabel.setFont(FONT_REGULAR);
        messageLabel.setForeground(TEXT_SECONDARY);

        contentArea = new JTextArea();
        contentArea.setFont(FONT_REGULAR);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        contentArea.setRows(5);

        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        messagePanel.add(messageLabel, BorderLayout.NORTH);
        messagePanel.add(scrollPane, BorderLayout.CENTER);

        // Add fields to form
        formFields.add(nameFieldPanel);
        formFields.add(Box.createRigidArea(new Dimension(0, 15)));
        formFields.add(emailFieldPanel);
        formFields.add(Box.createRigidArea(new Dimension(0, 15)));
        formFields.add(subjectFieldPanel);
        formFields.add(Box.createRigidArea(new Dimension(0, 15)));
        formFields.add(messagePanel);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        sendButton = new JButton("Gửi yêu cầu") {
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
        sendButton.setFont(FONT_BUTTON);
        sendButton.setForeground(Color.WHITE);
        sendButton.setBorderPainted(false);
        sendButton.setContentAreaFilled(false);
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.addActionListener(e -> handleSendRequest());

        buttonPanel.add(sendButton);

        // Add components to form container
        formContainer.add(formFields, BorderLayout.CENTER);
        formContainer.add(buttonPanel, BorderLayout.SOUTH);

        // Add components to panel
        panel.add(helpPanel, BorderLayout.NORTH);
        panel.add(formContainer, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSuccessPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));

                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        panel.setOpaque(false);

        // Success icon
        JLabel successIcon = new JLabel("✓");
        successIcon.setFont(new Font("Arial", Font.BOLD, 48));
        successIcon.setForeground(SUCCESS_COLOR);
        successIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create a circular background for the success icon
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Light green background
                g2.setColor(new Color(236, 253, 245));
                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setLayout(new GridBagLayout());
        iconPanel.setPreferredSize(new Dimension(100, 100));
        iconPanel.setMaximumSize(new Dimension(100, 100));
        iconPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconPanel.add(successIcon);

        // Success message
        JLabel titleLabel = new JLabel("Yêu cầu đã được gửi!");
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = new JLabel(
                "<html><div style='text-align: center;'>Cảm ơn bạn đã gửi yêu cầu hỗ trợ. Chúng tôi sẽ liên hệ lại với bạn sớm nhất có thể.</div></html>");
        messageLabel.setFont(FONT_REGULAR);
        messageLabel.setForeground(TEXT_SECONDARY);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Button to create new request
        JButton newRequestBtn = new JButton("Gửi yêu cầu mới") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient
                GradientPaint gradient = new GradientPaint(
                        0, 0, SECONDARY_COLOR,
                        getWidth(), getHeight(), PRIMARY_COLOR);
                g2.setPaint(gradient);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

                super.paintComponent(g2);
            }
        };
        newRequestBtn.setFont(FONT_BUTTON);
        newRequestBtn.setForeground(Color.WHITE);
        newRequestBtn.setBorderPainted(false);
        newRequestBtn.setContentAreaFilled(false);
        newRequestBtn.setFocusPainted(false);
        newRequestBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        newRequestBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        newRequestBtn.setMaximumSize(new Dimension(200, 40));
        newRequestBtn.addActionListener(e -> {
            resetForm();
            cardLayout.show(newRequestBtn.getParent().getParent().getParent(), "FORM");
        });

        // Add components with spacing
        panel.add(Box.createVerticalGlue());
        panel.add(iconPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(messageLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(newRequestBtn);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createFieldPanel(String labelText, String placeholder) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(2000, 55));

        JLabel label = new JLabel(labelText);
        label.setFont(FONT_REGULAR);
        label.setForeground(TEXT_SECONDARY);

        JTextField textField = new JTextField();
        textField.setFont(FONT_REGULAR);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 0, 5, 0)));

        // Add placeholder text
        textField.putClientProperty("JTextField.placeholderText", placeholder);

        panel.add(label, BorderLayout.NORTH);
        panel.add(textField, BorderLayout.CENTER);

        // Store the field as a client property
        panel.putClientProperty("field", textField);

        return panel;
    }

    private void handleSendRequest() {
        String name = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        ValidationUtils validationUtils = new ValidationUtils();

        if (name.isEmpty() || email.isEmpty() || content.isEmpty()) {
            showError("Vui lòng điền đầy đủ các trường bắt buộc (*)");
        } else if (!validationUtils.isValidEmail(email)) {
            showError("Email không đúng định dạng");
        } else {
            // Here you would typically send the data to your backend
            // For now, just show the success screen
            cardLayout.show(sendButton.getParent().getParent().getParent().getParent(), "SUCCESS");
        }
    }

    private void showError(String message) {
        JOptionPane optionPane = new JOptionPane(
                message,
                JOptionPane.ERROR_MESSAGE);
        JDialog dialog = optionPane.createDialog(this, "Lỗi");
        dialog.setVisible(true);
    }

    private void resetForm() {
        fullNameField.setText("");
        emailField.setText("");
        titleField.setText("");
        contentArea.setText("");
    }
}