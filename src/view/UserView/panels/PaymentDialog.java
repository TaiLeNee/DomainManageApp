package view.UserView.panels;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import model.*;
import repository.*;
import utils.ValidationUtils;
import utils.UserSession;

public class PaymentDialog extends JDialog {
    private User loggedInUser;
    private String[] selectedDomains;
    private HashMap<String, Double> domainPrices;
    private HashMap<String, Integer> domainRentalPeriods;
    private double totalPrice;
    private MyDomainsPanel myDomainsPanel;
    private RentalPeriodRepository rentalPeriodRepository;

    // UI Constants
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color PRIMARY_DARK_COLOR = new Color(0, 77, 153);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(222, 226, 230);
    private static final Color TEXT_COLOR = new Color(33, 37, 41);
    private static final Color TEXT_MUTED = new Color(108, 117, 125);

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font HEADING_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font VALUE_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font BTN_FONT = new Font("Segoe UI", Font.BOLD, 14);

    private DecimalFormat currencyFormat = new DecimalFormat("#,### VND");

    public PaymentDialog(Frame owner, String[] selectedDomains,
            HashMap<String, Double> domainPrices,
            HashMap<String, Integer> domainRentalPeriods,
            MyDomainsPanel myDomainsPanel) {
        super(owner, "Thanh toán", true);
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
        setSize(700, 700);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initComponents() {
        // Thiết lập layout tổng thể
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Panel chứa nội dung chính
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Thêm tiêu đề
        JPanel headerPanel = createHeaderPanel();
        contentPanel.add(headerPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Thêm panel thông tin tên miền
        JPanel domainInfoPanel = createDomainInfoPanel();
        contentPanel.add(domainInfoPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Thêm panel tổng hóa đơn
        JPanel totalPanel = createTotalPanel();
        contentPanel.add(totalPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Thêm panel phương thức thanh toán
        JPanel paymentMethodPanel = createPaymentMethodPanel();
        contentPanel.add(paymentMethodPanel);

        // Panel các nút thao tác
        JPanel buttonPanel = createButtonPanel();

        // Thêm các panel vào dialog
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("Xác nhận thanh toán");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);

        JLabel subtitleLabel = new JLabel("Vui lòng kiểm tra thông tin trước khi thanh toán");
        subtitleLabel.setFont(LABEL_FONT);
        subtitleLabel.setForeground(TEXT_MUTED);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(subtitleLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDomainInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1), // Thay đổi màu viền thành màu xanh (PRIMARY_COLOR)
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        // Panel tiêu đề
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Thông tin tên miền");
        titleLabel.setFont(HEADING_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);

        titlePanel.add(titleLabel, BorderLayout.WEST);

        // Tạo bảng hiển thị thông tin tên miền
        String[] columnNames = { "Tên miền", "Thời hạn", "Giá" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Thêm dữ liệu vào bảng
        for (String domain : selectedDomains) {
            double price = domainPrices.getOrDefault(domain, 0.0);
            int rentalPeriodId = domainRentalPeriods.getOrDefault(domain, 1); // Mặc định là 1 tháng
            String rentalPeriodInfo = getRentalPeriodInfo(rentalPeriodId);

            tableModel.addRow(new Object[] {
                    domain,
                    rentalPeriodInfo,
                    currencyFormat.format(price)
            });
        }

        JTable domainTable = new JTable(tableModel);
        domainTable.setRowHeight(40);
        domainTable.setFont(LABEL_FONT);
        domainTable.setShowGrid(true);
        domainTable.setGridColor(BORDER_COLOR);
        domainTable.setRowSelectionAllowed(false);
        domainTable.setFillsViewportHeight(true);
        domainTable.getTableHeader().setFont(VALUE_FONT);
        domainTable.getTableHeader().setBackground(PRIMARY_COLOR);
        domainTable.getTableHeader().setForeground(Color.BLACK); // Chuyển màu chữ tiêu đề cột sang đen
        domainTable.getTableHeader().setPreferredSize(new Dimension(100, 40));

        // Căn chỉnh cột
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

        // Điều chỉnh tỷ lệ độ rộng các cột theo phần trăm
        domainTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        int totalWidth = panel.getWidth() - 30; // Trừ đi padding của panel

        // Thiết lập tỷ lệ phần trăm cho từng cột
        TableColumnModel columnModel = domainTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(60); // Tên miền: 60%
        columnModel.getColumn(1).setPreferredWidth(20); // Thời hạn: 20%
        columnModel.getColumn(2).setPreferredWidth(20); // Giá: 20%

        // Áp dụng renderer cho căn chỉnh
        columnModel.getColumn(1).setCellRenderer(centerRenderer);
        columnModel.getColumn(2).setCellRenderer(rightRenderer);

        // Set màu nền cho các hàng xen kẽ
        domainTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (row % 2 == 0) {
                    comp.setBackground(new Color(240, 240, 250));
                } else {
                    comp.setBackground(Color.WHITE);
                }

                if (column == 2) { // Cột giá
                    setHorizontalAlignment(JLabel.RIGHT);
                } else if (column == 1) { // Cột thời hạn
                    setHorizontalAlignment(JLabel.CENTER);
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                }

                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return comp;
            }
        });

        // Tạo ScrollPane cho bảng và đảm bảo nó lấp đầy không gian
        JScrollPane tableScrollPane = new JScrollPane(domainTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.setPreferredSize(new Dimension(totalWidth, 150));

        // Thêm các thành phần vào panel
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);

        // Thêm một ComponentListener để điều chỉnh kích thước cột khi panel thay đổi
        // kích thước
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Cập nhật lại kích thước cột khi panel thay đổi kích thước
                int panelWidth = panel.getWidth() - 30; // Trừ đi padding
                TableColumnModel tcm = domainTable.getColumnModel();

                // Áp dụng tỷ lệ phần trăm cho từng cột
                tcm.getColumn(0).setPreferredWidth((int) (panelWidth * 0.6)); // 60%
                tcm.getColumn(1).setPreferredWidth((int) (panelWidth * 0.2)); // 20%
                tcm.getColumn(2).setPreferredWidth((int) (panelWidth * 0.2)); // 20%
            }
        });

        return panel;
    }

    private JPanel createTotalPanel() {
        // Thay đổi JPanel thành FlowLayout với căn giữa
        JPanel outerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        outerPanel.setOpaque(false);

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(500, 160)); // Thiết lập kích thước cố định cho panel bên trong
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Tiêu đề
        JLabel titleLabel = new JLabel("Tổng hóa đơn");
        titleLabel.setFont(HEADING_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Tạo panel tổng tiền
        JPanel summaryPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        summaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Số lượng tên miền
        JLabel domainCountLabel = new JLabel("Số lượng tên miền:");
        domainCountLabel.setFont(LABEL_FONT);
        JLabel domainCountValue = new JLabel(selectedDomains.length + " tên miền");
        domainCountValue.setFont(VALUE_FONT);
        domainCountValue.setHorizontalAlignment(JLabel.RIGHT);

        // Phương thức thanh toán
        JLabel paymentMethodLabel = new JLabel("Phương thức thanh toán:");
        paymentMethodLabel.setFont(LABEL_FONT);
        JLabel paymentMethodValue = new JLabel("Thanh toán qua mã QR");
        paymentMethodValue.setFont(VALUE_FONT);
        paymentMethodValue.setHorizontalAlignment(JLabel.RIGHT);

        // Tổng tiền
        JLabel totalPriceLabel = new JLabel("Tổng thanh toán:");
        totalPriceLabel.setFont(VALUE_FONT);
        totalPriceLabel.setForeground(PRIMARY_DARK_COLOR);
        JLabel totalPriceValue = new JLabel(currencyFormat.format(totalPrice));
        totalPriceValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalPriceValue.setForeground(PRIMARY_DARK_COLOR);
        totalPriceValue.setHorizontalAlignment(JLabel.RIGHT);

        // Thêm các thành phần vào panel tổng tiền
        summaryPanel.add(domainCountLabel);
        summaryPanel.add(domainCountValue);
        summaryPanel.add(paymentMethodLabel);
        summaryPanel.add(paymentMethodValue);
        summaryPanel.add(totalPriceLabel);
        summaryPanel.add(totalPriceValue);

        // Thêm các thành phần vào panel
        panel.add(titleLabel);
        panel.add(summaryPanel);

        // Thêm panel vào panel ngoài căn giữa
        outerPanel.add(panel);

        return outerPanel;
    }

    private JPanel createPaymentMethodPanel() {
        // Thay đổi JPanel thành FlowLayout với căn giữa
        JPanel outerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        outerPanel.setOpaque(false);

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(500, 400)); // Thiết lập kích thước cố định cho panel bên trong
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Tiêu đề
        JLabel titleLabel = new JLabel("Phương thức thanh toán");
        titleLabel.setFont(HEADING_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Panel mã QR
        JPanel qrPanel = new JPanel();
        qrPanel.setLayout(new BoxLayout(qrPanel, BoxLayout.Y_AXIS));
        qrPanel.setOpaque(false);
        qrPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        qrPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Thông tin QR
        JLabel qrInfoLabel = new JLabel("Quét mã QR bằng ứng dụng ngân hàng hoặc ví điện tử để thanh toán");
        qrInfoLabel.setFont(LABEL_FONT);
        qrInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tạo ảnh QR code
        ImageIcon qrIcon = new ImageIcon("src/img/qrcode-default.png");
        Image qrImage = qrIcon.getImage();
        int qrSize = 200;
        Image scaledQR = qrImage.getScaledInstance(qrSize, qrSize, Image.SCALE_SMOOTH);

        JLabel qrLabel = new JLabel(new ImageIcon(scaledQR));
        qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Hướng dẫn
        JPanel instructionPanel = new JPanel();
        instructionPanel.setLayout(new BoxLayout(instructionPanel, BoxLayout.Y_AXIS));
        instructionPanel.setOpaque(false);
        instructionPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        instructionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel step1 = new JLabel("1. Mở ứng dụng ngân hàng hoặc ví điện tử của bạn");
        step1.setFont(SMALL_FONT);
        step1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel step2 = new JLabel("2. Quét mã QR bên trên");
        step2.setFont(SMALL_FONT);
        step2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel step3 = new JLabel("3. Xác nhận thanh toán với số tiền: " + currencyFormat.format(totalPrice));
        step3.setFont(SMALL_FONT);
        step3.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel step4 = new JLabel("4. Nhấn 'Xác nhận' sau khi hoàn tất thanh toán");
        step4.setFont(SMALL_FONT);
        step4.setAlignmentX(Component.CENTER_ALIGNMENT);

        instructionPanel.add(step1);
        instructionPanel.add(Box.createVerticalStrut(5));
        instructionPanel.add(step2);
        instructionPanel.add(Box.createVerticalStrut(5));
        instructionPanel.add(step3);
        instructionPanel.add(Box.createVerticalStrut(5));
        instructionPanel.add(step4);

        // Thêm các thành phần vào panel QR
        qrPanel.add(qrInfoLabel);
        qrPanel.add(Box.createVerticalStrut(20));

        JPanel qrCenterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        qrCenterPanel.setOpaque(false);
        qrCenterPanel.add(qrLabel);
        qrPanel.add(qrCenterPanel);

        qrPanel.add(Box.createVerticalStrut(10));
        qrPanel.add(instructionPanel);

        // Thêm các thành phần vào panel chính
        panel.add(titleLabel);
        panel.add(qrPanel);

        // Thêm panel vào panel ngoài căn giữa
        outerPanel.add(panel);

        return outerPanel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        // Nút hủy
        JButton cancelButton = new JButton("Hủy");
        cancelButton.setFont(BTN_FONT);
        cancelButton.setForeground(DANGER_COLOR);
        cancelButton.setBackground(Color.WHITE);
        cancelButton.setBorder(BorderFactory.createLineBorder(DANGER_COLOR, 2, true));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.setFocusPainted(false);
        cancelButton.setPreferredSize(new Dimension(120, 40));

        // Nút xác nhận
        JButton confirmButton = new JButton("Xác nhận");
        confirmButton.setFont(BTN_FONT);
        confirmButton.setForeground(Color.BLACK); // Chuyển màu chữ từ trắng sang đen
        confirmButton.setBackground(SUCCESS_COLOR);
        confirmButton.setBorder(BorderFactory.createEmptyBorder());
        confirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmButton.setFocusPainted(false);
        confirmButton.setPreferredSize(new Dimension(120, 40));

        // Thêm sự kiện cho nút
        cancelButton.addActionListener(e -> dispose());

        confirmButton.addActionListener(e -> {
            if (processPayment()) {
                JOptionPane.showMessageDialog(this,
                        "Thanh toán thành công!",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();

                // Refresh the domain panel
                myDomainsPanel.loadDomainsFromDatabase();
            }
        });

        panel.add(cancelButton);
        panel.add(confirmButton);

        return panel;
    }

    private boolean processPayment() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            Map<Integer, Double> domainIdPrices = new HashMap<>();
            Map<Integer, Integer> domainIdRentalPeriods = new HashMap<>();
            Map<Integer, String> domainNames = new HashMap<>();
            Map<Integer, String> domainExtensions = new HashMap<>();

            // Lấy domain ID và các thông tin liên quan từ tên miền được chọn
            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < selectedDomains.length; i++) {
                placeholders.append("?,");
            }
            placeholders.setLength(placeholders.length() - 1); // Xóa dấu phẩy cuối cùng

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
                throw new SQLException("Không tìm thấy tên miền trong cơ sở dữ liệu.");
            }

            // Xóa các mục đã thanh toán khỏi bảng cart
            StringBuilder domainIdsStr = new StringBuilder();
            for (int id : domainIdPrices.keySet()) {
                domainIdsStr.append(id).append(",");
            }
            domainIdsStr.setLength(domainIdsStr.length() - 1); // Xóa dấu phẩy cuối

            String deleteCartSQL = "DELETE FROM cart WHERE user_id = ? AND domain_id IN (" + domainIdsStr + ")";
            try (PreparedStatement deleteCartStmt = connection.prepareStatement(deleteCartSQL)) {
                deleteCartStmt.setInt(1, loggedInUser.getId());
                deleteCartStmt.executeUpdate();
            }

            // Tạo một đơn hàng duy nhất cho tất cả domain
            // Sử dụng thông tin của domain đầu tiên cho đơn hàng chính
            int firstDomainId = domainIdPrices.keySet().iterator().next();
            int firstRentalPeriodId = domainIdRentalPeriods.get(firstDomainId);

            // Lấy thông tin số tháng cho gói thuê đầu tiên
            int rentalMonths = getRentalMonthsFromId(firstRentalPeriodId);

            String createOrderSQL = "INSERT INTO orders (buyer_id, domain_id, rental_period_id, status, created_at, expiry_date, total_price) VALUES (?, ?, ?, ?, GETDATE(), DATEADD(month, ?, GETDATE()), ?)";
            int orderId = 0;

            try (PreparedStatement createOrderStmt = connection.prepareStatement(createOrderSQL,
                    Statement.RETURN_GENERATED_KEYS)) {
                createOrderStmt.setInt(1, loggedInUser.getId());
                createOrderStmt.setInt(2, firstDomainId);
                createOrderStmt.setInt(3, firstRentalPeriodId);
                createOrderStmt.setString(4, "Đang xử lý");
                createOrderStmt.setInt(5, rentalMonths);
                createOrderStmt.setDouble(6, totalPrice);
                createOrderStmt.executeUpdate();

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
        return 1; // Mặc định 1 tháng nếu không tìm thấy
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
        return 0.0; // Mặc định không giảm giá nếu không tìm thấy
    }
}