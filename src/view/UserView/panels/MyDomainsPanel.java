package view.UserView.panels;

import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import model.User;
import repository.DatabaseConnection;
import utils.ValidationUtils;

public class MyDomainsPanel extends JPanel {
    private JTable domainsTable;
    private DefaultTableModel tableModel;
    private JButton payButton;
    private JButton deleteButton;
    private double totalPrice = 0.0;
    private User loggedInUser;
    private JComboBox<String> rentalPeriodComboBox; // ComboBox chọn thời gian thuê

    public MyDomainsPanel(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        // Khởi tạo ComboBox chọn thời gian thuê
        rentalPeriodComboBox = new JComboBox<>();
        rentalPeriodComboBox.addItem("1 tháng - Không giảm giá");
        rentalPeriodComboBox.addItem("6 tháng - Giảm 10%");
        rentalPeriodComboBox.addItem("12 tháng - Giảm 20%");

        JLabel label = new JLabel("Giỏ hàng của tôi", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(label, BorderLayout.NORTH);

        // Tạo bảng hiển thị tên miền
        String[] columnNames = { "Tên miền", "Giá", "" }; // Cột thứ ba để chứa checkbox
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Chỉ cho phép chỉnh sửa cột checkbox
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 2 ? Boolean.class : String.class; // Cột checkbox
            }
        };

        domainsTable = new JTable(tableModel);
        domainsTable.setRowHeight(40);
        domainsTable.setShowVerticalLines(false);
        domainsTable.setIntercellSpacing(new Dimension(0, 0));
        domainsTable.getTableHeader().setReorderingAllowed(false);
        domainsTable.getTableHeader().setPreferredSize(new Dimension(100, 40));
        domainsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        domainsTable.getTableHeader().setBackground(new Color(240, 240, 240));

        // Tùy chỉnh renderer cho các cột
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        domainsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // Cột giá

        // Tùy chỉnh cột checkbox
        TableColumn checkboxColumn = domainsTable.getColumnModel().getColumn(2);
        checkboxColumn.setHeaderValue(""); // Không hiển thị tiêu đề cho cột checkbox
        checkboxColumn.setMaxWidth(50); // Giới hạn chiều rộng của cột checkbox

        JScrollPane scrollPane = new JScrollPane(domainsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Panel chứa các nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));

        // Nút thanh toán
        payButton = new JButton("Thanh toán");
        payButton.setBackground(new Color(39, 174, 96));
        payButton.setForeground(Color.BLACK);
        payButton.setFont(new Font("Arial", Font.BOLD, 14));
        payButton.setEnabled(false); // Vô hiệu hóa ban đầu
        payButton.addActionListener(e -> handlePayment());
        buttonPanel.add(payButton);

        // Nút xóa
        deleteButton = new JButton("Xóa");
        deleteButton.setBackground(new Color(231, 76, 60));
        deleteButton.setForeground(Color.BLACK);
        deleteButton.setFont(new Font("Arial", Font.BOLD, 14));
        deleteButton.setEnabled(false); // Vô hiệu hóa ban đầu
        deleteButton.addActionListener(e -> handleDelete());
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Lắng nghe thay đổi trong bảng để kích hoạt nút
        tableModel.addTableModelListener(e -> updateButtonStates());

        // Tải dữ liệu từ SQL khi khởi tạo
        loadDomainsFromDatabase();
    }

    public void loadDomainsFromDatabase() {
        // Xóa dữ liệu cũ trong bảng
        tableModel.setRowCount(0);

        // Kết nối cơ sở dữ liệu và lấy dữ liệu
        String query = "SELECT d.name + d.extension AS domain_name, c.price " +
                "FROM cart c " +
                "JOIN domains d ON c.domain_id = d.id " +
                "WHERE c.user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            // Giả sử bạn có một đối tượng `loggedInUser` chứa thông tin người dùng hiện tại
            statement.setInt(1, getLoggedInUserId());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String domainName = resultSet.getString("domain_name");
                double price = resultSet.getDouble("price");
                tableModel.addRow(new Object[] { domainName, String.format("%,.2f VND", price), false }); // Thêm dữ
                                                                                                          // liệu vào
                                                                                                          // bảng
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu từ cơ sở dữ liệu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Cập nhật trạng thái của các nút thanh toán và xóa
    private void updateButtonStates() {
        // Kiểm tra nếu có ít nhất một hàng được đánh dấu
        boolean hasSelected = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((boolean) tableModel.getValueAt(i, 2)) {
                hasSelected = true;
                break;
            }
        }
        payButton.setEnabled(hasSelected);
        deleteButton.setEnabled(hasSelected);
    }

    /// Xử lý sự kiện thanh toán
    private void handlePayment() {
        // Lấy danh sách tên miền được chọn
        StringBuilder selectedDomainsBuilder = new StringBuilder();
        HashMap<String, Double> domainPrices = new HashMap<>();
        HashMap<String, Integer> domainRentalPeriods = new HashMap<>();
        double totalAmount = 0.0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((boolean) tableModel.getValueAt(i, 2)) {
                String domainName = (String) tableModel.getValueAt(i, 0);
                String priceText = (String) tableModel.getValueAt(i, 1);
                double price = Double.parseDouble(priceText.replace(",", "").replace(" VND", ""));

                // Lấy thông tin rental period ID
                int rentalPeriodId = getSelectedRentalPeriodId();

                selectedDomainsBuilder.append(domainName).append(",");
                domainPrices.put(domainName, price);
                domainRentalPeriods.put(domainName, rentalPeriodId);
                totalAmount += price;
            }
        }

        if (selectedDomainsBuilder.length() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn ít nhất một tên miền để thanh toán.",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Xóa dấu phẩy cuối cùng
        selectedDomainsBuilder.setLength(selectedDomainsBuilder.length() - 1);
        String[] selectedDomains = selectedDomainsBuilder.toString().split(",");

        // Hiển thị dialog thanh toán mới
        PaymentDialog paymentDialog = new PaymentDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                selectedDomains,
                domainPrices,
                domainRentalPeriods,
                this);
        paymentDialog.setVisible(true);
    }

    private void handleDelete() {
        // Lấy danh sách tên miền được chọn để xóa
        StringBuilder selectedDomains = new StringBuilder();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((boolean) tableModel.getValueAt(i, 2)) { // Kiểm tra checkbox
                String domainName = (String) tableModel.getValueAt(i, 0);
                selectedDomains.append(domainName).append(",");
            }
        }

        if (selectedDomains.length() == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một tên miền để xóa.", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Xóa dấu phẩy cuối cùng
        selectedDomains.setLength(selectedDomains.length() - 1);

        // Xác nhận xóa
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa các tên miền đã chọn?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Xóa tên miền khỏi cơ sở dữ liệu
        String[] domainArray = selectedDomains.toString().split(",");
        String query = "DELETE FROM cart WHERE user_id = ? AND domain_id = (SELECT id FROM domains WHERE CONCAT(name, extension) = ?)";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            for (String domainName : domainArray) {
                statement.setInt(1, getLoggedInUserId());
                statement.setString(2, domainName.trim());
                statement.executeUpdate();
            }

            // Xóa các dòng đã chọn khỏi bảng hiển thị
            for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
                if ((boolean) tableModel.getValueAt(i, 2)) {
                    tableModel.removeRow(i);
                }
            }

            JOptionPane.showMessageDialog(this, "Xóa thành công các tên miền đã chọn.", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa dữ liệu: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processPayment(String selectedDomains, double totalPrice) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            // Tách danh sách tên miền thành mảng
            String[] domainArray = selectedDomains.split(",");
            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < domainArray.length; i++) {
                placeholders.append("?,");
            }
            placeholders.setLength(placeholders.length() - 1); // Xóa dấu phẩy cuối cùng

            // Lấy danh sách domain_id và rental_period_id từ bảng cart
            String getDomainIdsSQL = "SELECT d.id, d.name, d.extension, d.price, c.rental_period_id " +
                    "FROM domains d " +
                    "JOIN cart c ON d.id = c.domain_id " +
                    "WHERE CONCAT(d.name, d.extension) IN (" + placeholders + ") " +
                    "AND c.user_id = ?";

            List<Integer> domainIdsList = new ArrayList<>();
            Map<Integer, String> domainNames = new HashMap<>();
            Map<Integer, String> domainExtensions = new HashMap<>();
            Map<Integer, Double> domainPrices = new HashMap<>();
            Map<Integer, Integer> domainRentalPeriodIds = new HashMap<>();

            try (PreparedStatement getDomainIdsStmt = connection.prepareStatement(getDomainIdsSQL)) {
                int paramIndex = 1;
                for (String domain : domainArray) {
                    getDomainIdsStmt.setString(paramIndex++, domain.trim());
                }
                getDomainIdsStmt.setInt(paramIndex, getLoggedInUserId());

                ResultSet rs = getDomainIdsStmt.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    domainIdsList.add(id);
                    domainNames.put(id, rs.getString("name"));
                    domainExtensions.put(id, rs.getString("extension"));
                    domainPrices.put(id, rs.getDouble("price"));
                    domainRentalPeriodIds.put(id, rs.getInt("rental_period_id"));
                }
            }

            if (domainIdsList.isEmpty()) {
                throw new SQLException("Không tìm thấy tên miền trong cơ sở dữ liệu.");
            }

            // Tạo một chuỗi chứa các ID tên miền để sử dụng trong câu SQL
            StringBuilder domainIdsStr = new StringBuilder();
            for (int id : domainIdsList) {
                domainIdsStr.append(id).append(",");
            }
            domainIdsStr.setLength(domainIdsStr.length() - 1); // Xóa dấu phẩy cuối

            // Xóa các mục đã thanh toán khỏi bảng cart
            String deleteCartSQL = "DELETE FROM cart WHERE user_id = ? AND domain_id IN (" + domainIdsStr + ")";
            try (PreparedStatement deleteCartStmt = connection.prepareStatement(deleteCartSQL)) {
                deleteCartStmt.setInt(1, getLoggedInUserId());
                deleteCartStmt.executeUpdate();
            }

            // Tạo một đơn hàng duy nhất cho tất cả domain
            // Sử dụng thông tin của domain đầu tiên cho đơn hàng chính
            int firstDomainId = domainIdsList.get(0);
            int firstRentalPeriodId = domainRentalPeriodIds.get(firstDomainId);

            // Lấy thông tin số tháng cho gói thuê đầu tiên
            int rentalMonths = getRentalMonthsById(firstRentalPeriodId);
            int orderId = 0;

            String createOrderSQL = "INSERT INTO orders (buyer_id, domain_id, rental_period_id, status, created_at, expiry_date, total_price) VALUES (?, ?, ?, ?, GETDATE(), DATEADD(month, ?, GETDATE()), ?)";
            try (PreparedStatement createOrderStmt = connection.prepareStatement(createOrderSQL,
                    Statement.RETURN_GENERATED_KEYS)) {
                createOrderStmt.setInt(1, getLoggedInUserId());
                createOrderStmt.setInt(2, firstDomainId); // Sử dụng domain_id đầu tiên cho đơn hàng chính
                createOrderStmt.setInt(3, firstRentalPeriodId);
                createOrderStmt.setString(4, "Đang xử lý");
                createOrderStmt.setInt(5, rentalMonths);
                createOrderStmt.setDouble(6, totalPrice);
                createOrderStmt.executeUpdate();

                // Lấy ID của đơn hàng vừa tạo
                try (ResultSet generatedKeys = createOrderStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        orderId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Tạo đơn hàng thất bại, không lấy được ID đơn hàng.");
                    }
                }
            }

            // Tạo chi tiết đơn hàng cho từng tên miền với rental period tương ứng
            String insertOrderDetailsSQL = "INSERT INTO order_details (order_id, domain_id, domain_name, domain_extension, price, original_price, rental_period_id, purchase_date, status, expiry_date) VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE(), ?, DATEADD(month, ?, GETDATE()))";
            try (PreparedStatement insertDetailsStmt = connection.prepareStatement(insertOrderDetailsSQL)) {
                for (int domainId : domainIdsList) {
                    int rentalPeriodId = domainRentalPeriodIds.get(domainId);
                    int months = getRentalMonthsById(rentalPeriodId);
                    double discount = getDiscountById(rentalPeriodId);

                    double basePrice = domainPrices.get(domainId);
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

            // Xóa các dòng đã thanh toán khỏi bảng hiển thị
            for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
                if ((boolean) tableModel.getValueAt(i, 2)) {
                    tableModel.removeRow(i);
                }
            }

            JOptionPane.showMessageDialog(this, "Thanh toán thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);

            // Cập nhật OrdersPanel
            updateOrdersPanel();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi xử lý thanh toán: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDomainFromDatabase(String domainName) {
        String query = "DELETE FROM cart WHERE user_id = ? AND domain_id = (SELECT id FROM domains WHERE CONCAT(name, extension) = ?)";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, getLoggedInUserId());
            statement.setString(2, domainName);
            statement.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa dữ liệu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateOrdersPanel() {
        // Lấy OrdersPanel từ CardLayout
        Component[] components = getParent().getComponents();
        OrdersPanel ordersPanel = null;
        for (Component component : components) {
            if (component instanceof OrdersPanel) {
                ordersPanel = (OrdersPanel) component;
                break;
            }
        }

        if (ordersPanel != null) {
            // Sử dụng refresh() method từ OrdersPanel để làm mới dữ liệu
            ordersPanel.refresh();
        }
    }

    private int getLoggedInUserId() {
        // Giả sử bạn có một đối tượng `loggedInUser` chứa thông tin người dùng hiện tại
        return loggedInUser.getId();
    }

    private int getSelectedRentalPeriodId() {
        // Trả về ID của gói thuê dựa trên lựa chọn của người dùng
        return rentalPeriodComboBox.getSelectedIndex() + 1;
    }

    private int getSelectedRentalPeriodMonths() {
        // Trả về số tháng tương ứng với gói thuê
        switch (rentalPeriodComboBox.getSelectedIndex()) {
            case 1:
                return 6; // 6 tháng
            case 2:
                return 12; // 12 tháng
            default:
                return 1; // 1 tháng
        }
    }

    private int getRentalMonthsById(int rentalPeriodId) {
        switch (rentalPeriodId) {
            case 2:
                return 6; // 6 tháng
            case 3:
                return 12; // 12 tháng
            default:
                return 1; // 1 tháng
        }
    }

    private double getDiscountById(int rentalPeriodId) {
        switch (rentalPeriodId) {
            case 2:
                return 0.10; // Giảm 10% cho 6 tháng
            case 3:
                return 0.20; // Giảm 20% cho 12 tháng
            default:
                return 0.0; // Không giảm giá cho 1 tháng
        }
    }
}