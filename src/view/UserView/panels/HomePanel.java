package view.UserView.panels;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import model.Domain;
import model.User;
import repository.DomainRepository;
import service.DomainService;

public class HomePanel extends JPanel {
    // Modern color palette
    private static final Color BG_COLOR = new Color(248, 250, 252);
    private static final Color PRIMARY_COLOR = new Color(41, 59, 95);
    private static final Color SECONDARY_COLOR = new Color(66, 91, 138);
    private static final Color ACCENT_COLOR = new Color(255, 111, 0);
    private static final Color TEXT_PRIMARY = new Color(34, 40, 49);
    private static final Color TEXT_SECONDARY = new Color(130, 139, 162);
    private static final Color BORDER_COLOR = new Color(230, 235, 241);

    // Modern fonts
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    private User currentUser;
    private DomainService domainService;
    private DomainRepository domainRepository;

    private JTextField searchField;
    private JPanel resultPanel;
    private JPanel welcomePanel;
    private CardLayout resultCardLayout;
    private JPanel searchResultsPanel;
    private JPanel noResultsPanel;
    private JPanel loadingPanel;

    public HomePanel(User currentUser) {
        this.currentUser = currentUser;
        this.domainService = new DomainService();
        this.domainRepository = new DomainRepository();

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        // Initialize panels
        initComponents();
    }

    private void initComponents() {
        // Create welcome section
        welcomePanel = createWelcomePanel();
        add(welcomePanel, BorderLayout.NORTH);

        // Create main content panel with padding
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 30, 30, 30));

        // Create search panel
        JPanel searchPanel = createSearchPanel();
        contentPanel.add(searchPanel, BorderLayout.NORTH);

        // Create result panel with card layout
        resultPanel = new JPanel();
        resultCardLayout = new CardLayout();
        resultPanel.setLayout(resultCardLayout);
        resultPanel.setBackground(Color.WHITE);

        // Create search results panel
        searchResultsPanel = new JPanel();
        searchResultsPanel.setLayout(new BorderLayout());
        searchResultsPanel.setBackground(Color.WHITE);

        // Create no results panel
        noResultsPanel = createNoResultsPanel();

        // Create loading panel
        loadingPanel = createLoadingPanel();

        // Add panels to card layout
        resultPanel.add(createInitialPanel(), "INITIAL");
        resultPanel.add(searchResultsPanel, "RESULTS");
        resultPanel.add(noResultsPanel, "NO_RESULTS");
        resultPanel.add(loadingPanel, "LOADING");

        // Show initial panel
        resultCardLayout.show((Container) resultPanel, "INITIAL");

        contentPanel.add(resultPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();

                // Enable antialiasing
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, PRIMARY_COLOR,
                        getWidth(), getHeight(), SECONDARY_COLOR);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Add a subtle pattern overlay
                g2d.setColor(new Color(255, 255, 255, 20));
                for (int i = 0; i < getWidth(); i += 20) {
                    for (int j = 0; j < getHeight(); j += 20) {
                        g2d.fillOval(i, j, 4, 4);
                    }
                }

                g2d.dispose();
            }
        };

        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(getWidth(), 240));

        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.setOpaque(false);
        innerPanel.setBorder(new EmptyBorder(40, 50, 40, 50));

        // Welcome text with time-based greeting
        String greeting = getTimeBasedGreeting();

        JLabel welcomeLabel = new JLabel(greeting + ", " + currentUser.getFullName() + "!");
        welcomeLabel.setFont(FONT_TITLE);
        welcomeLabel.setForeground(Color.WHITE);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Tìm kiếm và quản lý tên miền của bạn tại một nơi");
        subtitleLabel.setFont(FONT_REGULAR);
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));

        // Current date display
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy");
        String dateStr = dateFormat.format(new Date());

        JLabel dateLabel = new JLabel(dateStr);
        dateLabel.setFont(FONT_REGULAR);
        dateLabel.setForeground(new Color(255, 255, 255, 200));

        // Left panel for texts
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(welcomeLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(subtitleLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        leftPanel.add(dateLabel);

        innerPanel.add(leftPanel, BorderLayout.WEST);

        // Right panel for decorative elements or stats
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(300, 0));

        try {
            ImageIcon icon = new ImageIcon("src/img/null.png");
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(250, 150, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(img));
                rightPanel.add(imageLabel);
            }
        } catch (Exception e) {
            // No need to handle, just don't show image
        }

        innerPanel.add(rightPanel, BorderLayout.EAST);
        panel.add(innerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 20, 0),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1, true),
                        BorderFactory.createEmptyBorder(25, 25, 25, 25))));

        // Search title
        JLabel titleLabel = new JLabel("Tìm tên miền");
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setForeground(TEXT_PRIMARY);

        // Search description
        JLabel descLabel = new JLabel("Tìm kiếm tên miền có sẵn hoặc kiểm tra tên miền của bạn");
        descLabel.setFont(FONT_REGULAR);
        descLabel.setForeground(TEXT_SECONDARY);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(descLabel, BorderLayout.SOUTH);

        // Search field with rounded border and icon
        searchField = new JTextField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                super.paintComponent(g2);
            }
        };

        searchField.setFont(FONT_REGULAR);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        searchField.setPreferredSize(new Dimension(0, 45));

        // Create search button with gradient
        JButton searchButton = new JButton("Tìm kiếm") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient for button
                GradientPaint gradient = new GradientPaint(
                        0, 0, ACCENT_COLOR,
                        0, getHeight(), new Color(255, 132, 41));
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                super.paintComponent(g2);
            }
        };

        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchButton.setBorderPainted(false);
        searchButton.setContentAreaFilled(false);
        searchButton.setFocusPainted(false);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.setPreferredSize(new Dimension(120, 45));

        // Add action listener
        searchButton.addActionListener(e -> searchDomain());

        // Add enter key listener to search field
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchDomain();
                }
            }
        });

        // Search input panel
        JPanel searchInputPanel = new JPanel(new BorderLayout(10, 0));
        searchInputPanel.setOpaque(false);
        searchInputPanel.add(searchField, BorderLayout.CENTER);
        searchInputPanel.add(searchButton, BorderLayout.EAST);

        // Add all to main panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(searchInputPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInitialPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1, true),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)));

        // Domain extensions section title
        JLabel titleLabel = new JLabel("Phần mở rộng tên miền phổ biến");
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Domain extensions cards panel
        JPanel extensionsPanel = new JPanel(new GridLayout(1, 5, 15, 0));
        extensionsPanel.setOpaque(false);
        extensionsPanel.setMaximumSize(new Dimension(900, 120));
        extensionsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add extension cards
        extensionsPanel.add(createExtensionCard(".com", "200.000 VND", "Thương mại, phổ biến nhất"));
        extensionsPanel.add(createExtensionCard(".net", "150.000 VND", "Mạng, công nghệ"));
        extensionsPanel.add(createExtensionCard(".org", "180.000 VND", "Tổ chức phi lợi nhuận"));
        extensionsPanel.add(createExtensionCard(".vn", "400.000 VND", "Tên miền Việt Nam"));
        extensionsPanel.add(createExtensionCard(".info", "120.000 VND", "Thông tin, dữ liệu"));

        panel.add(extensionsPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 40)));

        // Features section
        JLabel featuresLabel = new JLabel("Tại sao chọn dịch vụ của chúng tôi?");
        featuresLabel.setFont(FONT_SUBTITLE);
        featuresLabel.setForeground(TEXT_PRIMARY);
        featuresLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(featuresLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Features panel in grid
        JPanel featuresPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        featuresPanel.setOpaque(false);
        featuresPanel.setMaximumSize(new Dimension(900, 300));
        featuresPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add feature cards
        featuresPanel.add(
                createFeatureCard("Giá cả cạnh tranh", "Chúng tôi cung cấp mức giá tốt nhất cho dịch vụ tên miền"));
        featuresPanel.add(createFeatureCard("Hỗ trợ 24/7", "Đội ngũ kỹ thuật hỗ trợ 24/7 cho mọi vấn đề"));
        featuresPanel.add(
                createFeatureCard("Bảng điều khiển dễ sử dụng", "Quản lý tên miền dễ dàng với giao diện thân thiện"));
        featuresPanel
                .add(createFeatureCard("Bảo mật cao", "Bảo vệ tên miền của bạn với các tính năng bảo mật tiên tiến"));

        panel.add(featuresPanel);

        return panel;
    }

    private JPanel createExtensionCard(String extension, String price, String description) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw background
                GradientPaint gradient;

                switch (extension) {
                    case ".com":
                        gradient = new GradientPaint(
                                0, 0, new Color(52, 152, 219),
                                getWidth(), getHeight(), new Color(41, 128, 185));
                        break;
                    case ".net":
                        gradient = new GradientPaint(
                                0, 0, new Color(155, 89, 182),
                                getWidth(), getHeight(), new Color(142, 68, 173));
                        break;
                    case ".org":
                        gradient = new GradientPaint(
                                0, 0, new Color(26, 188, 156),
                                getWidth(), getHeight(), new Color(22, 160, 133));
                        break;
                    case ".vn":
                        gradient = new GradientPaint(
                                0, 0, new Color(231, 76, 60),
                                getWidth(), getHeight(), new Color(192, 57, 43));
                        break;
                    default:
                        gradient = new GradientPaint(
                                0, 0, new Color(243, 156, 18),
                                getWidth(), getHeight(), new Color(230, 126, 34));
                }

                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Add subtle pattern
                g2.setColor(new Color(255, 255, 255, 20));
                for (int i = 0; i < getWidth(); i += 15) {
                    for (int j = 0; j < getHeight(); j += 15) {
                        g2.fillOval(i, j, 2, 2);
                    }
                }

                g2.dispose();
            }
        };

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Extension name
        JLabel extensionLabel = new JLabel(extension);
        extensionLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        extensionLabel.setForeground(Color.WHITE);
        extensionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Price
        JLabel priceLabel = new JLabel(price);
        priceLabel.setFont(FONT_REGULAR);
        priceLabel.setForeground(new Color(255, 255, 255, 220));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Description
        JLabel descLabel = new JLabel("<html><div style='text-align: center;'>" + description + "</div></html>");
        descLabel.setFont(FONT_SMALL);
        descLabel.setForeground(new Color(255, 255, 255, 200));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(extensionLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(priceLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(descLabel);

        // Add click handler to search for this extension
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                searchField.setText(extension);
                searchDomain();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2, true),
                        BorderFactory.createEmptyBorder(13, 13, 13, 13)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            }
        });

        return panel;
    }

    private JPanel createFeatureCard(String title, String description) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 40), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        // Icon (placeholder)
        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(40, 40));
        iconLabel.setHorizontalAlignment(JLabel.CENTER);
        iconLabel.setVerticalAlignment(JLabel.CENTER);

        // Use text as placeholder for icon
        String iconText = title.substring(0, 1).toUpperCase();
        iconLabel.setText(iconText);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setBackground(PRIMARY_COLOR);
        iconLabel.setOpaque(true);

        // Make the icon circular
        iconLabel.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Text panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel("<html><div style='width:200px'>" + description + "</div></html>");
        descLabel.setFont(FONT_REGULAR);
        descLabel.setForeground(TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(descLabel);

        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createNoResultsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1, true),
                BorderFactory.createEmptyBorder(50, 50, 50, 50)));

        // Icon
        JLabel iconLabel = new JLabel("🔍");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Message
        JLabel messageLabel = new JLabel("Không tìm thấy kết quả");
        messageLabel.setFont(FONT_SUBTITLE);
        messageLabel.setForeground(TEXT_PRIMARY);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Description
        JLabel descLabel = new JLabel("Vui lòng thử tìm kiếm với từ khóa khác");
        descLabel.setFont(FONT_REGULAR);
        descLabel.setForeground(TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(iconLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(messageLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(descLabel);

        return panel;
    }

    private JPanel createLoadingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1, true),
                BorderFactory.createEmptyBorder(50, 50, 50, 50)));

        // Loading spinner (using text as placeholder)
        JLabel spinnerLabel = new JLabel("Loading...");
        spinnerLabel.setFont(FONT_SUBTITLE);
        spinnerLabel.setForeground(PRIMARY_COLOR);
        spinnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Message
        JLabel messageLabel = new JLabel("Đang tìm kiếm tên miền...");
        messageLabel.setFont(FONT_REGULAR);
        messageLabel.setForeground(TEXT_SECONDARY);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(spinnerLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(messageLabel);

        return panel;
    }

    private void searchDomain() {
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập tên miền hoặc phần mở rộng để tìm kiếm",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Show loading panel
        resultCardLayout.show((Container) resultPanel, "LOADING");

        // Create worker thread for search
        SwingWorker<List<Domain>, Void> worker = new SwingWorker<List<Domain>, Void>() {
            @Override
            protected List<Domain> doInBackground() throws Exception {
                // Simulate network delay
                Thread.sleep(800);

                // Call domain service to search
                return domainRepository.searchDomains(searchTerm);
            }

            @Override
            protected void done() {
                try {
                    List<Domain> results = get();

                    if (results != null && !results.isEmpty()) {
                        // Show results
                        displaySearchResults(results);
                        resultCardLayout.show((Container) resultPanel, "RESULTS");
                    } else {
                        // Show no results panel
                        resultCardLayout.show((Container) resultPanel, "NO_RESULTS");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Show error message
                    JOptionPane.showMessageDialog(
                            HomePanel.this,
                            "Có lỗi xảy ra khi tìm kiếm: " + e.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    resultCardLayout.show((Container) resultPanel, "INITIAL");
                }
            }
        };

        worker.execute();
    }

    private void displaySearchResults(List<Domain> domains) {
        // Clear previous results
        searchResultsPanel.removeAll();

        // Create results header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        JLabel resultLabel = new JLabel("Kết quả tìm kiếm: " + domains.size() + " tên miền");
        resultLabel.setFont(FONT_SUBTITLE);
        resultLabel.setForeground(TEXT_PRIMARY);

        headerPanel.add(resultLabel, BorderLayout.WEST);

        // Create results content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Add result items
        for (Domain domain : domains) {
            JPanel domainPanel = createDomainResultPanel(domain);
            domainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(domainPanel);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        searchResultsPanel.add(headerPanel, BorderLayout.NORTH);
        searchResultsPanel.add(scrollPane, BorderLayout.CENTER);
        searchResultsPanel.revalidate();
        searchResultsPanel.repaint();
    }

    private JPanel createDomainResultPanel(Domain domain) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        // Domain name panel
        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.setOpaque(false);

        String fullDomainName = domain.getName() + domain.getExtension();
        JLabel nameLabel = new JLabel(fullDomainName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(TEXT_PRIMARY);

        // Status label
        JLabel statusLabel = new JLabel(domain.getStatus());
        statusLabel.setFont(FONT_SMALL);

        // Set color based on status
        if ("Khả dụng".equals(domain.getStatus())) {
            statusLabel.setForeground(new Color(46, 204, 113));
        } else {
            statusLabel.setForeground(new Color(231, 76, 60));
        }

        namePanel.add(nameLabel, BorderLayout.NORTH);
        namePanel.add(statusLabel, BorderLayout.SOUTH);

        // Price panel
        JPanel pricePanel = new JPanel(new BorderLayout());
        pricePanel.setOpaque(false);

        JLabel priceLabel = new JLabel(formatPrice(domain.getPrice()));
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        priceLabel.setForeground(ACCENT_COLOR);
        priceLabel.setHorizontalAlignment(JLabel.RIGHT);

        JLabel priceDescLabel = new JLabel("giá / năm");
        priceDescLabel.setFont(FONT_SMALL);
        priceDescLabel.setForeground(TEXT_SECONDARY);
        priceDescLabel.setHorizontalAlignment(JLabel.RIGHT);

        pricePanel.add(priceLabel, BorderLayout.NORTH);
        pricePanel.add(priceDescLabel, BorderLayout.SOUTH);

        // Action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);

        // Add to cart button
        JButton addButton = new JButton("Thêm vào giỏ hàng") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient for button
                if ("Khả dụng".equals(domain.getStatus())) {
                    GradientPaint gradient = new GradientPaint(
                            0, 0, ACCENT_COLOR,
                            0, getHeight(), new Color(255, 132, 41));
                    g2.setPaint(gradient);
                } else {
                    g2.setColor(new Color(200, 200, 200));
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                super.paintComponent(g2);
            }
        };

        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.setBorderPainted(false);
        addButton.setContentAreaFilled(false);
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.setEnabled("Khả dụng".equals(domain.getStatus()));

        addButton.addActionListener(e -> addDomainToCart(domain));

        actionPanel.add(addButton);

        // Add all components to main panel
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(namePanel, BorderLayout.WEST);
        leftPanel.add(pricePanel, BorderLayout.EAST);

        panel.add(leftPanel, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.EAST);

        return panel;
    }

    private void addDomainToCart(Domain domain) {
        // Access MyDomainsPanel to add domain
        try {
            // Find parent container to access MyDomainsPanel
            Container parent = this.getParent();
            while (parent != null && !(parent.getLayout() instanceof CardLayout)) {
                parent = parent.getParent();
            }

            if (parent != null) {
                // Find MyDomainsPanel in the parent
                for (Component comp : parent.getComponents()) {
                    if (comp instanceof MyDomainsPanel) {
                        ((MyDomainsPanel) comp).addDomain(domain);

                        // Show success message
                        JOptionPane.showMessageDialog(
                                this,
                                "Đã thêm tên miền " + domain.getName() + domain.getExtension() + " vào giỏ hàng!",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);

                        return;
                    }
                }
            }

            // Fallback if cannot find MyDomainsPanel
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể thêm tên miền vào giỏ hàng. Vui lòng thử lại sau.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi khi thêm tên miền vào giỏ hàng: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatPrice(double price) {
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,### VND");
        return formatter.format(price);
    }

    private String getTimeBasedGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour < 12) {
            return "Chào buổi sáng";
        } else if (hour < 18) {
            return "Chào buổi chiều";
        } else {
            return "Chào buổi tối";
        }
    }
}