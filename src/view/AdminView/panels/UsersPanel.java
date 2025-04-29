package view.AdminView.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import model.User;
import repository.UserRepository;

public class UsersPanel extends JPanel {
    private static final Color BG_COLOR = new Color(245, 245, 245);
    private static final Color PRIMARY_COLOR = new Color(0, 102, 102);
    private static final Color ACCENT_COLOR = new Color(255, 153, 0);

    private UserRepository userRepository;
    private JTable usersTable;
    private DefaultTableModel tableModel;
    private JFrame parentFrame;
    private JComboBox<String> roleFilter;

    public UsersPanel(UserRepository userRepository, JFrame parentFrame) {
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

        roleFilter = new JComboBox<>(new String[] { "Tất cả vai trò", "Admin", "Customer" });
        roleFilter.setPreferredSize(new Dimension(150, 35));

        roleFilter.addActionListener(e -> filterUsers());

        searchPanel.add(searchField);
        searchPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        searchPanel.add(roleFilter);

        // Nút thêm người dùng
        JButton addButton = new JButton("Thêm người dùng");
        addButton.setBackground(ACCENT_COLOR);
        addButton.setForeground(Color.BLACK);
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.setPreferredSize(new Dimension(150, 35));

        addButton.addActionListener(e -> showAddUserDialog());

        toolPanel.add(searchPanel, BorderLayout.WEST);
        toolPanel.add(addButton, BorderLayout.EAST);

        // Bảng người dùng
        String[] columns = { "ID", "Họ tên", "Email", "Vai trò", "Ngày tạo", "Trạng thái", "Thao tác" };
        tableModel = new DefaultTableModel(columns, 0);

        usersTable = new JTable(tableModel);
        usersTable.setRowHeight(40);
        usersTable.setShowVerticalLines(false);

        // Render cho cột vai trò
        usersTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                switch (value.toString()) {
                    case "Admin":
                        c.setForeground(new Color(231, 76, 60));
                        break;
                    case "Customer":
                        c.setForeground(new Color(41, 128, 185));
                        break;
                    default:
                        c.setForeground(Color.BLACK);
                }

                return c;
            }
        });

        // Render cho cột trạng thái
        usersTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if ("Hoạt động".equals(value)) {
                    c.setForeground(new Color(39, 174, 96));
                } else if ("Bị khóa".equals(value)) {
                    c.setForeground(new Color(231, 76, 60));
                } else {
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        });

        // Button renderer cho cột thao tác
        usersTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                actionsPanel.setOpaque(false);

                JButton editButton = new JButton("Sửa");
                editButton.setPreferredSize(new Dimension(60, 30));
                editButton.setBackground(new Color(52, 152, 219));
                editButton.setForeground(Color.BLACK);
                editButton.setFocusPainted(false);

                JButton statusButton = new JButton("Đổi trạng thái");
                statusButton.setPreferredSize(new Dimension(100, 30));
                statusButton.setBackground(new Color(243, 156, 18));
                statusButton.setForeground(Color.BLACK);
                statusButton.setFocusPainted(false);

                actionsPanel.add(editButton);
                actionsPanel.add(statusButton);

                return actionsPanel;
            }
        });

        // Mouse listener cho các nút trong bảng
        usersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = usersTable.getColumnModel().getColumnIndexAtX(e.getX());
                int row = e.getY() / usersTable.getRowHeight();

                if (row < usersTable.getRowCount() && row >= 0 && column == 6) {
                    // Lấy toạ độ của ô được click
                    Rectangle rect = usersTable.getCellRect(row, column, false);

                    // Tính toán vị trí tương đối trong ô
                    int xInCell = e.getX() - rect.x;

                    int userId = Integer.parseInt(usersTable.getValueAt(row, 0).toString());

                    // Nếu click gần phía trái (nút Sửa)
                    if (xInCell <= 65) {
                        editUser(userId);
                    }
                    // Nếu click gần phía phải (nút Đổi trạng thái)
                    else if (xInCell > 65) {
                        toggleUserStatus(userId);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(toolPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Load dữ liệu
        loadUserData();
    }

    public void loadUserData() {
        // Xóa dữ liệu cũ
        tableModel.setRowCount(0);

        try {
            List<User> users = userRepository.getAllUsers();
            populateUserData(users);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Không thể tải dữ liệu người dùng: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);

            // Nếu có lỗi, hiển thị thông báo trong bảng
            tableModel.addRow(new Object[] { "", "", "", "", "", "Không thể tải dữ liệu", "" });
        }
    }

    private void populateUserData(List<User> users) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        for (User user : users) {
            // Lấy tên vai trò
            String roleName = "";
            switch (user.getRoleAsInt()) {
                case 1:
                    roleName = "Admin";
                    break;
                case 2:
                    roleName = "Customer";
                    break;
                default:
                    roleName = "Không xác định";
            }

            // Xác định trạng thái
            String status = user.isActive() ? "Hoạt động" : "Bị khóa";

            // Định dạng ngày tạo
            String createdDate = user.getCreatedDate() != null ? dateFormat.format(user.getCreatedDate()) : "";

            tableModel.addRow(new Object[] {
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    roleName,
                    createdDate,
                    status,
                    ""
            });
        }

        if (users.isEmpty()) {
            tableModel.addRow(new Object[] { "", "", "", "", "", "Không có người dùng", "" });
        }
    }

    private void filterUsers() {
        String selectedRole = (String) roleFilter.getSelectedItem();

        if ("Tất cả vai trò".equals(selectedRole)) {
            loadUserData();
            return;
        }

        int roleId;
        switch (selectedRole) {
            case "Admin":
                roleId = 1;
                break;
            case "Customer":
                roleId = 2;
                break;
            default:
                roleId = 0;
        }

        // Xóa dữ liệu cũ
        tableModel.setRowCount(0);

        try {
            List<User> allUsers = userRepository.getAllUsers();
            List<User> filteredUsers = allUsers.stream()
                    .filter(user -> user.getRoleAsInt() == roleId)
                    .collect(Collectors.toList());

            populateUserData(filteredUsers);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Lỗi khi lọc người dùng: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddUserDialog() {
        // TODO: Hiển thị form thêm người dùng mới
        JOptionPane.showMessageDialog(parentFrame,
                "Chức năng thêm người dùng mới sẽ được triển khai sau.",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void editUser(int userId) {
        try {
            User user = userRepository.getUserById(userId);
            if (user != null) {
                // Tạo form chỉnh sửa người dùng
                JOptionPane.showMessageDialog(parentFrame,
                        "Chỉnh sửa thông tin người dùng: " + user.getEmail(),
                        "Chỉnh sửa người dùng",
                        JOptionPane.INFORMATION_MESSAGE);

                // TODO: Hiển thị form chỉnh sửa người dùng
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Không thể tải thông tin người dùng: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleUserStatus(int userId) {
        try {
            User user = userRepository.getUserById(userId);
            if (user != null) {
                // Đổi trạng thái người dùng
                user.setActive(!user.isActive());
                boolean result = userRepository.updateUser(user);

                if (result) {
                    String status = user.isActive() ? "kích hoạt" : "khóa";
                    JOptionPane.showMessageDialog(parentFrame,
                            "Đã " + status + " người dùng thành công!",
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                    // Refresh data
                    loadUserData();
                } else {
                    JOptionPane.showMessageDialog(parentFrame,
                            "Không thể thay đổi trạng thái người dùng!",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Lỗi khi thay đổi trạng thái người dùng: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}