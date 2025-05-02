package view.AdminView.panels;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Domain;
import model.Order;
import model.OrderDetails;
import model.User;
import repository.DomainRepository;
import repository.OrderDetailsRepository;
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
    private OrderDetailsRepository orderDetailsRepository;

    private JTable ordersTable;
    private JTable detailsTable;
    private DefaultTableModel ordersTableModel;
    private DefaultTableModel detailsTableModel;
    private JFrame parentFrame;
    private JComboBox<String> statusFilter;
    private JButton updateStatusButton;

    // Để theo dõi đơn hàng và chi tiết đơn hàng hiện tại được chọn
    private int selectedOrderId = -1;
    private int selectedOrderDetailId = -1;

    public OrdersPanel(OrderRepository orderRepository, DomainRepository domainRepository,
            UserRepository userRepository, JFrame parentFrame) {
        this.orderRepository = orderRepository;
        this.domainRepository = domainRepository;
        this.userRepository = userRepository;
        this.orderDetailsService = new OrderDetailsService();
        this.orderDetailsRepository = new OrderDetailsRepository();
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

        // Nút xuất báo cáo và cập nhật trạng thái
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        updateStatusButton = new JButton("Cập nhật trạng thái");
        updateStatusButton.setBackground(new Color(39, 174, 96));
        updateStatusButton.setForeground(Color.BLACK);
        updateStatusButton.setFocusPainted(false);
        updateStatusButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateStatusButton.setPreferredSize(new Dimension(150, 35));
        updateStatusButton.setEnabled(false); // Mặc định không kích hoạt cho đến khi chọn một hàng

        JButton exportButton = new JButton("Xuất dữ liệu");
        exportButton.setBackground(new Color(52, 152, 219));
        exportButton.setForeground(Color.BLACK);
        exportButton.setFocusPainted(false);
        exportButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportButton.setPreferredSize(new Dimension(120, 35));

        updateStatusButton.addActionListener(e -> updateSelectedStatus());
        exportButton.addActionListener(e -> exportOrderReport());

        buttonPanel.add(updateStatusButton);
        buttonPanel.add(exportButton);

        toolPanel.add(searchPanel, BorderLayout.WEST);
        toolPanel.add(buttonPanel, BorderLayout.EAST);

        // Tạo JSplitPane để chứa hai bảng
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5); // Chia đều không gian
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);

        // Bảng đơn hàng
        JPanel ordersPanel = new JPanel(new BorderLayout());
        ordersPanel.setBackground(Color.WHITE);
        ordersPanel.setBorder(BorderFactory.createTitledBorder("Danh sách đơn hàng"));

        String[] orderColumns = { "ID", "Người mua", "Email", "Ngày đặt", "Tổng tiền", "Trạng thái" };
        ordersTableModel = new DefaultTableModel(orderColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ordersTable = new JTable(ordersTableModel);
        ordersTable.setRowHeight(40);
        ordersTable.setShowVerticalLines(false);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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

        JScrollPane ordersScrollPane = new JScrollPane(ordersTable);
        ordersScrollPane.setBorder(BorderFactory.createEmptyBorder());
        ordersPanel.add(ordersScrollPane, BorderLayout.CENTER);

        // Bảng chi tiết đơn hàng (tên miền)
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Chi tiết đơn hàng - Tên miền"));

        String[] detailColumns = { "ID", "Tên miền", "Giá", "Ngày mua", "Trạng thái" };
        detailsTableModel = new DefaultTableModel(detailColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        detailsTable = new JTable(detailsTableModel);
        detailsTable.setRowHeight(40);
        detailsTable.setShowVerticalLines(false);
        detailsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Render cho cột trạng thái của chi tiết
        detailsTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
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

        JScrollPane detailsScrollPane = new JScrollPane(detailsTable);
        detailsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        detailsPanel.add(detailsScrollPane, BorderLayout.CENTER);

        // Thêm SelectionListener cho bảng đơn hàng
        ordersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = ordersTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        String orderId = ((String) ordersTable.getValueAt(selectedRow, 0)).substring(1); // Bỏ dấu # đầu
                                                                                                         // tiên
                        selectedOrderId = Integer.parseInt(orderId);
                        loadOrderDetails(selectedOrderId);
                        updateStatusButton.setEnabled(true);
                    } else {
                        selectedOrderId = -1;
                        clearDetailsTable();
                        updateStatusButton.setEnabled(false);
                    }
                }
            }
        });

        // Thêm SelectionListener cho bảng chi tiết
        detailsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = detailsTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        String detailId = ((String) detailsTable.getValueAt(selectedRow, 0)).substring(1); // Bỏ dấu #
                                                                                                           // đầu tiên
                        selectedOrderDetailId = Integer.parseInt(detailId);
                        updateStatusButton.setEnabled(true);
                    } else {
                        selectedOrderDetailId = -1;
                        if (selectedOrderId == -1) {
                            updateStatusButton.setEnabled(false);
                        }
                    }
                }
            }
        });

        splitPane.setTopComponent(ordersPanel);
        splitPane.setBottomComponent(detailsPanel);

        add(toolPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // Load dữ liệu đơn hàng
        loadOrderData();
    }

    public void loadOrderData() {
        // Xóa dữ liệu cũ
        ordersTableModel.setRowCount(0);

        try {
            List<Order> orders = orderRepository.getAllOrders();
            populateOrdersTable(orders);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Không thể tải dữ liệu đơn hàng: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);

            // Nếu có lỗi, hiển thị thông báo trong bảng
            ordersTableModel.addRow(new Object[] { "", "", "", "", "", "Không thể tải dữ liệu" });
        }
    }

    private void populateOrdersTable(List<Order> orders) {
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

            // Thêm hàng vào bảng đơn hàng
            ordersTableModel.addRow(new Object[] {
                    "#" + order.getId(),
                    userName,
                    user != null ? user.getEmail() : "N/A",
                    orderDate,
                    priceFormat.format(order.getTotalPrice()),
                    order.getStatus()
            });
        }

        if (orders.isEmpty()) {
            ordersTableModel.addRow(new Object[] { "", "", "", "", "", "Không có đơn hàng" });
        }
    }

    private void loadOrderDetails(int orderId) {
        // Xóa dữ liệu cũ
        detailsTableModel.setRowCount(0);

        try {
            List<OrderDetails> orderDetails = orderDetailsService.getOrderDetailsByOrderId(orderId);
            populateDetailsTable(orderDetails);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Không thể tải dữ liệu chi tiết đơn hàng: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);

            // Nếu có lỗi, hiển thị thông báo trong bảng
            detailsTableModel.addRow(new Object[] { "", "", "", "", "Không thể tải dữ liệu" });
        }
    }

    private void populateDetailsTable(List<OrderDetails> orderDetails) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DecimalFormat priceFormat = new DecimalFormat("#,### VND");

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

            // Thêm hàng vào bảng chi tiết đơn hàng
            detailsTableModel.addRow(new Object[] {
                    "#" + detail.getId(),
                    detail.getFullDomainName(),
                    priceFormat.format(detail.getPrice()),
                    purchaseDate,
                    detail.getStatus()
            });
        }

        if (orderDetails.isEmpty()) {
            detailsTableModel.addRow(new Object[] { "", "", "", "", "Không có chi tiết đơn hàng" });
        }
    }

    private void clearDetailsTable() {
        detailsTableModel.setRowCount(0);
    }

    private void filterOrders() {
        String selectedStatus = (String) statusFilter.getSelectedItem();

        if ("Tất cả trạng thái".equals(selectedStatus)) {
            loadOrderData();
            return;
        }

        // Xóa dữ liệu cũ
        ordersTableModel.setRowCount(0);

        try {
            List<Order> allOrders = orderRepository.getAllOrders();
            List<Order> filteredOrders = allOrders.stream()
                    .filter(order -> selectedStatus.equals(order.getStatus()))
                    .collect(Collectors.toList());

            populateOrdersTable(filteredOrders);
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
                for (int i = 0; i < ordersTableModel.getColumnCount(); i++) {
                    writer.write(ordersTableModel.getColumnName(i)
                            + (i == ordersTableModel.getColumnCount() - 1 ? "\n" : ","));
                }

                // Ghi dữ liệu từng hàng
                for (int i = 0; i < ordersTableModel.getRowCount(); i++) {
                    for (int j = 0; j < ordersTableModel.getColumnCount(); j++) {
                        Object value = ordersTableModel.getValueAt(i, j);
                        writer.write((value != null ? value.toString() : "") +
                                (j == ordersTableModel.getColumnCount() - 1 ? "\n" : ","));
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

    private void updateSelectedStatus() {
        if (selectedOrderId != -1) {
            updateOrderStatus(selectedOrderId);
        } else if (selectedOrderDetailId != -1) {
            updateOrderDetailStatus(selectedOrderDetailId);
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

    private void updateOrderDetailStatus(int detailId) {
        try {
            OrderDetails detail = orderDetailsRepository.getOrderDetailById(detailId);
            if (detail != null) {
                String[] statuses = { "Hoàn thành", "Đang xử lý", "Hủy" };

                String selectedStatus = (String) JOptionPane.showInputDialog(parentFrame,
                        "Chọn trạng thái mới cho chi tiết đơn hàng #" + detailId,
                        "Cập nhật trạng thái",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        statuses,
                        detail.getStatus());

                if (selectedStatus != null && !selectedStatus.equals(detail.getStatus())) {
                    // Sử dụng phương thức mới updateOrderDetailStatus
                    boolean result = orderDetailsRepository.updateOrderDetailStatus(detailId, selectedStatus);

                    if (result) {
                        JOptionPane.showMessageDialog(parentFrame,
                                "Cập nhật trạng thái thành công!",
                                "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                        // Refresh data
                        loadOrderDetails(selectedOrderId);
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