package backEnd.service;

import backEnd.repository.UserRepository;
import backEnd.repository.OrderRepository;
import backEnd.repository.DomainRepository;
import entity.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final DomainRepository domainRepository;

    public UserService(Connection connection) {
        this.userRepository = new UserRepository(connection);
        this.orderRepository = new OrderRepository(connection);
        this.domainRepository = new DomainRepository(connection);
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
    public Optional<User> getUserById(int id) throws SQLException {
        return userRepository.findById(id);
    }

    // Lấy tất cả người dùng
    public List<User> getAllUsers() throws SQLException {
        return userRepository.findAll();
    }

    // Cập nhật thông tin người dùng
    public void updateUser(User user) throws SQLException {
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
}