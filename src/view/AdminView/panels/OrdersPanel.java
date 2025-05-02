package view.AdminView.panels;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Domain;
import model.Order;
import model.OrderDetails;
import model.User;
import repository.DomainRepository;
import repository.OrderRepository;
import repository.UserRepository;
import service.OrderDetailsService;

public class OrdersPanel extends JPanel {
    private static final Color BG_COLOR = new Color(245, 245, 245);
    private static final Color PRIMARY_COLOR = new Color(0, 102, 102);
    private static final Color ACCENT_COLOR = new Color(255, 153, 0);

    private OrderRepository orderRepository;
    private DomainRepository domainRepository;
    private UserRepository userRepository;
    private OrderDetailsService orderDetailsService;
    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private JFrame parentFrame;
    private JComboBox<String> statusFilter;

    public OrdersPanel(OrderRepository orderRepository, DomainRepository domainRepository,
            UserRepository userRepository, JFrame parentFrame) {
        this.orderRepository = orderRepository;
        this.domainRepository = domainRepository;
        this.userRepository = userRepository;
        this.orderDetailsService = new OrderDetailsService();
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        initComponents();
    }

    private void initComponents() {
        // Panel công cụ
        JPanel toolPanel = new JPanel(new BorderLayout());
        toolPanel.setBackground(Color.WHITE);
        toolPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                new EmptyBorder(10, 20, 10, 20)));

        // Tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);

        JTextField searchField = new JTextField(25);
        searchField.setPreferredSize(new Dimension(300, 35));

        statusFilter = new JComboBox<>(
                new String[] { "Tất cả trạng thái", "Hoàn thành", "Đang xử lý", "Hủy" });
        statusFilter.setPreferredSize(new Dimension(150, 35));

        statusFilter.addActionListener(e -> filterOrders());

        searchPanel.add(searchField);
        searchPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        searchPanel.add(statusFilter);

        // Nút xuất báo cáo
        JButton exportButton = new JButton("Xuất dữ liệu");
        exportButton.setBackground(new Color(52, 152, 219));
        exportButton.setForeground(Color.BLACK);
        exportButton.setFocusPainted(false);
        exportButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportButton.setPreferredSize(new Dimension(120, 35));

        exportButton.addActionListener(e -> exportOrderReport());

        toolPanel.add(searchPanel, BorderLayout.WEST);
        toolPanel.add(exportButton, BorderLayout.EAST);

        // Bảng đơn hàng
        String[] columns = { "ID", "Tên miền", "Người mua", "Ngày", "Giá", "Trạng thái", "Thao tác" };
        tableModel = new DefaultTableModel(columns, 0);

        ordersTable = new JTable(tableModel);
        ordersTable.setRowHeight(40);
        ordersTable.setShowVerticalLines(false);

        // Render cho cột trạng thái
        ordersTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if ("Hoàn thành".equals(value)) {
                    c.setForeground(new Color(39, 174, 96));
                } else if ("Đang xử lý".equals(value)) {
                    c.setForeground(new Color(243, 156, 18));
                } else if ("Hủy".equals(value)) {
                    c.setForeground(new Color(231, 76, 60));
                } else {
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        });

        // Button renderer cho cột thao tác
        ordersTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                actionsPanel.setOpaque(false);

                JButton viewButton = new JButton("Xem");
                viewButton.setPreferredSize(new Dimension(60, 30));
                viewButton.setBackground(new Color(52, 152, 219));
                viewButton.setForeground(Color.BLACK);
                viewButton.setFocusPainted(false);

                JButton updateButton = new JButton("Cập nhật");
                updateButton.setPreferredSize(new Dimension(80, 30));
                updateButton.setBackground(new Color(39, 174, 96));
                updateButton.setForeground(Color.BLACK);
                updateButton.setFocusPainted(false);

                actionsPanel.add(viewButton);
                actionsPanel.add(updateButton);

                return actionsPanel;
            }
        });

        // Mouse listener cho các nút trong bảng
        ordersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = ordersTable.getColumnModel().getColumnIndexAtX(e.getX());
                int row = e.getY() / ordersTable.getRowHeight();

                if (row < ordersTable.getRowCount() && row >= 0 && column == 6) {
                    // Lấy toạ độ của ô được click
                    Rectangle rect = ordersTable.getCellRect(row, column, false);

                    // Tính toán vị trí tương đối trong ô
                    int xInCell = e.getX() - rect.x;

                    String orderId = ((String) ordersTable.getValueAt(row, 0)).substring(1); // Bỏ dấu # đầu tiên
                    int orderIdValue = Integer.parseInt(orderId);

                    // Kiểm tra là record đơn hàng hay chi tiết đơn hàng
                    boolean isOrderDetail = row < tableModel.getRowCount() &&
                            tableModel.getValueAt(row, 0) != null &&
                            tableModel.getValueAt(row, 0).toString().contains("-");

                    // Nếu là chi tiết đơn hàng, lấy ID đơn hàng chính
                    if (isOrderDetail) {
                        String[] parts = orderId.split("-");
                        if (parts.length > 0) {
                            orderIdValue = Integer.parseInt(parts[0]);
                        }
                    }

                    // Nếu click gần phía trái (nút Xem)
                    if (xInCell <= 65) {
                        viewOrderDetail(orderIdValue);
                    }
                    // Nếu click gần phía phải (nút Cập nhật)
                    else if (xInCell > 65) {
                        updateOrderStatus(orderIdValue);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(toolPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Load dữ liệu
        loadOrderData();
    }

    public void loadOrderData() {
        // Xóa dữ liệu cũ
        tableModel.setRowCount(0);

        try {
            List<Order> orders = orderRepository.getAllOrders();
            populateOrderData(orders);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Không thể tải dữ liệu đơn hàng: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);

            // Nếu có lỗi, hiển thị thông báo trong bảng
            tableModel.addRow(new Object[] { "", "", "", "", "", "Không thể tải dữ liệu", "" });
        }
    }

    private void populateOrderData(List<Order> orders) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DecimalFormat priceFormat = new DecimalFormat("#,### VND");

        for (Order order : orders) {
            // Lấy thông tin người dùng
            User user = null;
            try {
                user = userRepository.getUserById(order.getUserId());
            } catch (Exception e) {
                // Bỏ qua lỗi khi lấy thông tin user
            }

            String userName = user != null ? user.getEmail() : "N/A";

            // Xử lý an toàn cho ngày đặt hàng
            String orderDate = "N/A";
            try {
                if (order.getOrderDate() != null) {
                    orderDate = dateFormat.format(order.getOrderDate());
                }
            } catch (Exception e) {
                // Nếu không thể định dạng ngày, giữ giá trị mặc định "N/A"
                System.err.println("Lỗi định dạng ngày đặt hàng: " + e.getMessage());
            }

            // First add the order header row (showing total price)
            tableModel.addRow(new Object[] {
                    "#" + order.getId(),
                    "Đơn hàng #" + order.getId(),
                    userName,
                    orderDate,
                    priceFormat.format(order.getTotalPrice()),
                    order.getStatus(),
                    ""
            });

            // Then add detailed rows for each domain in this order
            List<OrderDetails> orderDetails = orderDetailsService.getOrderDetailsByOrderId(order.getId());
            if (orderDetails != null && !orderDetails.isEmpty()) {
                for (OrderDetails detail : orderDetails) {
                    // Xử lý an toàn cho ngày mua
                    String purchaseDate = "N/A";
                    try {
                        if (detail.getPurchaseDate() != null) {
                            purchaseDate = dateFormat.format(java.sql.Timestamp.valueOf(detail.getPurchaseDate()));
                        }
                    } catch (Exception e) {
                        // Nếu không thể định dạng ngày, giữ giá trị mặc định "N/A"
                        System.err.println("Lỗi định dạng ngày mua: " + e.getMessage());
                    }

                    // Add individual domain details with their own prices
                    tableModel.addRow(new Object[] {
                            "#" + order.getId() + "-" + detail.getId(),
                            detail.getFullDomainName(),
                            "",
                            purchaseDate,
                            priceFormat.format(detail.getPrice()),
                            detail.getStatus(),
                            ""
                    });
                }
            }
        }

        if (orders.isEmpty()) {
            tableModel.addRow(new Object[] { "", "", "", "", "", "Không có đơn hàng", "" });
        }
    }

    private void filterOrders() {
        String selectedStatus = (String) statusFilter.getSelectedItem();

        if ("Tất cả trạng thái".equals(selectedStatus)) {
            loadOrderData();
            return;
        }

        // Xóa dữ liệu cũ
        tableModel.setRowCount(0);

        try {
            List<Order> allOrders = orderRepository.getAllOrders();
            List<Order> filteredOrders = allOrders.stream()
                    .filter(order -> selectedStatus.equals(order.getStatus()))
                    .collect(Collectors.toList());

            populateOrderData(filteredOrders);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Lỗi khi lọc đơn hàng: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportOrderReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn vị trí lưu file");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Đặt tên file mặc định
        fileChooser.setSelectedFile(new java.io.File("orders_report.csv"));

        int userSelection = fileChooser.showSaveDialog(parentFrame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();

            try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(fileToSave), "UTF-8")) {
                // Ghi tiêu đề cột
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    writer.write(tableModel.getColumnName(i) + (i == tableModel.getColumnCount() - 1 ? "\n" : ","));
                }

                // Ghi dữ liệu từng hàng
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Object value = tableModel.getValueAt(i, j);
                        writer.write((value != null ? value.toString() : "") +
                                (j == tableModel.getColumnCount() - 1 ? "\n" : ","));
                    }
                }

                JOptionPane.showMessageDialog(parentFrame,
                        "Xuất dữ liệu thành công!",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parentFrame,
                        "Lỗi khi xuất dữ liệu: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewOrderDetail(int orderId) {
        try {
            Order order = orderRepository.getOrderById(orderId);
            if (order != null) {
                User user = userRepository.getUserById(order.getUserId());
                List<OrderDetails> details = orderDetailsService.getOrderDetailsByOrderId(orderId);

                // Create order details message
                StringBuilder detailsMessage = new StringBuilder();
                detailsMessage.append("Mã đơn hàng: #").append(order.getId()).append("\n");
                detailsMessage.append("Khách hàng: ").append(user != null ? user.getEmail() : "N/A").append("\n");

                // Xử lý an toàn cho ngày đặt hàng
                String orderDateStr = "N/A";
                try {
                    if (order.getOrderDate() != null) {
                        orderDateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(order.getOrderDate());
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi định dạng ngày đặt hàng: " + e.getMessage());
                }
                detailsMessage.append("Ngày đặt: ").append(orderDateStr).append("\n");

                detailsMessage.append("Trạng thái: ").append(order.getStatus()).append("\n\n");

                detailsMessage.append("Chi tiết tên miền:\n");
                if (details != null && !details.isEmpty()) {
                    for (OrderDetails detail : details) {
                        detailsMessage.append("- ").append(detail.getFullDomainName())
                                .append(": ").append(new DecimalFormat("#,### VND").format(detail.getPrice()))
                                .append("\n");
                    }
                } else {
                    detailsMessage.append("- ").append("Không có chi tiết tên miền").append("\n");
                }

                detailsMessage.append("\nTổng tiền: ")
                        .append(new DecimalFormat("#,### VND").format(order.getTotalPrice()));

                JOptionPane.showMessageDialog(parentFrame, detailsMessage.toString(),
                        "Chi tiết đơn hàng #" + order.getId(),
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(parentFrame,
                        "Không tìm thấy thông tin đơn hàng!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Không thể tải thông tin đơn hàng: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateOrderStatus(int orderId) {
        try {
            Order order = orderRepository.getOrderById(orderId);
            if (order != null) {
                String[] statuses = { "Hoàn thành", "Đang xử lý", "Hủy" };

                String selectedStatus = (String) JOptionPane.showInputDialog(parentFrame,
                        "Chọn trạng thái mới cho đơn hàng #" + orderId,
                        "Cập nhật trạng thái",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        statuses,
                        order.getStatus());

                if (selectedStatus != null && !selectedStatus.equals(order.getStatus())) {
                    // Sử dụng phương thức mới updateOrderStatus
                    boolean result = orderRepository.updateOrderStatus(orderId, selectedStatus);

                    if (result) {
                        JOptionPane.showMessageDialog(parentFrame,
                                "Cập nhật trạng thái thành công!",
                                "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                        // Refresh data
                        loadOrderData();
                    } else {
                        JOptionPane.showMessageDialog(parentFrame,
                                "Không thể cập nhật trạng thái!",
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Lỗi khi cập nhật trạng thái: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}