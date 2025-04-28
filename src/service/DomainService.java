package service;

import model.Domain;
import model.Order;
import model.RentalPeriod;
import repository.DatabaseConnection;
import repository.DomainRepository;
import repository.OrderRepository;
import repository.RentalPeriodRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DomainService {
    private DomainRepository domainRepository;
    private OrderRepository orderRepository;
    private RentalPeriodRepository rentalPeriodRepository;

    public DomainService(Connection connection) {
        this.domainRepository = new DomainRepository(connection);
        this.orderRepository = new OrderRepository(connection);
        this.rentalPeriodRepository = new RentalPeriodRepository(connection);
    }

    // Constructor mặc định để AdminDashboardView có thể khởi tạo
    public DomainService() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            this.domainRepository = new DomainRepository(connection);
            this.orderRepository = new OrderRepository(connection);
            this.rentalPeriodRepository = new RentalPeriodRepository(connection);
        } catch (SQLException e) {
            System.err.println("Error creating DomainService: " + e.getMessage());
            e.printStackTrace();

            // Sử dụng các repository mặc định nếu không thể kết nối
            this.domainRepository = new DomainRepository();
            this.orderRepository = new OrderRepository();
            this.rentalPeriodRepository = new RentalPeriodRepository();
        }
    }

    // Phương thức được sử dụng bởi AdminDashboardView
    public List<Domain> getExpiringDomains(int daysThreshold, int limit) {
        List<Domain> result = new ArrayList<>();
        try {
            // Lấy tất cả tên miền
            List<Domain> domains = domainRepository.getAllDomains();

            // Lọc tên miền sắp hết hạn trong số ngày cụ thể
            Date currentDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            calendar.add(Calendar.DAY_OF_MONTH, daysThreshold);
            Date thresholdDate = calendar.getTime();

            List<Domain> expiringDomains = domains.stream()
                    .filter(domain -> {
                        // Chuyển đổi LocalDateTime sang Date để so sánh
                        LocalDateTime expiryLocalDateTime = domain.getExpiryDate();
                        if (expiryLocalDateTime == null) {
                            return false;
                        }

                        Date expiryDate = Date.from(expiryLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
                        return expiryDate != null &&
                                !expiryDate.before(currentDate) &&
                                !expiryDate.after(thresholdDate);
                    })
                    .limit(limit)
                    .collect(Collectors.toList());

            return expiringDomains;
        } catch (Exception e) {
            System.err.println("Error getting expiring domains: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    // Phương thức để lấy tất cả các gói thuê
    public List<RentalPeriod> getAllRentalPeriods() throws SQLException {
        return rentalPeriodRepository.findAll();
    }

    // Phương thức để tạo một đơn hàng thuê tên miền
    public Order createRentalOrder(int userId, int domainId, int rentalPeriodId) throws SQLException {
        // Lấy thông tin tên miền
        Optional<Domain> domainOpt = domainRepository.findById(domainId);
        if (!domainOpt.isPresent() || !domainOpt.get().getStatus().equalsIgnoreCase("Available")) {
            throw new IllegalArgumentException("Tên miền không khả dụng");
        }
        Domain domain = domainOpt.get();

        // Lấy thông tin gói thuê
        Optional<RentalPeriod> periodOpt = rentalPeriodRepository.findById(rentalPeriodId);
        if (!periodOpt.isPresent()) {
            throw new IllegalArgumentException("Gói thuê không hợp lệ");
        }
        RentalPeriod period = periodOpt.get();

        // Tính tổng giá tiền
        double totalPrice = domain.getPrice() * period.getMonths() * (1 - period.getDiscount());

        // Tính ngày hết hạn
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plusMonths(period.getMonths());

        // Tạo đơn hàng mới
        Order order = new Order();
        order.setBuyerId(userId);
        order.setDomainId(domainId);
        order.setRentalPeriodId(rentalPeriodId);
        order.setStatus("Pending");
        order.setCreatedAt(now);
        order.setExpiryDate(expiryDate);
        order.setTotalPrice(totalPrice);

        // Lưu đơn hàng
        orderRepository.save(order);

        // Cập nhật trạng thái tên miền
        domain.setStatus("Reserved");
        domainRepository.save(domain);

        return order;
    }

    // Phương thức để hiển thị các lựa chọn thuê cho tên miền
    public List<Domain> getDomainRentalOptions(String domainName) throws SQLException {
        List<Domain> options = DomainExtensionService.generateDomainsWithAllExtensions(domainName);

        // Kiểm tra xem tên miền nào đã được thuê
        for (Domain option : options) {
            Optional<Domain> existingDomain = domainRepository.findByNameAndExtension(option.getName(),
                    option.getExtension());
            if (existingDomain.isPresent()) {
                Domain domain = existingDomain.get();
                option.setId(domain.getId());
                option.setStatus(domain.getStatus());
                option.setExpiryDate(domain.getExpiryDate());
            }
        }

        return options;
    }

    // Phương thức để tính giá thuê cho các gói khác nhau
    public double[] calculateRentalPrices(double monthlyPrice, int[] periods) {
        double[] prices = new double[periods.length];
        for (int i = 0; i < periods.length; i++) {
            int months = periods[i];
            switch (months) {
                case 1:
                    prices[i] = monthlyPrice; // Giá gốc cho 1 tháng
                    break;
                case 6:
                    prices[i] = monthlyPrice * 6 * 0.9; // Giảm 10% khi thuê 6 tháng
                    break;
                case 12:
                    prices[i] = monthlyPrice * 12 * 0.8; // Giảm 20% khi thuê 12 tháng
                    break;
                default:
                    prices[i] = monthlyPrice * months; // Không có khuyến mãi
            }
        }
        return prices;
    }
}