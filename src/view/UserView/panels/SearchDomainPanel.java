package view.UserView.panels;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import model.User;
import repository.DatabaseConnection;
import service.DomainExtensionService;
import utils.UserSession;

public class SearchDomainPanel extends JPanel {
    private JPanel resultPanel;
    private DomainExtensionService domainExtensionService;
    private MyDomainsPanel myDomainsPanel; // Tham chiếu đến MyDomainsPanel

    public SearchDomainPanel(DomainExtensionService domainExtensionService, MyDomainsPanel myDomainsPanel) {
        this.domainExtensionService = domainExtensionService;
        this.myDomainsPanel = myDomainsPanel; // Gán tham chiếu
        

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
                    JOptionPane.ERROR_MESSAGE
                );
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
                addDomainToCart(domain, Double.parseDouble(price));
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
     * Lưu tên miền vào giỏ hàng trong cơ sở dữ liệu.
     * @param domainName Tên miền.
     * @param price Giá của tên miền.
     */
    /**
 * Lưu tên miền vào giỏ hàng trong cơ sở dữ liệu.
 * @param domainName Tên miền.
 * @param price Giá của tên miền.
 */
    private void addDomainToCart(String domainName, double price) {
        String insertDomainQuery = "IF NOT EXISTS (SELECT 1 FROM domains WHERE name = ? AND extension = ?) " +
                                "INSERT INTO domains (name, extension, price, status) VALUES (?, ?, ?, 'Available')";
        String insertCartQuery = "INSERT INTO cart (user_id, domain_id, price) VALUES (?, (SELECT id FROM domains WHERE name = ? AND extension = ?), ?)";

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

            // Thêm tên miền vào bảng domains nếu chưa tồn tại
            insertDomainStmt.setString(1, name);
            insertDomainStmt.setString(2, extension);
            insertDomainStmt.setString(3, name);
            insertDomainStmt.setString(4, extension);
            insertDomainStmt.setDouble(5, price); // Giá lấy từ tham số
            insertDomainStmt.executeUpdate();

            // Thêm tên miền vào bảng cart
            insertCartStmt.setInt(1, userId); // ID người dùng
            insertCartStmt.setString(2, name); // Tên miền (name)
            insertCartStmt.setString(3, extension); // Phần mở rộng (extension)
            insertCartStmt.setDouble(4, price); // Giá của tên miền
            int rowsAffected = insertCartStmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "Đã thêm " + domainName + " vào giỏ hàng!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
                );

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
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private int getLoggedInUserId() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Người dùng chưa đăng nhập.");
        }
        return currentUser.getId();
    }
}