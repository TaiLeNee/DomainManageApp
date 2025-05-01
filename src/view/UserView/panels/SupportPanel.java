package view.UserView.panels;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import utils.ValidationUtils;

public class SupportPanel extends JPanel {
    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField titleField;
    private JTextArea contentArea;

    private static final Color LINE_COLOR = new Color(0, 0, 0, 80);

    @SuppressWarnings("static-access")
    public SupportPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 238, 210)); // màu vàng nhạt

        // Tiêu đề
        JLabel label = new JLabel("Hỗ trợ trực tuyến", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setBorder(new EmptyBorder(20, 0, 50, 0));
        add(label, BorderLayout.NORTH);

        // Panel chứa form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Họ tên
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(createLabel("Họ tên"), gbc);

        gbc.gridx = 1;
        fullNameField = createTextField();
        gbc.gridwidth = 3; // Gộp 3 cột để căn chỉnh đẹp hơn
        formPanel.add(fullNameField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1; // Reset lại gridwidth
        formPanel.add(createLabel("Email"), gbc);

        gbc.gridx = 1;
        emailField = createTextField();
        gbc.gridwidth = 3; // Gộp 3 cột để căn chỉnh đẹp hơn
        formPanel.add(emailField, gbc);

        // Tiêu đề
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        formPanel.add(createLabel("Tiêu đề"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        titleField = createTextField();
        formPanel.add(titleField, gbc);

        // Nội dung
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        formPanel.add(createLabel("Nội dung"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        contentArea = new JTextArea(1, 20);
        contentArea.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, LINE_COLOR));
        contentArea.setFont(new Font("Arial", Font.PLAIN, 14));
        contentArea.setOpaque(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        formPanel.add(contentArea, gbc);

        // Nút GỬI
        JButton sendButton = new JButton("Gửi");
        sendButton.setFocusPainted(false);
        sendButton.setBackground(new Color(220, 53, 69)); // đỏ kiểu "danger" (#dc3545)
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setContentAreaFilled(true);
        sendButton.setOpaque(true);
        sendButton.setForeground(Color.WHITE); // chữ trắng
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // hiệu ứng chuột
        sendButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25)); // padding nút

        // Tạo panel chứa nút và căn giữa
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 20, 0));
        buttonPanel.add(sendButton);

        add(buttonPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> {
            String name = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();

            if (name.isEmpty() || email.isEmpty() || content.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Vui lòng nhập đầy đủ thông tin!",
                        "Thiếu thông tin",
                        JOptionPane.WARNING_MESSAGE
                );
            } else if (new ValidationUtils().isValidEmail(email)) {
                // Xử lý gửi dữ liệu ở đây (nếu có)

                // Thông báo thành công
                JOptionPane.showMessageDialog(
                        this,
                        "Gửi yêu cầu hỗ trợ thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );

                // (Tuỳ chọn) Xoá nội dung sau khi gửi
                fullNameField.setText("");
                emailField.setText("");
                titleField.setText("");
                contentArea.setText("");
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Email sai định dạng!",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        add(formPanel, BorderLayout.CENTER);
    }

    // Tạo JLabel có kiểu dáng thống nhất
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        return label;
    }

    // Tạo JTextField có kiểu dáng thống nhất
    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Arial", Font.PLAIN, 14));
        tf.setOpaque(false);
        tf.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, LINE_COLOR));
        return tf;
    }
}