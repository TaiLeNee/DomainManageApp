package view.UserView.panels;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import model.RentalPeriod;
import model.User;
import repository.DatabaseConnection;
import repository.RentalPeriodRepository;
import service.DomainExtensionService;
import service.DomainService;
import utils.UserSession;

public class SearchDomainPanel extends JPanel {
    private JPanel resultPanel;
    private DomainExtensionService domainExtensionService;
    private MyDomainsPanel myDomainsPanel;
    private RentalPeriodRepository rentalPeriodRepository;
    private DomainService domainService;
    
    // Modern color scheme
    private static final Color PRIMARY_COLOR = new Color(74, 144, 226);
    private static final Color SECONDARY_COLOR = new Color(52, 73, 93);
    private static final Color ACCENT_COLOR = new Color(46, 204, 113);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(52, 58, 64);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    private static final Color BORDER_COLOR = new Color(233, 236, 239);

    public SearchDomainPanel(DomainExtensionService domainExtensionService, MyDomainsPanel myDomainsPanel) {
        this.domainExtensionService = domainExtensionService;
        this.myDomainsPanel = myDomainsPanel;
        this.rentalPeriodRepository = new RentalPeriodRepository();
        this.domainService = new DomainService();

        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        // Create header with gradient background
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Create main content panel
        JPanel contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, PRIMARY_COLOR,
                    0, getHeight(), SECONDARY_COLOR
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 120));

        // Title
        JLabel titleLabel = new JLabel("Tìm kiếm tên miền của bạn", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Khám phá và đăng ký tên miền hoàn hảo cho dự án của bạn", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 180));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        return headerPanel;
    }
    
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Search panel with modern design
        JPanel searchPanel = createSearchPanel();
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Results panel
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(BACKGROUND_COLOR);
        
        JScrollPane scrollPane = new JScrollPane(resultPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        return contentPanel;
    }
    
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Card shadow effect
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 15, 15);
                g2d.setColor(new Color(0, 0, 0, 5));
                g2d.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 15, 15);
                
                // Card background
                g2d.setColor(CARD_COLOR);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
            }
        };
        searchPanel.setLayout(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        searchPanel.setOpaque(false);
        
        // Search input container
        JPanel inputContainer = new JPanel(new BorderLayout(15, 0));
        inputContainer.setOpaque(false);
        
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 2, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        searchField.setBackground(Color.WHITE);
        searchField.setForeground(TEXT_PRIMARY);
        
        // Placeholder effect
        String placeholder = "Nhập tên miền (ví dụ: example.com)";
        searchField.setText(placeholder);
        searchField.setForeground(TEXT_SECONDARY);
        
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchField.getText().equals(placeholder)) {
                    searchField.setText("");
                    searchField.setForeground(TEXT_PRIMARY);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(placeholder);
                    searchField.setForeground(TEXT_SECONDARY);
                }
            }
        });
        
        JButton searchButton = createStyledButton("Tìm kiếm", PRIMARY_COLOR, Color.WHITE, new Font("Segoe UI", Font.BOLD, 16));
        searchButton.setPreferredSize(new Dimension(140, 54));
        
        // Search action
        Runnable searchAction = () -> {
            String fullDomainName = searchField.getText().trim();
            if (!fullDomainName.isEmpty() && !fullDomainName.equals(placeholder)) {
                searchDomain(fullDomainName);
            } else {
                showStyledMessage("Vui lòng nhập tên miền để tìm kiếm!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        };
        
        searchButton.addActionListener(e -> searchAction.run());
        searchField.addActionListener(e -> searchAction.run());
        
        inputContainer.add(searchField, BorderLayout.CENTER);
        inputContainer.add(searchButton, BorderLayout.EAST);
        
        searchPanel.add(inputContainer, BorderLayout.CENTER);
        return searchPanel;
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color textColor, Font font) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(bgColor.brighter());
                } else {
                    g2d.setColor(bgColor);
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setFont(font);
        button.setForeground(textColor);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }

    public void searchDomain(String fullDomainName) {
        resultPanel.removeAll();
        
        // Add loading indicator
        JLabel loadingLabel = new JLabel("Đang tìm kiếm...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        loadingLabel.setForeground(TEXT_SECONDARY);
        resultPanel.add(loadingLabel);
        resultPanel.revalidate();
        resultPanel.repaint();
        
        // Simulate loading and then show results
        SwingUtilities.invokeLater(() -> {
            resultPanel.removeAll();
            
            List<String[]> results = domainExtensionService.searchDomainWithExtensions(fullDomainName);
            
            if (results.isEmpty()) {
                JPanel noResultPanel = createNoResultPanel();
                resultPanel.add(noResultPanel);
                         } else {
                 for (String[] result : results) {
                    String domain = result[0];
                    String status = result[1];
                    String price = result[2];
                    
                    JPanel domainCard = createDomainCard(domain, status, Double.parseDouble(price));
                    resultPanel.add(domainCard);
                    resultPanel.add(Box.createRigidArea(new Dimension(0, 15)));
                }
            }
            
            resultPanel.revalidate();
            resultPanel.repaint();
        });
    }
    
    private JPanel createNoResultPanel() {
        JPanel noResultPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_COLOR);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
            }
        };
        noResultPanel.setLayout(new BorderLayout());
        noResultPanel.setBorder(BorderFactory.createEmptyBorder(40, 30, 40, 30));
        noResultPanel.setOpaque(false);
        
        JLabel noResultLabel = new JLabel("Không tìm thấy kết quả phù hợp", SwingConstants.CENTER);
        noResultLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        noResultLabel.setForeground(TEXT_SECONDARY);
        
        JLabel suggestionLabel = new JLabel("Hãy thử với tên miền khác hoặc kiểm tra chính tả", SwingConstants.CENTER);
        suggestionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        suggestionLabel.setForeground(TEXT_SECONDARY);
        
        JPanel textPanel = new JPanel(new BorderLayout(0, 10));
        textPanel.setOpaque(false);
        textPanel.add(noResultLabel, BorderLayout.CENTER);
        textPanel.add(suggestionLabel, BorderLayout.SOUTH);
        
        noResultPanel.add(textPanel, BorderLayout.CENTER);
        return noResultPanel;
    }
    
    private JPanel createDomainCard(String domain, String status, double price) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow effect
                g2d.setColor(new Color(0, 0, 0, 8));
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 12, 12);
                
                // Card background
                g2d.setColor(CARD_COLOR);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Status indicator bar
                if (status.equals("Khả dụng")) {
                    g2d.setColor(ACCENT_COLOR);
                } else {
                    g2d.setColor(DANGER_COLOR);
                }
                g2d.fillRoundRect(0, 0, 4, getHeight(), 12, 12);
                g2d.dispose();
            }
        };
        
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        // Domain info panel
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        infoPanel.setOpaque(false);
        
        JLabel domainLabel = new JLabel(domain);
        domainLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        domainLabel.setForeground(TEXT_PRIMARY);
        
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedPrice = numberFormat.format(price) + " VND/tháng";
        
        JPanel statusPricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusPricePanel.setOpaque(false);
        
        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(status.equals("Khả dụng") ? ACCENT_COLOR : DANGER_COLOR);
        
        JLabel priceLabel = new JLabel(" • " + formattedPrice);
        priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        priceLabel.setForeground(TEXT_SECONDARY);
        
        statusPricePanel.add(statusLabel);
        statusPricePanel.add(priceLabel);
        
        infoPanel.add(domainLabel);
        infoPanel.add(statusPricePanel);
        
        // Action button
        JButton addButton = createStyledButton("Thêm vào giỏ", ACCENT_COLOR, Color.WHITE, new Font("Segoe UI", Font.BOLD, 14));
        addButton.setPreferredSize(new Dimension(140, 40));
        addButton.setEnabled(status.equals("Khả dụng"));
        
                 if (!status.equals("Khả dụng")) {
             addButton = createStyledButton("Không khả dụng", new Color(200, 200, 200), Color.WHITE, new Font("Segoe UI", Font.BOLD, 14));
             addButton.setPreferredSize(new Dimension(160, 40));
         } else {
            addButton.addActionListener(e -> showRentalPeriodDialog(domain, price));
        }
        
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(addButton, BorderLayout.EAST);
        
        return card;
    }

    private void showRentalPeriodDialog(String domainName, double basePrice) {
        JDialog rentalDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chọn gói thuê", true);
        rentalDialog.setSize(500, 450);
        rentalDialog.setLocationRelativeTo(this);
        rentalDialog.setLayout(new BorderLayout());
        
        // Header panel with gradient
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, PRIMARY_COLOR, 0, getHeight(), SECONDARY_COLOR);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 80));
        
        JLabel headerTitle = new JLabel("Chọn gói thuê cho " + domainName, SwingConstants.CENTER);
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerTitle.setForeground(Color.WHITE);
        headerPanel.add(headerTitle, BorderLayout.CENTER);
        
        // Main content panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(30, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);
        
        // Base price info
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        JLabel basePriceLabel = new JLabel("Giá cơ bản: " + currencyFormat.format(basePrice) + "/tháng");
        basePriceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        basePriceLabel.setForeground(TEXT_SECONDARY);
        basePriceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(basePriceLabel);
        
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        
        // Period selection
        JLabel periodLabel = new JLabel("Chọn thời gian thuê:");
        periodLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        periodLabel.setForeground(TEXT_PRIMARY);
        periodLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(periodLabel);
        
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        JComboBox<RentalPeriodItem> periodComboBox = new JComboBox<>();
        periodComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        periodComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        periodComboBox.setMaximumSize(new Dimension(400, 40));
        periodComboBox.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        try {
            List<RentalPeriod> periods = rentalPeriodRepository.findAll();
            for (RentalPeriod period : periods) {
                double discount = period.getDiscount() * 100;
                String label = period.getMonths() + " tháng";
                if (discount > 0) {
                    label += " (Tiết kiệm " + (int) discount + "%)";
                }
                RentalPeriodItem item = new RentalPeriodItem(period, label);
                periodComboBox.addItem(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showStyledMessage("Lỗi khi tải các gói thuê: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        
        mainPanel.add(periodComboBox);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        
        // Price display
        JLabel finalPriceLabel = new JLabel();
        finalPriceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        finalPriceLabel.setForeground(ACCENT_COLOR);
        finalPriceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(finalPriceLabel);
        
        // Update price when selection changes
        updatePriceInfo(basePrice, (RentalPeriodItem) periodComboBox.getSelectedItem(), finalPriceLabel);
        periodComboBox.addActionListener(e -> {
            RentalPeriodItem selectedItem = (RentalPeriodItem) periodComboBox.getSelectedItem();
            updatePriceInfo(basePrice, selectedItem, finalPriceLabel);
        });
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton confirmButton = createStyledButton("Thêm vào giỏ hàng", ACCENT_COLOR, Color.WHITE, new Font("Segoe UI", Font.BOLD, 14));
        confirmButton.setPreferredSize(new Dimension(150, 40));
        confirmButton.addActionListener(e -> {
            RentalPeriodItem selectedItem = (RentalPeriodItem) periodComboBox.getSelectedItem();
            addDomainToCart(domainName, basePrice, selectedItem.period);
            rentalDialog.dispose();
        });
        
        JButton cancelButton = createStyledButton("Hủy", new Color(108, 117, 125), Color.WHITE, new Font("Segoe UI", Font.BOLD, 14));
        cancelButton.setPreferredSize(new Dimension(80, 40));
        cancelButton.addActionListener(e -> rentalDialog.dispose());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);
        
        rentalDialog.add(headerPanel, BorderLayout.NORTH);
        rentalDialog.add(mainPanel, BorderLayout.CENTER);
        rentalDialog.add(buttonPanel, BorderLayout.SOUTH);
        rentalDialog.setVisible(true);
    }

    private void updatePriceInfo(double basePrice, RentalPeriodItem item, JLabel priceLabel) {
        if (item == null) return;

        RentalPeriod period = item.period;
        int months = period.getMonths();
        double discount = period.getDiscount();

        double originalPrice = basePrice * months;
        double finalPrice = originalPrice * (1 - discount);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        if (discount > 0) {
            priceLabel.setText("<html>Tổng tiền: <span style='text-decoration: line-through; color: #6c757d;'>" + 
                currencyFormat.format(originalPrice) + "</span> → <span style='color: #2ecc71; font-weight: bold;'>" + 
                currencyFormat.format(finalPrice) + "</span></html>");
        } else {
            priceLabel.setText("Tổng tiền: " + currencyFormat.format(finalPrice));
        }
    }
    
    private void showStyledMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    private void addDomainToCart(String domainName, double basePrice, RentalPeriod rentalPeriod) {
        String insertDomainQuery = "IF NOT EXISTS (SELECT 1 FROM domains WHERE name = ? AND extension = ?) " +
                "INSERT INTO domains (name, extension, price, status) VALUES (?, ?, ?, N'Sẵn sàng')";
        String checkCartQuery = "SELECT COUNT(*) FROM cart c JOIN domains d ON c.domain_id = d.id WHERE c.user_id = ? AND d.name = ? AND d.extension = ?";
        String insertCartQuery = "INSERT INTO cart (user_id, domain_id, price, rental_period_id) VALUES (?, (SELECT id FROM domains WHERE name = ? AND extension = ?), ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement insertDomainStmt = connection.prepareStatement(insertDomainQuery);
                PreparedStatement checkCartStmt = connection.prepareStatement(checkCartQuery);
                PreparedStatement insertCartStmt = connection.prepareStatement(insertCartQuery)) {

            int userId = getLoggedInUserId();
            if (userId <= 0) {
                throw new SQLException("Không tìm thấy thông tin người dùng.");
            }

            String[] domainParts = domainName.split("\\.", 2);
            if (domainParts.length != 2) {
                throw new SQLException("Tên miền không hợp lệ: " + domainName);
            }
            String name = domainParts[0];
            String extension = "." + domainParts[1];

            // Kiểm tra xem domain đã có trong cart chưa
            checkCartStmt.setInt(1, userId);
            checkCartStmt.setString(2, name);
            checkCartStmt.setString(3, extension);
            ResultSet rs = checkCartStmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                showStyledMessage(
                        "❗ Tên miền " + domainName + " đã có trong giỏ hàng của bạn!\n\n" +
                        "💡 Tip: Bạn có thể đúp chuột vào tên miền trong giỏ hàng để chỉnh sửa thời gian thuê.",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int months = rentalPeriod.getMonths();
            double discount = rentalPeriod.getDiscount();
            double finalPrice = basePrice * months * (1 - discount);

            insertDomainStmt.setString(1, name);
            insertDomainStmt.setString(2, extension);
            insertDomainStmt.setString(3, name);
            insertDomainStmt.setString(4, extension);
            insertDomainStmt.setDouble(5, basePrice);
            insertDomainStmt.executeUpdate();

            insertCartStmt.setInt(1, userId);
            insertCartStmt.setString(2, name);
            insertCartStmt.setString(3, extension);
            insertCartStmt.setDouble(4, finalPrice);
            insertCartStmt.setInt(5, rentalPeriod.getId());
            int rowsAffected = insertCartStmt.executeUpdate();

            if (rowsAffected > 0) {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                showStyledMessage(
                        "✅ Đã thêm " + domainName + " vào giỏ hàng!\n\n" +
                                "📅 Thời gian thuê: " + months + " tháng\n" +
                                "💰 Giá: " + currencyFormat.format(finalPrice),
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);

                myDomainsPanel.loadDomainsFromDatabase();
            } else {
                throw new SQLException("Không thể thêm tên miền vào giỏ hàng.");
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("UQ_cart_user_domain")) {
                showStyledMessage(
                        "❗ Tên miền " + domainName + " đã có trong giỏ hàng của bạn!\n\n" +
                        "💡 Tip: Bạn có thể đúp chuột vào tên miền trong giỏ hàng để chỉnh sửa thời gian thuê.",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                showStyledMessage(
                        "Lỗi khi thêm vào cơ sở dữ liệu: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private int getLoggedInUserId() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Người dùng chưa đăng nhập.");
        }
        return currentUser.getId();
    }

    private static class RentalPeriodItem {
        private RentalPeriod period;
        private String label;

        public RentalPeriodItem(RentalPeriod period, String label) {
            this.period = period;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
