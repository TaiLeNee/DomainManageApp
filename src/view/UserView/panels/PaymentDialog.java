package view.UserView.panels;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import model.*;
import repository.*;
import utils.UserSession;

public class PaymentDialog extends JDialog {
    private User loggedInUser;
    private String[] selectedDomains;
    private HashMap<String, Double> domainPrices;
    private HashMap<String, Integer> domainRentalPeriods;
    private double totalPrice;
    private MyDomainsPanel myDomainsPanel;
    private RentalPeriodRepository rentalPeriodRepository;
    
    // UI Components
    private JPanel mainPanel;
    private JPanel qrPanel;
    private JButton confirmButton;
    private boolean paymentConfirmed = false;

    // UI Constants
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(222, 226, 230);

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font HEADING_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font VALUE_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font BTN_FONT = new Font("Segoe UI", Font.BOLD, 14);

    private DecimalFormat currencyFormat = new DecimalFormat("#,### VND");

    public PaymentDialog(Frame owner, String[] selectedDomains,
            HashMap<String, Double> domainPrices,
            HashMap<String, Integer> domainRentalPeriods,
            MyDomainsPanel myDomainsPanel) {
        super(owner, "Thanh toán tên miền", true);
        this.selectedDomains = selectedDomains;
        this.domainPrices = domainPrices;
        this.domainRentalPeriods = domainRentalPeriods;
        this.myDomainsPanel = myDomainsPanel;
        this.loggedInUser = UserSession.getInstance().getCurrentUser();
        this.rentalPeriodRepository = new RentalPeriodRepository();

        // Tính tổng tiền
        this.totalPrice = 0;
        for (String domain : selectedDomains) {
            if (domainPrices.containsKey(domain)) {
                this.totalPrice += domainPrices.get(domain);
            }
        }

        initComponents();
        setupDialog();
    }

    private void setupDialog() {
        setSize(600, 700);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Main content panel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add components to main panel
        mainPanel.add(createHeaderPanel());
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(createDomainListPanel());
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(createTotalPanel());
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(createPaymentMethodPanel());

        // QR Panel (initially hidden)
        qrPanel = createQRPanel();
        qrPanel.setVisible(false);

        // Add panels to dialog
        add(mainPanel, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("Xác nhận thanh toán");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);

        JLabel subtitleLabel = new JLabel("Vui lòng kiểm tra thông tin trước khi thanh toán");
        subtitleLabel.setFont(LABEL_FONT);
        subtitleLabel.setForeground(Color.GRAY);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(subtitleLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDomainListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        // Title
        JLabel titleLabel = new JLabel("Danh sách tên miền");
        titleLabel.setFont(HEADING_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);

        // Domain list in scroll pane
        JPanel domainListContainer = new JPanel();
        domainListContainer.setLayout(new BoxLayout(domainListContainer, BoxLayout.Y_AXIS));
        domainListContainer.setBackground(CARD_COLOR);

        for (String domain : selectedDomains) {
            double price = domainPrices.getOrDefault(domain, 0.0);
            int rentalPeriodId = domainRentalPeriods.getOrDefault(domain, 1);
            String rentalInfo = getRentalPeriodInfo(rentalPeriodId);

            JPanel domainItem = createDomainItem(domain, rentalInfo, price);
            domainListContainer.add(domainItem);
            domainListContainer.add(Box.createVerticalStrut(5));
        }

        JScrollPane scrollPane = new JScrollPane(domainListContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(0, 200)); // Fixed height for scroll
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDomainItem(String domain, String rentalInfo, double price) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(new Color(245, 247, 250));
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        item.setPreferredSize(new Dimension(0, 45));

        // Create single line label with domain and rental info
        String domainText = domain + " - " + rentalInfo;
        JLabel domainInfoLabel = new JLabel(domainText);
        domainInfoLabel.setFont(LABEL_FONT);

        JLabel priceLabel = new JLabel(currencyFormat.format(price));
        priceLabel.setFont(VALUE_FONT);
        priceLabel.setForeground(PRIMARY_COLOR);

        item.add(domainInfoLabel, BorderLayout.WEST);
        item.add(priceLabel, BorderLayout.EAST);

        return item;
    }

    private JPanel createTotalPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel("Tổng thanh toán");
        titleLabel.setFont(HEADING_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);

        JLabel totalLabel = new JLabel(currencyFormat.format(totalPrice));
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalLabel.setForeground(PRIMARY_COLOR);

        JLabel countLabel = new JLabel("(" + selectedDomains.length + " tên miền)");
        countLabel.setFont(LABEL_FONT);
        countLabel.setForeground(Color.GRAY);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.add(totalLabel);
        rightPanel.add(countLabel);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createPaymentMethodPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel("Phương thức thanh toán");
        titleLabel.setFont(HEADING_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);

        JPanel methodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        methodPanel.setOpaque(false);

        JLabel methodIcon = new JLabel("💳");
        methodIcon.setFont(new Font("Segoe UI", Font.PLAIN, 20));

        JLabel methodLabel = new JLabel("Chuyển khoản ngân hàng");
        methodLabel.setFont(LABEL_FONT);

        methodPanel.add(methodIcon);
        methodPanel.add(Box.createHorizontalStrut(10));
        methodPanel.add(methodLabel);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(methodPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createQRPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SUCCESS_COLOR, 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel titleLabel = new JLabel("Quét mã QR để thanh toán");
        titleLabel.setFont(HEADING_FONT);
        titleLabel.setForeground(SUCCESS_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tạo QR code động với thông tin thanh toán
        JLabel qrLabel = createDynamicQRCode();
        qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrLabel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        // Thông tin thanh toán
        JLabel bankInfoLabel = new JLabel("Ngân hàng: MBBank");
        bankInfoLabel.setFont(VALUE_FONT);
        bankInfoLabel.setForeground(SUCCESS_COLOR);
        bankInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel accountLabel = new JLabel("STK: 0868807137");
        accountLabel.setFont(VALUE_FONT);
        accountLabel.setForeground(SUCCESS_COLOR);
        accountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel amountLabel = new JLabel("Số tiền: " + currencyFormat.format(totalPrice));
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        amountLabel.setForeground(SUCCESS_COLOR);
        amountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nội dung chuyển khoản
        String transferContent = generateTransferContent();
        JLabel contentLabel = new JLabel("Nội dung: " + transferContent);
        contentLabel.setFont(LABEL_FONT);
        contentLabel.setForeground(PRIMARY_COLOR);
        contentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel noteLabel = new JLabel("Vui lòng chuyển khoản đúng số tiền và nội dung");
        noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noteLabel.setForeground(Color.GRAY);
        noteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(qrLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(bankInfoLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(accountLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(amountLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(contentLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(noteLabel);

        return panel;
    }

    private JLabel createDynamicQRCode() {
        try {
            // Tạo nội dung QR code với thông tin thanh toán thực tế
            String qrContent = generateQRContent();
            
            // Thử load hình ảnh QR code từ file qrcode.jpg
            ImageIcon qrIcon = null;
            String[] possiblePaths = {
                "qrcode.jpg",
                "src/qrcode.jpg", 
                "./qrcode.jpg",
                "src/img/qrcode.jpg",
                "img/qrcode.jpg"
            };
            
            for (String path : possiblePaths) {
                try {
                    qrIcon = new ImageIcon(path);
                    if (qrIcon.getIconWidth() > 0) {
                        System.out.println("Đã load QR code từ: " + path);
                        break; // Nếu load thành công thì dừng
                    }
                } catch (Exception e) {
                    // Tiếp tục thử đường dẫn khác
                }
            }
            
            JLabel qrLabel;
            if (qrIcon != null && qrIcon.getIconWidth() > 0) {
                // Scale hình ảnh về kích thước phù hợp
                Image qrImage = qrIcon.getImage();
                Image scaledQR = qrImage.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                qrLabel = new JLabel(new ImageIcon(scaledQR));
                qrLabel.setPreferredSize(new Dimension(200, 200));
                qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
                qrLabel.setVerticalAlignment(SwingConstants.CENTER);
                qrLabel.setBackground(Color.WHITE);
                qrLabel.setOpaque(true);
            } else {
                // Fallback sử dụng ASCII art nếu không load được hình
                System.out.println("Không thể load hình ảnh QR code, sử dụng ASCII fallback");
                qrLabel = new JLabel("<html><div style='text-align: center;'>" +
                        "<div style='font-family: monospace; font-size: 8px; line-height: 8px;'>" +
                        generateAsciiQRCode() +
                        "</div></html>");
                qrLabel.setPreferredSize(new Dimension(200, 200));
                qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
                qrLabel.setVerticalAlignment(SwingConstants.CENTER);
                qrLabel.setBackground(Color.WHITE);
                qrLabel.setOpaque(true);
            }
            
            // Tooltip với thông tin đầy đủ
            qrLabel.setToolTipText("<html>" + qrContent.replace("\n", "<br>") + "</html>");
            
            return qrLabel;
            
        } catch (Exception e) {
            // Fallback QR placeholder
            System.err.println("Lỗi khi tạo QR code: " + e.getMessage());
            JLabel qrLabel = new JLabel("QR CODE");
            qrLabel.setPreferredSize(new Dimension(200, 200));
            qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
            qrLabel.setVerticalAlignment(SwingConstants.CENTER);
            qrLabel.setBackground(new Color(240, 240, 240));
            qrLabel.setOpaque(true);
            qrLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            qrLabel.setForeground(Color.GRAY);
            return qrLabel;
        }
    }

    private String generateQRContent() {
        StringBuilder content = new StringBuilder();
        content.append("Bank: MBBank\n");
        content.append("Account: 0868807137\n");
        content.append("Amount: ").append(String.format("%.0f", totalPrice)).append(" VND\n");
        content.append("Content: ").append(generateTransferContent()).append("\n");
        content.append("Domains: ");
        for (int i = 0; i < selectedDomains.length; i++) {
            content.append(selectedDomains[i]);
            if (i < selectedDomains.length - 1) content.append(", ");
        }
        return content.toString();
    }

    private String generateTransferContent() {
        // Tạo mã giao dịch duy nhất
        String transactionId = "DOM" + System.currentTimeMillis() % 100000;
        String username = (loggedInUser != null) ? loggedInUser.getUsername() : "GUEST";
        return transactionId + " " + username;
    }

    private String generateAsciiQRCode() {
        // Tạo QR code ASCII đơn giản
        return "██████████████████████<br>" +
               "██&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;██&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;██<br>" +
               "██&nbsp;&nbsp;████&nbsp;&nbsp;██&nbsp;&nbsp;████&nbsp;&nbsp;██<br>" +
               "██&nbsp;&nbsp;████&nbsp;&nbsp;██&nbsp;&nbsp;████&nbsp;&nbsp;██<br>" +
               "██&nbsp;&nbsp;████&nbsp;&nbsp;██&nbsp;&nbsp;████&nbsp;&nbsp;██<br>" +
               "██&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;██&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;██<br>" +
               "██████████████████████<br>" +
               "████&nbsp;&nbsp;██&nbsp;&nbsp;████&nbsp;&nbsp;██&nbsp;&nbsp;██<br>" +
               "██&nbsp;&nbsp;████&nbsp;&nbsp;████&nbsp;&nbsp;████<br>" +
               "████&nbsp;&nbsp;██████&nbsp;&nbsp;██&nbsp;&nbsp;██<br>" +
               "██&nbsp;&nbsp;&nbsp;&nbsp;██&nbsp;&nbsp;██&nbsp;&nbsp;████&nbsp;&nbsp;██<br>" +
               "████&nbsp;&nbsp;████&nbsp;&nbsp;██&nbsp;&nbsp;████<br>" +
               "██████████████████████<br>" +
               "██&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;██&nbsp;&nbsp;██&nbsp;&nbsp;████<br>" +
               "██&nbsp;&nbsp;████&nbsp;&nbsp;██&nbsp;&nbsp;██&nbsp;&nbsp;██<br>" +
               "██&nbsp;&nbsp;████&nbsp;&nbsp;██&nbsp;&nbsp;████&nbsp;&nbsp;██<br>" +
               "██&nbsp;&nbsp;████&nbsp;&nbsp;██&nbsp;&nbsp;██&nbsp;&nbsp;██<br>" +
               "██&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;██&nbsp;&nbsp;████&nbsp;&nbsp;██<br>" +
               "██████████████████████";
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton cancelButton = new JButton("Hủy");
        cancelButton.setFont(BTN_FONT);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBackground(DANGER_COLOR);
        cancelButton.setBorder(BorderFactory.createEmptyBorder());
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.setFocusPainted(false);
        cancelButton.setOpaque(true);
        cancelButton.setContentAreaFilled(true);
        cancelButton.setBorderPainted(false);
        cancelButton.setPreferredSize(new Dimension(100, 35));

        confirmButton = new JButton("Xác nhận");
        confirmButton.setFont(BTN_FONT);
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setBackground(SUCCESS_COLOR);
        confirmButton.setBorder(BorderFactory.createEmptyBorder());
        confirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmButton.setFocusPainted(false);
        confirmButton.setOpaque(true);
        confirmButton.setContentAreaFilled(true);
        confirmButton.setBorderPainted(false);
        confirmButton.setPreferredSize(new Dimension(100, 35));

        // Event listeners
        cancelButton.addActionListener(e -> dispose());
        confirmButton.addActionListener(this::handleConfirmPayment);

        panel.add(cancelButton);
        panel.add(confirmButton);

        return panel;
    }

    private void handleConfirmPayment(ActionEvent e) {
        System.out.println("handleConfirmPayment được gọi, paymentConfirmed = " + paymentConfirmed);
        
        if (!paymentConfirmed) {
            System.out.println("Lần đầu tiên bấm xác nhận - hiển thị QR code");
            // Show QR code
            paymentConfirmed = true;
            showQRCode();
            confirmButton.setText("Hoàn tất");
            confirmButton.setBackground(PRIMARY_COLOR);
        } else {
            System.out.println("Lần thứ hai bấm - xử lý thanh toán");
            // Process payment
            if (processPayment()) {
                JOptionPane.showMessageDialog(this,
                        "Thanh toán thành công!\nTên miền sẽ được kích hoạt trong vài phút.",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
                myDomainsPanel.loadDomainsFromDatabase();
            }
        }
    }

    private void showQRCode() {
        System.out.println("Hiển thị QR code...");
        
        // Remove main panel and add QR panel
        remove(mainPanel);
        add(qrPanel, BorderLayout.CENTER);
        
        // Update dialog size for QR display
        setSize(400, 500);
        setLocationRelativeTo(getOwner());
        
        // Đảm bảo QR panel hiển thị
        qrPanel.setVisible(true);
        
        revalidate();
        repaint();
        
        System.out.println("QR panel đã được thêm và hiển thị");
    }

    private boolean processPayment() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            Map<Integer, Double> domainIdPrices = new HashMap<>();
            Map<Integer, Integer> domainIdRentalPeriods = new HashMap<>();
            Map<Integer, String> domainNames = new HashMap<>();
            Map<Integer, String> domainExtensions = new HashMap<>();

            // Get domain information from database
            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < selectedDomains.length; i++) {
                placeholders.append("?,");
            }
            placeholders.setLength(placeholders.length() - 1);

            String getDomainIdsSQL = "SELECT d.id, d.name, d.extension, d.price, c.rental_period_id " +
                    "FROM domains d " +
                    "JOIN cart c ON d.id = c.domain_id " +
                    "WHERE CONCAT(d.name, d.extension) IN (" + placeholders + ") " +
                    "AND c.user_id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(getDomainIdsSQL)) {
                int paramIndex = 1;
                for (String domain : selectedDomains) {
                    stmt.setString(paramIndex++, domain);
                }
                stmt.setInt(paramIndex, loggedInUser.getId());

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String extension = rs.getString("extension");
                    double price = rs.getDouble("price");
                    int rentalPeriodId = rs.getInt("rental_period_id");

                    domainIdPrices.put(id, price);
                    domainIdRentalPeriods.put(id, rentalPeriodId);
                    domainNames.put(id, name);
                    domainExtensions.put(id, extension);
                }
            }

            if (domainIdPrices.isEmpty()) {
                throw new SQLException("Không tìm thấy tên miền trong giỏ hàng.");
            }

            // Remove from cart
            StringBuilder domainIdsStr = new StringBuilder();
            for (int id : domainIdPrices.keySet()) {
                domainIdsStr.append(id).append(",");
            }
            domainIdsStr.setLength(domainIdsStr.length() - 1);

            String deleteCartSQL = "DELETE FROM cart WHERE user_id = ? AND domain_id IN (" + domainIdsStr + ")";
            try (PreparedStatement deleteCartStmt = connection.prepareStatement(deleteCartSQL)) {
                deleteCartStmt.setInt(1, loggedInUser.getId());
                deleteCartStmt.executeUpdate();
            }

            // Create order
            int firstDomainId = domainIdPrices.keySet().iterator().next();
            int firstRentalPeriodId = domainIdRentalPeriods.get(firstDomainId);
            int rentalMonths = getRentalMonthsFromId(firstRentalPeriodId);

            String createOrderSQL = "INSERT INTO orders (buyer_id, rental_period_id, status, created_at, expiry_date, total_price) VALUES (?, ?, ?, GETDATE(), DATEADD(month, ?, GETDATE()), ?)";
            int orderId = 0;

            try (PreparedStatement createOrderStmt = connection.prepareStatement(createOrderSQL, Statement.RETURN_GENERATED_KEYS)) {
                createOrderStmt.setInt(1, loggedInUser.getId());
                createOrderStmt.setInt(2, firstRentalPeriodId);
                createOrderStmt.setString(3, "Đang xử lý");
                createOrderStmt.setInt(4, rentalMonths);
                createOrderStmt.setDouble(5, totalPrice);
                createOrderStmt.executeUpdate();

                try (ResultSet generatedKeys = createOrderStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        orderId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Tạo đơn hàng thất bại.");
                    }
                }
            }

            // Create order details
            String insertOrderDetailsSQL = "INSERT INTO order_details (order_id, domain_id, domain_name, domain_extension, price, original_price, rental_period_id, purchase_date, status, expiry_date) VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE(), ?, DATEADD(month, ?, GETDATE()))";
            try (PreparedStatement insertDetailsStmt = connection.prepareStatement(insertOrderDetailsSQL)) {
                for (int domainId : domainIdPrices.keySet()) {
                    int rentalPeriodId = domainIdRentalPeriods.get(domainId);
                    int months = getRentalMonthsFromId(rentalPeriodId);
                    double discount = getDiscountFromId(rentalPeriodId);

                    double basePrice = domainIdPrices.get(domainId);
                    double originalPrice = basePrice * months;
                    double finalPrice = originalPrice * (1 - discount);

                    insertDetailsStmt.setInt(1, orderId);
                    insertDetailsStmt.setInt(2, domainId);
                    insertDetailsStmt.setString(3, domainNames.get(domainId));
                    insertDetailsStmt.setString(4, domainExtensions.get(domainId));
                    insertDetailsStmt.setDouble(5, finalPrice);
                    insertDetailsStmt.setDouble(6, originalPrice);
                    insertDetailsStmt.setInt(7, rentalPeriodId);
                    insertDetailsStmt.setString(8, "Đang xử lý");
                    insertDetailsStmt.setInt(9, months);

                    insertDetailsStmt.addBatch();
                }
                insertDetailsStmt.executeBatch();
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi xử lý thanh toán: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private String getRentalPeriodInfo(int rentalPeriodId) {
        try {
            RentalPeriod period = rentalPeriodRepository.getRentalPeriodById(rentalPeriodId);
            if (period != null) {
                int months = period.getMonths();
                double discount = period.getDiscount() * 100;

                if (discount > 0) {
                    return months + " tháng (Giảm " + (int) discount + "%)";
                } else {
                    return months + " tháng";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "1 tháng";
    }

    private int getRentalMonthsFromId(int rentalPeriodId) {
        try {
            RentalPeriod period = rentalPeriodRepository.getRentalPeriodById(rentalPeriodId);
            if (period != null) {
                return period.getMonths();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private double getDiscountFromId(int rentalPeriodId) {
        try {
            RentalPeriod period = rentalPeriodRepository.getRentalPeriodById(rentalPeriodId);
            if (period != null) {
                return period.getDiscount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // Test method để debug PaymentDialog
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Khởi động test PaymentDialog...");
                
                JFrame testFrame = new JFrame("Test Frame");
                testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                testFrame.setSize(300, 200);
                testFrame.setLocationRelativeTo(null);
                testFrame.setVisible(true);

                // Tạo dữ liệu test
                String[] testDomains = {"example.com", "test.net"};
                HashMap<String, Double> testPrices = new HashMap<>();
                testPrices.put("example.com", 500000.0);
                testPrices.put("test.net", 300000.0);
                
                HashMap<String, Integer> testPeriods = new HashMap<>();
                testPeriods.put("example.com", 1);
                testPeriods.put("test.net", 1);

                System.out.println("Tạo PaymentDialog với dữ liệu test...");
                System.out.println("Domains: " + java.util.Arrays.toString(testDomains));
                System.out.println("Total price: " + (testPrices.get("example.com") + testPrices.get("test.net")));

                // Tạo PaymentDialog
                PaymentDialog dialog = new PaymentDialog(testFrame, testDomains, testPrices, testPeriods, null);
                System.out.println("PaymentDialog đã được tạo. Hiển thị dialog...");
                dialog.setVisible(true);
                
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Lỗi khi test PaymentDialog: " + e.getMessage());
                JOptionPane.showMessageDialog(null, 
                    "Lỗi: " + e.getMessage(), 
                    "Test Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}