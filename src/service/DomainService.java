package service;

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
import model.Domain;
import model.Order;
import model.OrderDetails;
import model.RentalPeriod;
import repository.DatabaseConnection;
import repository.DomainRepository;
import repository.OrderRepository;
import repository.RentalPeriodRepository;
import service.OrderDetailsService;

public class DomainService {
    private DomainRepository domainRepository;
    private OrderRepository orderRepository;
    private RentalPeriodRepository rentalPeriodRepository;
    private OrderDetailsService orderDetailsService;

    public DomainService(Connection connection) {
        this.domainRepository = new DomainRepository(connection);
        this.orderRepository = new OrderRepository(connection);
        this.rentalPeriodRepository = new RentalPeriodRepository(connection);
        this.orderDetailsService = new OrderDetailsService();
    }

    // Constructor mặc định để AdminDashboardView có thể khởi tạo
    public DomainService() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            this.domainRepository = new DomainRepository(connection);
            this.orderRepository = new OrderRepository(connection);
            this.rentalPeriodRepository = new RentalPeriodRepository(connection);
            this.orderDetailsService = new OrderDetailsService();
        } catch (SQLException e) {
            System.err.println("Error creating DomainService: " + e.getMessage());
            e.printStackTrace();

            // Sử dụng các repository mặc định nếu không thể kết nối
            this.domainRepository = new DomainRepository();
            this.orderRepository = new OrderRepository();
            this.rentalPeriodRepository = new RentalPeriodRepository();
            this.orderDetailsService = new OrderDetailsService();
        }
    }

    // Phương thức được sử dụng bởi AdminDashboardView
    public List<Domain> getExpiringDomains(int daysThreshold, int limit) {
        List<Domain> result = new ArrayList<>();
        try {
            // Lấy tất cả tên miền
            List<Domain> domains = domainRepository.getAllDomains();

            // Lọc tên miền sắp hết hạn trong khoảng thời gian daysThreshold (mặc định 30
            // ngày)
            LocalDateTime currentDateTime = LocalDateTime.now();
            LocalDateTime thresholdDateTime = currentDateTime.plusDays(daysThreshold);

            List<Domain> expiringDomains = domains.stream()
                    .filter(domain -> {
                        LocalDateTime expiryDate = domain.getExpiryDate();
                        if (expiryDate == null) {
                            return false;
                        }
                        // Chỉ lấy những domain mà thời gian hết hạn nằm giữa hiện tại và ngưỡng
                        return expiryDate.isAfter(currentDateTime) && expiryDate.isBefore(thresholdDateTime);
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

        // Tính giá gốc và giá sau khi giảm
        double originalPrice = domain.getPrice() * period.getMonths();
        double discountedPrice = calculatePriceForPeriod(domain.getPrice(), period.getMonths());

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
        order.setTotalPrice(discountedPrice); // Lưu giá đã giảm vào order

        // Lưu đơn hàng
        orderRepository.save(order);

        // Tạo chi tiết đơn hàng (OrderDetails)
        createOrderDetails(order.getId(), domain, originalPrice, discountedPrice, period, now, order.getStatus());

        // Cập nhật trạng thái tên miền
        domain.setStatus("Reserved");
        domainRepository.save(domain);

        return order;
    }

    // Phương thức tạo nhiều đơn hàng cho giỏ hàng
    public List<Order> createBulkRentalOrders(int userId, List<Integer> domainIds, int rentalPeriodId)
            throws SQLException {
        List<Order> createdOrders = new ArrayList<>();

        // Lấy thông tin gói thuê
        Optional<RentalPeriod> periodOpt = rentalPeriodRepository.findById(rentalPeriodId);
        if (!periodOpt.isPresent()) {
            throw new IllegalArgumentException("Gói thuê không hợp lệ");
        }
        RentalPeriod period = periodOpt.get();

        // Tạo đơn hàng cho từng tên miền
        for (int domainId : domainIds) {
            try {
                // Lấy thông tin tên miền
                Optional<Domain> domainOpt = domainRepository.findById(domainId);
                if (!domainOpt.isPresent() || !domainOpt.get().getStatus().equalsIgnoreCase("Available")) {
                    continue; // Bỏ qua tên miền không khả dụng
                }
                Domain domain = domainOpt.get();

                // Tính giá gốc và giá sau khi giảm
                double originalPrice = domain.getPrice() * period.getMonths();
                double discountedPrice = calculatePriceForPeriod(domain.getPrice(), period.getMonths());

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
                order.setTotalPrice(discountedPrice); // Lưu giá đã giảm vào order

                // Lưu đơn hàng
                orderRepository.save(order);

                // Tạo chi tiết đơn hàng (OrderDetails)
                createOrderDetails(order.getId(), domain, originalPrice, discountedPrice, period, now,
                        order.getStatus());

                // Cập nhật trạng thái tên miền
                domain.setStatus("Reserved");
                domainRepository.save(domain);

                createdOrders.add(order);
            } catch (Exception e) {
                System.err.println("Error creating order for domain ID " + domainId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        return createdOrders;
    }

    // Tạo chi tiết đơn hàng cho một domain
    private void createOrderDetails(int orderId, Domain domain, double originalPrice, double discountedPrice,
            RentalPeriod period, LocalDateTime purchaseDate, String status) {
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setOrderId(orderId);
        orderDetails.setDomainId(domain.getId());
        orderDetails.setDomainName(domain.getName());
        orderDetails.setDomainExtension(domain.getExtension());
        orderDetails.setOriginalPrice(originalPrice); // Lưu giá gốc
        orderDetails.setPrice(discountedPrice); // Lưu giá đã giảm
        orderDetails.setPurchaseDate(purchaseDate);
        orderDetails.setExpiryDate(purchaseDate.plusMonths(period.getMonths())); // Set expiry date based on rental
                                                                                 // period
        orderDetails.setRentalPeriodId(period.getId());
        orderDetails.setStatus(status);

        // Tạo chi tiết đơn hàng qua service
        orderDetailsService.createOrderDetail(orderDetails);
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
            prices[i] = calculatePriceForPeriod(monthlyPrice, periods[i]);
        }
        return prices;
    }

    // Phương thức để tính giá thuê cho một khoảng thời gian cụ thể
    public double calculatePriceForPeriod(double basePrice, int months) {
        try {
            Optional<RentalPeriod> period = rentalPeriodRepository.findByMonths(months);
            if (period.isPresent()) {
                return basePrice * months * (1 - period.get().getDiscount());
            }
        } catch (SQLException e) {
            System.err.println("Error calculating price for period: " + e.getMessage());
            e.printStackTrace();
        }

        // Nếu không tìm thấy giảm giá, trả về giá đầy đủ
        return basePrice * months;
    }

    // Get rental discount information for displaying in UI
    public String getRentalDiscountInfo(int months) {
        try {
            Optional<RentalPeriod> period = rentalPeriodRepository.findByMonths(months);
            if (period.isPresent()) {
                double discountPercent = period.get().getDiscount() * 100;
                if (discountPercent > 0) {
                    return String.format("Giảm %.0f%% cho thuê %d tháng", discountPercent, months);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting rental discount info: " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }
}