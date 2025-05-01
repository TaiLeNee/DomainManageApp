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
import model.User;
import repository.DomainRepository;
import repository.OrderRepository;
import repository.UserRepository;

public class OrdersPanel extends JPanel {
    private static final Color BG_COLOR = new Color(245, 245, 245);
    private static final Color PRIMARY_COLOR = new Color(0, 102, 102);
    private static final Color ACCENT_COLOR = new Color(255, 153, 0);

    private OrderRepository orderRepository;
    private DomainRepository domainRepository;
    private UserRepository userRepository;
    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private JFrame parentFrame;
    private JComboBox<String> statusFilter;

    public OrdersPanel(OrderRepository orderRepository, DomainRepository domainRepository,
            UserRepository userRepository, JFrame parentFrame) {
        this.orderRepository = orderRepository;
        this.domainRepository = domainRepository;
        this.userRepository = userRepository;
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

                    // Nếu click gần phía trái (nút Xem)
                    if (xInCell <= 65) {
                        viewOrderDetail(Integer.parseInt(orderId));
                    }
                    // Nếu click gần phía phải (nút Cập nhật)
                    else if (xInCell > 65) {
                        updateOrderStatus(Integer.parseInt(orderId));
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
            // Lấy thông tin tên miền và người dùng
            Domain domain = null;
            User user = null;
            try {
                domain = domainRepository.getDomainById(order.getDomainId());
                user = userRepository.getUserById(order.getUserId());
            } catch (Exception e) {
                // Bỏ qua lỗi khi lấy thông tin domain hoặc user
            }

            String domainName = domain != null ? domain.getName() + domain.getExtension() : "N/A";
            String userName = user != null ? user.getEmail() : "N/A";
            String orderDate = dateFormat.format(order.getOrderDate());
            String price = priceFormat.format(order.getTotalPrice());

            tableModel.addRow(new Object[] {
                    "#" + order.getId(),
                    domainName,
                    userName,
                    orderDate,
                    price,
                    order.getStatus(),
                    ""
            });
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
                        writer.write(tableModel.getValueAt(i, j) + (j == tableModel.getColumnCount() - 1 ? "\n" : ","));
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
                Domain domain = domainRepository.getDomainById(order.getDomainId());
                User user = userRepository.getUserById(order.getUserId());

                // Tạo thông báo chi tiết đơn hàng
                StringBuilder details = new StringBuilder();
                details.append("Mã đơn hàng: #").append(order.getId()).append("\n");
                details.append("Tên miền: ").append(domain != null ? domain.getName() + domain.getExtension() : "N/A")
                        .append("\n");
                details.append("Khách hàng: ").append(user != null ? user.getEmail() : "N/A").append("\n");
                details.append("Ngày đặt: ")
                        .append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(order.getOrderDate())).append("\n");
                details.append("Tổng tiền: ").append(new DecimalFormat("#,### VND").format(order.getTotalPrice()))
                        .append("\n");
                details.append("Trạng thái: ").append(order.getStatus()).append("\n");

                JOptionPane.showMessageDialog(parentFrame, details.toString(),
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
                    order.setStatus(selectedStatus);
                    boolean result = orderRepository.updateOrder(order);

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