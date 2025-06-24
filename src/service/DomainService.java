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
        if (!domainOpt.isPresent() || !domainOpt.get().getStatus().equalsIgnoreCase("Sẵn sàng")) {
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
        order.setRentalPeriodId(rentalPeriodId);
        order.setStatus("Đang xử lý");
        order.setCreatedAt(now);
        order.setExpiryDate(expiryDate);
        order.setTotalPrice(discountedPrice); // Lưu giá đã giảm vào order

        // Lưu đơn hàng
        System.out.println("rental_period_id = " + order.getRentalPeriodId());
        orderRepository.save(order);

        // Tạo chi tiết đơn hàng (OrderDetails)
        createOrderDetails(order.getId(), domain, originalPrice, discountedPrice, period, now, order.getStatus());

        // Cập nhật trạng thái tên miền
        domain.setStatus("Đã đặt");
        System.out.println("rental_period_id = " + order.getRentalPeriodId());
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
                if (!domainOpt.isPresent() || !domainOpt.get().getStatus().equalsIgnoreCase("Sẵn sàng")) {
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
                order.setRentalPeriodId(rentalPeriodId);
                order.setStatus("Đang xử lý");
                order.setCreatedAt(now);
                order.setExpiryDate(expiryDate);
                order.setTotalPrice(discountedPrice); // Lưu giá đã giảm vào order

                // Lưu đơn hàng
                System.out.println("rental_period_id = " + order.getRentalPeriodId());
                orderRepository.save(order);

                // Tạo chi tiết đơn hàng (OrderDetails)
                createOrderDetails(order.getId(), domain, originalPrice, discountedPrice, period, now,
                        order.getStatus());

                // Cập nhật trạng thái tên miền
                domain.setStatus("Đã đặt");
                System.out.println("rental_period_id = " + order.getRentalPeriodId());
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
        for (int i = 0;i < periods.length; i++) {
            prices[i] = calculatePriceForPeriod(monthlyPrice, periods[i]);
        }
        return prices;
    }

    // Phương thức để tính giá thuê cho một khoảng thời gian cụ thể
    public double calculatePriceForPeriod(double basePrice, int months) {
        // Validation để tránh overflow
        if (basePrice < 0 || months <= 0) {
            throw new IllegalArgumentException("Base price và months phải là số dương");
        }
        
        // Kiểm tra overflow trước khi tính toán
        if (basePrice > 999999999.99 / months) {
            throw new IllegalArgumentException("Giá tính toán quá lớn, vượt quá giới hạn cho phép");
        }

        try {
            Optional<RentalPeriod> period = rentalPeriodRepository.findByMonths(months);
            if (period.isPresent()) {
                double discount = period.get().getDiscount();
                // Kiểm tra discount hợp lệ
                if (discount < 0 || discount >= 1) {
                    System.err.println("Invalid discount value: " + discount + ". Using no discount.");
                    discount = 0;
                }
                
                double result = basePrice * months * (1 - discount);
                
                // Kiểm tra kết quả không vượt quá giới hạn DECIMAL(15,2)
                if (result > 9999999999999.99) {
                    throw new IllegalArgumentException("Giá tính toán quá lớn: " + result);
                }
                
                return result;
            }
        } catch (SQLException e) {
            System.err.println("Error calculating price for period: " + e.getMessage());
            e.printStackTrace();
        }

        // Nếu không tìm thấy giảm giá, trả về giá đầy đủ
        double result = basePrice * months;
        
        // Kiểm tra kết quả không vượt quá giới hạn
        if (result > 9999999999999.99) {
            throw new IllegalArgumentException("Giá tính toán quá lớn: " + result);
        }
        
        return result;
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

    /**
     * Creates an order from the shopping cart
     * 
     * @param userId      The ID of the user making the purchase
     * @param totalPrice  The total price of the order
     * @param cartDomains List of domains in the cart
     * @return true if order creation was successful, false otherwise
     */
    public boolean createOrderForCart(int userId, double totalPrice, List<Domain> cartDomains) {
        if (cartDomains == null || cartDomains.isEmpty()) {
            return false;
        }

        // Validate cart before processing - make sure all domains are still available
        java.util.List<String> unavailableDomains = validateAndCleanupCart(userId);
        if (!unavailableDomains.isEmpty()) {
            System.err.println("Some domains in cart are no longer available: " + unavailableDomains);
            return false; // Cart validation failed
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Start a transaction
            connection.setAutoCommit(false);

            try {
                // Create a parent order
                LocalDateTime now = LocalDateTime.now();
                Order mainOrder = new Order();
                mainOrder.setBuyerId(userId);
                mainOrder.setStatus("Đang xử lý");
                mainOrder.setCreatedAt(now);
                mainOrder.setTotalPrice(totalPrice);

                // Tìm rental period phổ biến nhất hoặc có thời gian dài nhất để gán cho đơn
                // hàng chính
                int maxMonths = 12; // Mặc định là 12 tháng nếu không tìm được gì
                int mainRentalPeriodId = 1; // Mặc định là ID 1 nếu không tìm được gói thuê

                // Tìm gói thuê có thời gian dài nhất trong giỏ hàng
                for (Domain domain : cartDomains) {
                    try {
                        int rentalPeriodId = getRentalPeriodIdFromCart(userId, domain.getId());
                        Optional<RentalPeriod> periodOpt = rentalPeriodRepository.findById(rentalPeriodId);

                        if (periodOpt.isPresent()) {
                            RentalPeriod period = periodOpt.get();
                            if (period.getMonths() > maxMonths) {
                                maxMonths = period.getMonths();
                                mainRentalPeriodId = rentalPeriodId;
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error getting rental period for domain: " + e.getMessage());
                    }
                }

                // Thiết lập rental_period_id cho đơn hàng chính
                mainOrder.setRentalPeriodId(mainRentalPeriodId);

                // Đặt ngày hết hạn dựa trên số tháng tối đa
                mainOrder.setExpiryDate(now.plusMonths(maxMonths));

                // Save the main order to get its ID
                orderRepository.save(mainOrder);

                // Process each domain in the cart
                for (Domain domain : cartDomains) {
                    // Get rental period ID from cart
                    int rentalPeriodId = getRentalPeriodIdFromCart(userId, domain.getId());
                    Optional<RentalPeriod> periodOpt = rentalPeriodRepository.findById(rentalPeriodId);

                    if (!periodOpt.isPresent()) {
                        // If rental period not found, use default 1 year (12 months)
                        periodOpt = rentalPeriodRepository.findByMonths(12);
                        if (!periodOpt.isPresent()) {
                            throw new SQLException("Default rental period not found");
                        }
                    }

                    RentalPeriod period = periodOpt.get();

                    // Calculate original and discounted price with validation
                    if (!utils.ValidationUtils.isValidPriceCalculation(domain.getPrice(), period.getMonths(), period.getDiscount())) {
                        System.err.println("Invalid price calculation for domain " + domain.getName() + domain.getExtension() + 
                                         ". Price: " + domain.getPrice() + ", Months: " + period.getMonths() + 
                                         ", Discount: " + period.getDiscount());
                        continue; // Skip this domain if calculation would overflow
                    }
                    
                    double originalPrice = domain.getPrice() * period.getMonths();
                    double discountedPrice = calculatePriceForPeriod(domain.getPrice(), period.getMonths());

                    // Set expiry date based on rental period
                    LocalDateTime expiryDate = now.plusMonths(period.getMonths());

                    // Create order details for this domain
                    OrderDetails orderDetails = new OrderDetails();
                    orderDetails.setOrderId(mainOrder.getId());
                    orderDetails.setDomainId(domain.getId());
                    orderDetails.setDomainName(domain.getName());
                    orderDetails.setDomainExtension(domain.getExtension());
                    orderDetails.setOriginalPrice(originalPrice);
                    orderDetails.setPrice(discountedPrice);
                    orderDetails.setPurchaseDate(now);
                    orderDetails.setExpiryDate(expiryDate);
                    orderDetails.setRentalPeriodId(period.getId());
                    orderDetails.setStatus("Đang xử lý");

                    // Save order details
                    orderDetailsService.createOrderDetail(orderDetails);

                    // Update domain status to Reserved and ensure expiry date is set
                    domain.setStatus("Đã đặt");
                    domain.setExpiryDate(expiryDate); // This ensures expiry date is not null
                    domainRepository.save(domain);

                    // Delete domain from cart
                    deleteFromCart(userId, domain.getId());
                }

                // Commit the transaction
                connection.commit();
                return true;
            } catch (Exception e) {
                // Rollback on error
                connection.rollback();
                System.err.println("Error creating order from cart: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                // Restore auto-commit
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get the rental period ID for a domain in the cart
     * 
     * @param userId   User ID
     * @param domainId Domain ID
     * @return Rental period ID
     */
    private int getRentalPeriodIdFromCart(int userId, int domainId) {
        int defaultPeriodId = 1; // Default to 1 year (ID=1)

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT rental_period_id FROM cart WHERE user_id = ? AND domain_id = ?";
            try (java.sql.PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, domainId);

                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("rental_period_id");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting rental period from cart: " + e.getMessage());
            e.printStackTrace();
        }

        return defaultPeriodId;
    }

    /**
     * Delete a domain from the user's cart
     * 
     * @param userId   User ID
     * @param domainId Domain ID
     */
    private void deleteFromCart(int userId, int domainId) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "DELETE FROM cart WHERE user_id = ? AND domain_id = ?";
            try (java.sql.PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, domainId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error deleting domain from cart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Remove domain from all users' carts (used when domain is approved for purchase)
     * This prevents conflicts when multiple users have the same domain in their cart
     * 
     * @param domainName The name of the domain (without extension)
     * @param domainExtension The extension of the domain (with dot)
     */
    public void removeDomainFromAllCarts(String domainName, String domainExtension) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Use stored procedure for better performance
            String callProcedure = "{call RemoveDomainFromAllCarts(?, ?)}";
            try (java.sql.CallableStatement stmt = connection.prepareCall(callProcedure)) {
                stmt.setString(1, domainName);
                stmt.setString(2, domainExtension);
                
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int deletedRows = rs.getInt("DeletedRows");
                        if (deletedRows > 0) {
                            System.out.println("Removed domain " + domainName + domainExtension + 
                                             " from " + deletedRows + " user cart(s) to prevent conflicts.");
                            
                            // Log the conflict for admin review
                            logDomainConflict(domainName, domainExtension, deletedRows);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error removing domain from all carts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validate and clean up cart before checkout
     * Removes domains that are no longer available
     * 
     * @param userId User ID to validate cart for
     * @return List of domain names that were removed due to unavailability
     */
    public java.util.List<String> validateAndCleanupCart(int userId) {
        java.util.List<String> removedDomains = new java.util.ArrayList<>();
        
        try (Connection connection = DatabaseConnection.getConnection()) {
            // First get list of unavailable domains in cart
            String validateCall = "{call ValidateCartDomains(?)}";
            try (java.sql.CallableStatement validateStmt = connection.prepareCall(validateCall)) {
                validateStmt.setInt(1, userId);
                
                try (java.sql.ResultSet rs = validateStmt.executeQuery()) {
                    while (rs.next()) {
                        String domainName = rs.getString("full_domain_name");
                        String status = rs.getString("status");
                        removedDomains.add(domainName + " (Trạng thái: " + status + ")");
                    }
                }
            }
            
            // Clean up unavailable domains from cart if any found
            if (!removedDomains.isEmpty()) {
                String cleanupCall = "{call CleanupUnavailableDomainsFromCart(?)}";
                try (java.sql.CallableStatement cleanupStmt = connection.prepareCall(cleanupCall)) {
                    cleanupStmt.setInt(1, userId);
                    cleanupStmt.executeQuery();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error validating cart: " + e.getMessage());
            e.printStackTrace();
        }
        
        return removedDomains;
    }

    /**
     * Log domain conflicts for admin review
     * 
     * @param domainName The domain name that caused conflict
     * @param domainExtension The domain extension
     * @param removedFromUserCount Number of users affected
     */
    private void logDomainConflict(String domainName, String domainExtension, int removedFromUserCount) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Create a simple log entry in the database for admin to review conflicts
            String insertLog = "INSERT INTO domain_conflict_log (domain_name, domain_extension, " +
                             "users_affected, conflict_date) VALUES (?, ?, ?, GETDATE())";
            
            try (java.sql.PreparedStatement stmt = connection.prepareStatement(insertLog)) {
                stmt.setString(1, domainName);
                stmt.setString(2, domainExtension);
                stmt.setInt(3, removedFromUserCount);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            // If log table doesn't exist, just print to console
            System.out.println("DOMAIN CONFLICT LOG: " + domainName + domainExtension + 
                             " removed from " + removedFromUserCount + " user(s) cart");
        }
    }
}