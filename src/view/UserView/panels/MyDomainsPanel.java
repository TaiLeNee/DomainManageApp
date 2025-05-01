package view.UserView.panels;

import java.awt.*;
import java.sql.*;
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

        JLabel label = new JLabel("Tên miền của tôi", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(label, BorderLayout.NORTH);

        // Tạo bảng hiển thị tên miền
        String[] columnNames = {"Tên miền", "Giá", ""}; // Cột thứ ba để chứa checkbox
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
                tableModel.addRow(new Object[]{domainName, String.format("%,.2f VND", price), false}); // Thêm dữ liệu vào bảng
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
        StringBuilder selectedDomains = new StringBuilder();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((boolean) tableModel.getValueAt(i, 2)) {
                String domainName = (String) tableModel.getValueAt(i, 0);
                String priceText = (String) tableModel.getValueAt(i, 1);
                double price = Double.parseDouble(priceText.replace(",", "").replace(" VND", ""));
                selectedDomains.append(domainName).append(",");
                totalPrice += price;
            }
        }
    
        if (selectedDomains.length() == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một tên miền để thanh toán.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        // Xóa dấu phẩy cuối cùng
        selectedDomains.setLength(selectedDomains.length() - 1);
    
        // Tạo cửa sổ JDialog
        JDialog paymentDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thanh toán", true);
        paymentDialog.setSize(500, 600);
        paymentDialog.setLocationRelativeTo(this);
        paymentDialog.setLayout(new BorderLayout());
    
        // Panel hiển thị thông tin tên miền và tổng giá
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
        JTextArea domainInfoArea = new JTextArea(selectedDomains.toString().replace(",", "\n"));
        domainInfoArea.setEditable(false);
        domainInfoArea.setFont(new Font("Arial", Font.PLAIN, 14));
        domainInfoArea.setBorder(BorderFactory.createTitledBorder("Tên miền đã chọn"));
        infoPanel.add(new JScrollPane(domainInfoArea), BorderLayout.CENTER);
    
        JLabel totalLabel = new JLabel("Tổng tiền: " + String.format("%,.0f VND", totalPrice));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        totalLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        infoPanel.add(totalLabel, BorderLayout.SOUTH);
    
        paymentDialog.add(infoPanel, BorderLayout.CENTER);
    
        // Panel chọn thời gian sử dụng
        JPanel rentalPeriodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rentalPeriodPanel.setBorder(BorderFactory.createTitledBorder("Thời gian sử dụng"));
    
        JComboBox<String> rentalPeriodComboBox = new JComboBox<>();
        rentalPeriodComboBox.addItem("1 tháng - Không giảm giá");
        rentalPeriodComboBox.addItem("6 tháng - Giảm 10%");
        rentalPeriodComboBox.addItem("12 tháng - Giảm 20%");
        rentalPeriodPanel.add(rentalPeriodComboBox);
    
        paymentDialog.add(rentalPeriodPanel, BorderLayout.NORTH);
    
        // Lắng nghe thay đổi trong ComboBox để cập nhật tổng tiền
        rentalPeriodComboBox.addActionListener(e -> {
            int selectedIndex = rentalPeriodComboBox.getSelectedIndex();
            double discount = 0.0;
            if (selectedIndex == 1) { // 6 tháng
                discount = 0.10;
            } else if (selectedIndex == 2) { // 12 tháng
                discount = 0.20;
            }
            double discountedPrice = totalPrice * (1 - discount);
            totalLabel.setText("Tổng tiền: " + String.format("%,.0f VND", discountedPrice));
        });
    
        // Panel chọn phương thức thanh toán
        JPanel paymentMethodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        paymentMethodPanel.setBorder(BorderFactory.createTitledBorder("Phương thức thanh toán"));
    
        JRadioButton qrPaymentButton = new JRadioButton("Quét mã QR");
        JRadioButton cardPaymentButton = new JRadioButton("Thẻ");
        ButtonGroup paymentGroup = new ButtonGroup();
        paymentGroup.add(qrPaymentButton);
        paymentGroup.add(cardPaymentButton);
    
        paymentMethodPanel.add(qrPaymentButton);
        paymentMethodPanel.add(cardPaymentButton);
    
        // Panel chứa nút Xác nhận và Hủy
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    
        JButton confirmButton = new JButton("Xác nhận");
        confirmButton.setBackground(new Color(39, 174, 96));
        confirmButton.setForeground(Color.BLACK);
        confirmButton.setFont(new Font("Arial", Font.BOLD, 14));
        confirmButton.addActionListener(e -> {
            if (!qrPaymentButton.isSelected() && !cardPaymentButton.isSelected()) {
                JOptionPane.showMessageDialog(paymentDialog, "Vui lòng chọn phương thức thanh toán.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
    
            int selectedIndex = rentalPeriodComboBox.getSelectedIndex();
            double discount = 0.0;
            if (selectedIndex == 1) { // 6 tháng
                discount = 0.10;
            } else if (selectedIndex == 2) { // 12 tháng
                discount = 0.20;
            }
            double finalPrice = totalPrice * (1 - discount);
    
            if (qrPaymentButton.isSelected()) {
                showQRCodeDialog();
            } else {
                showCardPaymentDialog(finalPrice);
            }
    
            // Xử lý thanh toán và cập nhật cơ sở dữ liệu
            processPayment(selectedDomains.toString(), finalPrice);
    
            paymentDialog.dispose();
        });
    
        JButton cancelButton = new JButton("Hủy");
        cancelButton.setBackground(new Color(231, 76, 60));
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.addActionListener(e -> paymentDialog.dispose());
    
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
    
        // Tạo panel chứa cả phương thức thanh toán và các nút
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(paymentMethodPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
    
        paymentDialog.add(southPanel, BorderLayout.SOUTH);
    
        // Hiển thị cửa sổ JDialog
        paymentDialog.setVisible(true);
    }
    
    // Phương thức hiển thị JDialog mã QR
    private void showQRCodeDialog() {
        JDialog qrDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Quét mã QR", true);
        qrDialog.setSize(400, 400);
        qrDialog.setLocationRelativeTo(this);
        qrDialog.setLayout(new BorderLayout());
    
        // Hiển thị mã QR từ tệp ảnh
        JLabel qrLabel = new JLabel("", SwingConstants.CENTER);
        qrLabel.setBorder(BorderFactory.createTitledBorder("Mã QR"));
        qrLabel.setIcon(new ImageIcon("src\\img\\qrcode-default.png")); // Đường dẫn đến tệp ảnh mã QR
        qrDialog.add(qrLabel, BorderLayout.CENTER);
    
        // Nút xác nhận
        JButton confirmButton = new JButton("Xác nhận");
        confirmButton.setBackground(new Color(39, 174, 96));
        confirmButton.setForeground(Color.BLACK);
        confirmButton.setFont(new Font("Arial", Font.BOLD, 14));
        confirmButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(qrDialog, "Thanh toán thành công bằng Quét mã QR!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            qrDialog.dispose();
        });
    
        // Nút hủy
        JButton cancelButton = new JButton("Hủy");
        cancelButton.setBackground(new Color(231, 76, 60));
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.addActionListener(e -> qrDialog.dispose()); // Chỉ đóng qrDialog
    
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
    
        qrDialog.add(buttonPanel, BorderLayout.SOUTH);
    
        qrDialog.setVisible(true);
    }

    // Phương thức hiển thị JDialog thanh toán bằng thẻ
    private void showCardPaymentDialog(double totalPrice) {
        JDialog cardDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thanh toán bằng thẻ", true);
        cardDialog.setSize(400, 300);
        cardDialog.setLocationRelativeTo(this);
        cardDialog.setLayout(new BorderLayout());
    
        // Panel chứa các trường thông tin thẻ
        JPanel cardInfoPanel = new JPanel(new GridBagLayout());
        cardInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
    
        // Số thẻ
        gbc.gridx = 0;
        gbc.gridy = 0;
        cardInfoPanel.add(new JLabel("Số thẻ:"), gbc);
    
        gbc.gridx = 1;
        JTextField cardNumberField = new JTextField(20);
        cardInfoPanel.add(cardNumberField, gbc);
    
        // Tên chủ thẻ
        gbc.gridx = 0;
        gbc.gridy = 1;
        cardInfoPanel.add(new JLabel("Tên chủ thẻ:"), gbc);
    
        gbc.gridx = 1;
        JTextField cardHolderField = new JTextField(20);
        cardInfoPanel.add(cardHolderField, gbc);
    
        // Ngày hết hạn
        gbc.gridx = 0;
        gbc.gridy = 2;
        cardInfoPanel.add(new JLabel("Ngày hết hạn (MM/YY):"), gbc);
    
        gbc.gridx = 1;
        JTextField expiryDateField = new JTextField(10);
        cardInfoPanel.add(expiryDateField, gbc);
    
        // Mã CVV
        gbc.gridx = 0;
        gbc.gridy = 3;
        cardInfoPanel.add(new JLabel("Mã CVV:"), gbc);
    
        gbc.gridx = 1;
        JTextField cvvField = new JTextField(5);
        cardInfoPanel.add(cvvField, gbc);
    
        cardDialog.add(cardInfoPanel, BorderLayout.CENTER);
    
        // Nút xác nhận
        JButton confirmButton = new JButton("Xác nhận");
        confirmButton.setBackground(new Color(39, 174, 96));
        confirmButton.setForeground(Color.BLACK);
        confirmButton.setFont(new Font("Arial", Font.BOLD, 14));
        confirmButton.addActionListener(e -> {
            String cardNumber = cardNumberField.getText().trim();
            String cardHolder = cardHolderField.getText().trim();
            String expiryDate = expiryDateField.getText().trim();
            String cvv = cvvField.getText().trim();
    
            // Kiểm tra thông tin thẻ
            if (cardNumber.isEmpty() || cardHolder.isEmpty() || expiryDate.isEmpty() || cvv.isEmpty()) {
                JOptionPane.showMessageDialog(cardDialog, "Vui lòng nhập đầy đủ thông tin thẻ.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
    
            if (!ValidationUtils.isValidCardNumber(cardNumber)) {
                JOptionPane.showMessageDialog(cardDialog, "Số thẻ không hợp lệ.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
    
            if (!ValidationUtils.isValidExpiryDate(expiryDate)) {
                JOptionPane.showMessageDialog(cardDialog, "Ngày hết hạn không hợp lệ. Định dạng phải là MM/YY.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
    
            if (!ValidationUtils.isValidCVV(cvv)) {
                JOptionPane.showMessageDialog(cardDialog, "Mã CVV không hợp lệ.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
    
            JOptionPane.showMessageDialog(cardDialog, "Thanh toán thành công bằng thẻ!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            cardDialog.dispose();
        });
    
        // Nút hủy
        JButton cancelButton = new JButton("Hủy");
        cancelButton.setBackground(new Color(231, 76, 60));
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.addActionListener(e -> cardDialog.dispose()); // Chỉ đóng cardDialog
    
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
    
        cardDialog.add(buttonPanel, BorderLayout.SOUTH);
    
        cardDialog.setVisible(true);
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
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một tên miền để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        // Xóa dấu phẩy cuối cùng
        selectedDomains.setLength(selectedDomains.length() - 1);
    
        // Xác nhận xóa
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa các tên miền đã chọn?", "Xác nhận", JOptionPane.YES_NO_OPTION);
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
    
            JOptionPane.showMessageDialog(this, "Xóa thành công các tên miền đã chọn.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
    
            // Lấy danh sách domain_id từ bảng domains
            String getDomainIdsSQL = "SELECT id FROM domains WHERE CONCAT(name, extension) IN (" + placeholders + ")";
            StringBuilder domainIds = new StringBuilder();
            try (PreparedStatement getDomainIdsStmt = connection.prepareStatement(getDomainIdsSQL)) {
                for (int i = 0; i < domainArray.length; i++) {
                    getDomainIdsStmt.setString(i + 1, domainArray[i].trim());
                }
                ResultSet rs = getDomainIdsStmt.executeQuery();
                while (rs.next()) {
                    domainIds.append(rs.getInt("id")).append(",");
                }
            }
            if (domainIds.length() == 0) {
                throw new SQLException("Không tìm thấy tên miền trong cơ sở dữ liệu.");
            }
            // Xóa dấu phẩy cuối cùng
            domainIds.setLength(domainIds.length() - 1);
    
            // Xóa các mục đã thanh toán khỏi bảng cart
            String deleteCartSQL = "DELETE FROM cart WHERE user_id = ? AND domain_id IN (" + domainIds + ")";
            try (PreparedStatement deleteCartStmt = connection.prepareStatement(deleteCartSQL)) {
                deleteCartStmt.setInt(1, getLoggedInUserId());
                deleteCartStmt.executeUpdate();
            }
    
            // Thêm thông tin vào bảng orders
            String insertOrderSQL = "INSERT INTO orders (buyer_id, domain_id, rental_period_id, status, created_at, expiry_date, total_price) VALUES (?, ?, ?, ?, GETDATE(), DATEADD(month, ?, GETDATE()), ?)";
            try (PreparedStatement insertOrderStmt = connection.prepareStatement(insertOrderSQL)) {
                for (String domainId : domainIds.toString().split(",")) {
                    try {
                        int parsedDomainId = Integer.parseInt(domainId.trim());
                        insertOrderStmt.setInt(1, getLoggedInUserId());
                        insertOrderStmt.setInt(2, parsedDomainId);
                        insertOrderStmt.setInt(3, getSelectedRentalPeriodId());
                        insertOrderStmt.setString(4, "Đang xử lý");
                        insertOrderStmt.setInt(5, getSelectedRentalPeriodMonths());
                        insertOrderStmt.setDouble(6, totalPrice);
                        insertOrderStmt.addBatch();
                    } catch (NumberFormatException e) {
                        throw new SQLException("Lỗi định dạng domain_id: " + domainId, e);
                    }
                }
                insertOrderStmt.executeBatch();
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
            JOptionPane.showMessageDialog(this, "Lỗi khi xử lý thanh toán: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Sử dụng JOIN để lấy thông tin tên miền từ bảng domains
            String query = "SELECT d.name + d.extension AS domain_name, o.total_price, o.created_at, o.status " +
                           "FROM orders o " +
                           "JOIN domains d ON o.domain_id = d.id " +
                           "WHERE o.buyer_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, getLoggedInUserId()); // Lấy ID người dùng hiện tại
                ResultSet rs = stmt.executeQuery();
    
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
                    ordersPanel.clearTable(); // Xóa dữ liệu cũ trước khi thêm dữ liệu mới
                    while (rs.next()) {
                        String domainName = rs.getString("domain_name");
                        double totalPrice = rs.getDouble("total_price");
                        Timestamp paymentDate = rs.getTimestamp("created_at");
                        String status = rs.getString("status");
    
                        // Thêm dữ liệu vào OrdersPanel
                        ordersPanel.addOrder(domainName, totalPrice, paymentDate, status);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải thông tin đơn hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
            case 1: return 6; // 6 tháng
            case 2: return 12; // 12 tháng
            default: return 1; // 1 tháng
        }
    }
}