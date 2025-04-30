package view.UserView.panels;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import service.DomainExtensionService;

public class HomePanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel mainContentPanel;

    public HomePanel(CardLayout cardLayout, JPanel mainContentPanel) {
        this.cardLayout = cardLayout;
        this.mainContentPanel = mainContentPanel;

        setLayout(new BorderLayout());

        // Câu chào
        JLabel welcomeLabel = new JLabel("Chào mừng đến với Hệ thống Quản lý Tên miền!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(welcomeLabel, BorderLayout.NORTH);

        // Panel chứa thanh tìm kiếm
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));

        JLabel searchLabel = new JLabel("Nhập tên miền:");
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.setBackground(new Color(41, 59, 95));
        searchButton.setForeground(Color.BLACK);
        searchButton.setFocusPainted(false);

        // Xử lý sự kiện khi nhấn nút "Tìm kiếm"
        searchButton.addActionListener(e -> {
            String domainName = searchField.getText().trim();
            if (!domainName.isEmpty()) {
                // Chuyển sang SearchDomainPanel và truyền tên miền
                Component[] components = mainContentPanel.getComponents();
                for (Component component : components) {
                    if (component instanceof SearchDomainPanel) {
                        SearchDomainPanel searchDomainPanel = (SearchDomainPanel) component;
                        searchDomainPanel.searchDomain(domainName);
                        break;
                    }
                }
                cardLayout.show(mainContentPanel, "SEARCH_DOMAIN_PANEL");
            } else {
                JOptionPane.showMessageDialog(
                    HomePanel.this,
                    "Vui lòng nhập tên miền để tìm kiếm!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // Thêm các thành phần vào panel tìm kiếm
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        add(searchPanel, BorderLayout.CENTER);

        // Thêm danh sách domain extensions
        add(createDomainExtensionsPanel(), BorderLayout.SOUTH);
    }

    private JPanel createDomainExtensionsPanel() {
        JPanel extensionsPanel = new JPanel();
        extensionsPanel.setLayout(new BoxLayout(extensionsPanel, BoxLayout.Y_AXIS));
        extensionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
        // Lấy dữ liệu từ cơ sở dữ liệu
        DomainExtensionService domainExtensionService = new DomainExtensionService();
        List<String[]> domainExtensions = domainExtensionService.getAllDomainExtensions();
    
        for (String[] extension : domainExtensions) {
            // Tạo JPanel thay vì JButton
            JPanel extensionPanel = new JPanel(new BorderLayout());
            extensionPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            extensionPanel.setBackground(Color.WHITE);
            extensionPanel.setPreferredSize(new Dimension(400, 50));
    
            JLabel nameLabel = new JLabel(extension[0]);
            nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
            nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    
            JLabel descriptionLabel = new JLabel(extension[1]);
            descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            descriptionLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    
            JLabel priceLabel = new JLabel(extension[2]);
            priceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            priceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            priceLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    
            extensionPanel.add(nameLabel, BorderLayout.WEST);
            extensionPanel.add(descriptionLabel, BorderLayout.CENTER);
            extensionPanel.add(priceLabel, BorderLayout.EAST);
    
            extensionsPanel.add(extensionPanel);
            extensionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
    
        // Thêm JScrollPane để cuộn
        JScrollPane scrollPane = new JScrollPane(extensionsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Tăng tốc độ cuộn
    
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(scrollPane, BorderLayout.CENTER);
    
        return containerPanel;
    }
}