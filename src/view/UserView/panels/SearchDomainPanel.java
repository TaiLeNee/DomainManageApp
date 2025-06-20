package view.UserView.panels;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

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
    private MyDomainsPanel myDomainsPanel; // Tham chiếu đến MyDomainsPanel
    private RentalPeriodRepository rentalPeriodRepository;
    private DomainService domainService;

    public SearchDomainPanel(DomainExtensionService domainExtensionService, MyDomainsPanel myDomainsPanel) {
        this.domainExtensionService = domainExtensionService;
        this.myDomainsPanel = myDomainsPanel; // Gán tham chiếu
        this.rentalPeriodRepository = new RentalPeriodRepository();
        this.domainService = new DomainService();

        setLayout(new BorderLayout());

        // Tiêu đề
        JLabel titleLabel = new JLabel("Kết quả tìm kiếm tên miền", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Panel chứa thanh tìm kiếm
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));

        JLabel searchLabel = new JLabel("Nhập tên miền:");
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        JTextField searchField = new JTextField(30);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.setBackground(new Color(41, 59, 95));
        searchButton.setForeground(Color.BLACK);
        searchButton.setFocusPainted(false);

        // Xử lý sự kiện khi nhấn nút "Tìm kiếm"
        searchButton.addActionListener(e -> {
            String fullDomainName = searchField.getText().trim();
            if (!fullDomainName.isEmpty()) {
                searchDomain(fullDomainName);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Vui lòng nhập tên miền để tìm kiếm!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Thêm các thành phần vào panel tìm kiếm
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        add(searchPanel, BorderLayout.NORTH);

        // Panel chứa kết quả
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(resultPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Tìm kiếm tên miền và hiển thị kết quả.
     * 
     * @param fullDomainName Tên miền đầy đủ người dùng nhập.
     */
    public void searchDomain(String fullDomainName) {
        // Xóa kết quả cũ
        resultPanel.removeAll();

        // Lấy danh sách domain extensions từ cơ sở dữ liệu
        List<String[]> results = domainExtensionService.searchDomainWithExtensions(fullDomainName);

        // Kiểm tra nếu không có kết quả
        if (results.isEmpty()) {
            JLabel noResultLabel = new JLabel("Không tìm thấy kết quả phù hợp.");
            noResultLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            noResultLabel.setForeground(Color.RED);
            resultPanel.add(noResultLabel);
        }

        // Thêm kết quả mới
        for (String[] result : results) {
            String domain = result[0];
            String status = result[1];
            String price = result[2];

            // Định dạng giá tiền với dấu phẩy
            NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
            String formattedPrice = numberFormat.format(Double.parseDouble(price)) + " VND";

            JPanel rowPanel = new JPanel(new BorderLayout());
            rowPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            JLabel domainLabel = new JLabel(domain);
            domainLabel.setFont(new Font("Arial", Font.PLAIN, 16));

            JLabel statusLabel = new JLabel(status);
            statusLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            statusLabel.setForeground(status.equals("Khả dụng") ? Color.GREEN : Color.RED);

            JLabel priceLabel = new JLabel(formattedPrice);
            priceLabel.setFont(new Font("Arial", Font.PLAIN, 16));

            JButton addToCartButton = new JButton("Thêm vào giỏ hàng");
            addToCartButton.setFont(new Font("Arial", Font.BOLD, 14));
            addToCartButton.setBackground(new Color(41, 59, 95));
            addToCartButton.setForeground(Color.BLACK);
            addToCartButton.setEnabled(status.equals("Khả dụng"));
            addToCartButton.addActionListener(e -> {
                showRentalPeriodDialog(domain, Double.parseDouble(price));
            });

            JPanel infoPanel = new JPanel(new GridLayout(1, 3, 10, 0));
            infoPanel.add(domainLabel);
            infoPanel.add(statusLabel);
            infoPanel.add(priceLabel);

            rowPanel.add(infoPanel, BorderLayout.CENTER);
            rowPanel.add(addToCartButton, BorderLayout.EAST);

            resultPanel.add(rowPanel);
        }

        // Cập nhật giao diện
        resultPanel.revalidate();
        resultPanel.repaint();
    }

    /**
     * Hiển thị dialog chọn thời gian thuê và giảm giá tương ứng
     * 
     * @param domainName Tên miền
     * @param basePrice  Giá cơ bản
     */
    private void showRentalPeriodDialog(String domainName, double basePrice) {
        // Tạo dialog chọn thời gian thuê
        JDialog rentalDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chọn thời gian thuê", true);
        rentalDialog.setSize(400, 350);
        rentalDialog.setLocationRelativeTo(this);
        rentalDialog.setLayout(new BorderLayout());

        // Panel chính
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Thông tin tên miền
        JLabel domainLabel = new JLabel("Tên miền: " + domainName);
        domainLabel.setFont(new Font("Arial", Font.BOLD, 16));
        domainLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(domainLabel);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Giá cơ bản
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        JLabel basePriceLabel = new JLabel("Giá cơ bản (1 tháng): " + currencyFormat.format(basePrice));
        basePriceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        basePriceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(basePriceLabel);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Chọn thời gian thuê
        JLabel periodLabel = new JLabel("Chọn thời gian thuê:");
        periodLabel.setFont(new Font("Arial", Font.BOLD, 14));
        periodLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(periodLabel);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // ComboBox chọn thời gian thuê
        JComboBox<RentalPeriodItem> periodComboBox = new JComboBox<>();

        try {
            // Lấy danh sách các gói thuê từ cơ sở dữ liệu
            List<RentalPeriod> periods = rentalPeriodRepository.findAll();
            for (RentalPeriod period : periods) {
                double discount = period.getDiscount() * 100;
                String label = period.getMonths() + " tháng";
                if (discount > 0) {
                    label += " (Giảm " + (int) discount + "%)";
                }

                RentalPeriodItem item = new RentalPeriodItem(period, label);
                periodComboBox.addItem(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải các gói thuê: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }

        periodComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        periodComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        periodComboBox.setMaximumSize(new Dimension(300, 30));
        mainPanel.add(periodComboBox);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Hiển thị thông tin giá
        JLabel finalPriceLabel = new JLabel();
        finalPriceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        finalPriceLabel.setForeground(new Color(0, 120, 0));
        finalPriceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(finalPriceLabel);

        // Cập nhật giá khi chọn thời gian thuê
        updatePriceInfo(basePrice, (RentalPeriodItem) periodComboBox.getSelectedItem(), finalPriceLabel);

        periodComboBox.addActionListener(e -> {
            RentalPeriodItem selectedItem = (RentalPeriodItem) periodComboBox.getSelectedItem();
            updatePriceInfo(basePrice, selectedItem, finalPriceLabel);
        });

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Panel chứa các nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Nút xác nhận
        JButton confirmButton = new JButton("Thêm vào giỏ hàng");
        confirmButton.setBackground(new Color(39, 174, 96));
        confirmButton.setForeground(Color.BLACK);
        confirmButton.setFont(new Font("Arial", Font.BOLD, 14));
        confirmButton.addActionListener(e -> {
            RentalPeriodItem selectedItem = (RentalPeriodItem) periodComboBox.getSelectedItem();
            addDomainToCart(domainName, basePrice, selectedItem.period);
            rentalDialog.dispose();
        });

        // Nút hủy
        JButton cancelButton = new JButton("Hủy");
        cancelButton.setBackground(new Color(231, 76, 60));
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.addActionListener(e -> rentalDialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        rentalDialog.add(mainPanel, BorderLayout.CENTER);
        rentalDialog.add(buttonPanel, BorderLayout.SOUTH);
        rentalDialog.setVisible(true);
    }

    /**
     * Cập nhật thông tin giá dựa trên thời gian thuê đã chọn
     */
    private void updatePriceInfo(double basePrice, RentalPeriodItem item, JLabel priceLabel) {
        if (item == null)
            return;

        RentalPeriod period = item.period;
        int months = period.getMonths();
        double discount = period.getDiscount();

        double originalPrice = basePrice * months;
        double finalPrice = originalPrice * (1 - discount);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Correct the HTML formatting for the price display
        if (discount > 0) {
            priceLabel.setText("<html>Giá thuê: <s>" + currencyFormat.format(originalPrice) + "</s> → "
                    + "<b>" + currencyFormat.format(finalPrice) + "</b></html>");
        } else {
            priceLabel.setText("Giá thuê: " + currencyFormat.format(finalPrice));
        }
    }

    /**
     * Lưu tên miền vào giỏ hàng trong cơ sở dữ liệu.
     * 
     * @param domainName   Tên miền
     * @param basePrice    Giá cơ bản một tháng
     * @param rentalPeriod Gói thuê đã chọn
     */
    private void addDomainToCart(String domainName, double basePrice, RentalPeriod rentalPeriod) {
        String insertDomainQuery = "IF NOT EXISTS (SELECT 1 FROM domains WHERE name = ? AND extension = ?) " +
                "INSERT INTO domains (name, extension, price, status) VALUES (?, ?, ?, N'Sẵn sàng')";
        String insertCartQuery = "INSERT INTO cart (user_id, domain_id, price, rental_period_id) VALUES (?, (SELECT id FROM domains WHERE name = ? AND extension = ?), ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement insertDomainStmt = connection.prepareStatement(insertDomainQuery);
                PreparedStatement insertCartStmt = connection.prepareStatement(insertCartQuery)) {

            // Lấy ID người dùng hiện tại
            int userId = getLoggedInUserId();
            if (userId <= 0) {
                throw new SQLException("Không tìm thấy thông tin người dùng.");
            }

            // Tách domainName thành name và extension
            String[] domainParts = domainName.split("\\.", 2); // Tách tên miền thành 2 phần: name và extension
            if (domainParts.length != 2) {
                throw new SQLException("Tên miền không hợp lệ: " + domainName);
            }
            String name = domainParts[0];
            String extension = "." + domainParts[1];

            // Tính giá sau khi giảm
            int months = rentalPeriod.getMonths();
            double discount = rentalPeriod.getDiscount();
            double finalPrice = basePrice * months * (1 - discount);

            // Thêm tên miền vào bảng domains nếu chưa tồn tại
            insertDomainStmt.setString(1, name);
            insertDomainStmt.setString(2, extension);
            insertDomainStmt.setString(3, name);
            insertDomainStmt.setString(4, extension);
            insertDomainStmt.setDouble(5, basePrice); // Giá cơ bản 1 tháng
            insertDomainStmt.executeUpdate();

            // Thêm tên miền vào bảng cart với thông tin gói thuê
            insertCartStmt.setInt(1, userId); // ID người dùng
            insertCartStmt.setString(2, name); // Tên miền (name)
            insertCartStmt.setString(3, extension); // Phần mở rộng (extension)
            insertCartStmt.setDouble(4, finalPrice); // Giá sau khi giảm
            insertCartStmt.setInt(5, rentalPeriod.getId()); // ID gói thuê
            int rowsAffected = insertCartStmt.executeUpdate();

            if (rowsAffected > 0) {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                JOptionPane.showMessageDialog(
                        this,
                        "Đã thêm " + domainName + " vào giỏ hàng!\n" +
                                "Thời gian thuê: " + months + " tháng\n" +
                                "Giá: " + currencyFormat.format(finalPrice),
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);

                // Gọi phương thức loadDomainsFromDatabase của MyDomainsPanel
                myDomainsPanel.loadDomainsFromDatabase();
            } else {
                throw new SQLException("Không thể thêm tên miền vào giỏ hàng.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi khi thêm vào cơ sở dữ liệu: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getLoggedInUserId() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Người dùng chưa đăng nhập.");
        }
        return currentUser.getId();
    }

    /**
     * Lớp wrapper để hiển thị nhãn tùy chỉnh trong combobox
     */
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