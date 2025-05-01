package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import model.*;
import repository.DatabaseConnection;
import repository.DomainRepository;
import repository.OrderRepository;
import repository.UserRepository;

public class UserService {
    private UserRepository userRepository;
    private OrderRepository orderRepository;
    private DomainRepository domainRepository;

    public UserService(Connection connection) {
        this.userRepository = new UserRepository(connection);
        this.orderRepository = new OrderRepository(connection);
        this.domainRepository = new DomainRepository(connection);
    }

    // Constructor mặc định để AdminDashboardView có thể khởi tạo
    public UserService() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            this.userRepository = new UserRepository(connection);
            this.orderRepository = new OrderRepository(connection);
            this.domainRepository = new DomainRepository(connection);
        } catch (SQLException e) {
            System.err.println("Error creating UserService: " + e.getMessage());
            e.printStackTrace();
            // Sử dụng repository mặc định nếu không thể kết nối
            this.userRepository = new UserRepository();
            this.orderRepository = new OrderRepository();
            this.domainRepository = new DomainRepository();
        }
    }

    // Lấy danh sách tên miền đã mua của một user
    public List<DomainPurchase> getUserDomainPurchases(int userId) throws SQLException {
        return userRepository.findDomainPurchasesByUserId(userId);
    }

    // Tìm kiếm người dùng theo tên đăng nhập
    public Optional<User> findUserByUsername(String username) throws SQLException {
        return userRepository.findByUsername(username);
    }

    // Thêm người dùng mới
    public void addUser(User user) throws SQLException {
        userRepository.save(user);
    }

    // Lấy thông tin người dùng theo ID
    public Optional<User> getUserByIdWithException(int id) throws SQLException {
        return userRepository.findById(id);
    }

    // Lấy tất cả người dùng (phương thức có throw SQLException)
    public List<User> getAllUsersWithException() throws SQLException {
        return userRepository.findAll();
    }

    // Cập nhật thông tin người dùng
    public void updateUserWithException(User user) throws SQLException {
        userRepository.save(user);
    }

    // Xóa người dùng
    public void deleteUser(int id) throws SQLException {
        userRepository.deleteById(id);
    }

    // Kiểm tra một tên miền có thuộc về user hay không
    public boolean isDomainOwnedByUser(int domainId, int userId) throws SQLException {
        List<Order> orders = orderRepository.findByBuyerId(userId);
        for (Order order : orders) {
            if (order.getDomainId() == domainId &&
                    (order.getStatus().equals("Completed") || order.getStatus().equals("Active"))) {
                return true;
            }
        }
        return false;
    }

    // Đổi mật khẩu
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        try {
            return userRepository.changePassword(userId, oldPassword, newPassword);
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Trả về false nếu có lỗi
        }
    }

    // Chỉnh sửa thông tin người dùng
    public boolean updateUserInfo(int userId, String fullName, String email) {
        try {
            return userRepository.updateUserInfo(userId, fullName, email);
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Trả về false nếu có lỗi
        }
    }

    public Optional<User> login(String usernameOrEmail, String password) throws SQLException {
        return userRepository.authenticate(usernameOrEmail, password);
    }

    public boolean register(User user) {
        try {
            // Kiểm tra username và email đã tồn tại chưa
            Optional<User> existingUsername = userRepository.findByUsername(user.getUsername());
            if (existingUsername.isPresent()) {
                throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
            }

            Optional<User> existingEmail = userRepository.findByEmail(user.getEmail());
            if (existingEmail.isPresent()) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }

            // Đặt vai trò mặc định là Customer nếu chưa được chỉ định
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("Customer");
            }

            userRepository.save(user);
            return true;
        } catch (SQLException | IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Phương thức được sử dụng bởi AdminDashboardView (không throw Exception)
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public User getUserById(int id) {
        return userRepository.getUserById(id);
    }

    public boolean updateUser(User user) {
        return userRepository.updateUser(user);
    }

    // Kiểm tra xem người dùng có quyền admin không
    public boolean isAdmin(User user) {
        return user != null && "Admin".equalsIgnoreCase(user.getRole());
    }

    // Kiểm tra xem người dùng có quyền staff không (giờ đây chỉ Admin có quyền này)
    public boolean isStaff(User user) {
        return user != null && "Admin".equalsIgnoreCase(user.getRole());
    }

    // Tạo người dùng mới với quyền khách hàng
    public boolean createCustomer(User user) {
        user.setRole("Customer");
        return register(user);
    }
}