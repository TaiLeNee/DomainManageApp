package service;

import model.Domain;
import model.Order;
import model.OrderDetails;
import model.RentalPeriod;
import repository.DomainRepository;
import repository.OrderDetailsRepository;
import repository.OrderRepository;
import repository.RentalPeriodRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class OrderDetailsService {
    private final OrderDetailsRepository orderDetailsRepository;
    private final DomainRepository domainRepository;
    private final OrderRepository orderRepository;
    private final RentalPeriodRepository rentalPeriodRepository;

    public OrderDetailsService() {
        this.orderDetailsRepository = new OrderDetailsRepository();
        this.domainRepository = new DomainRepository();
        this.orderRepository = new OrderRepository();
        this.rentalPeriodRepository = new RentalPeriodRepository();

        // Ensure the order_details table exists
        orderDetailsRepository.createTableIfNotExists();
    }

    // Create order details for a domain purchase
    public boolean createOrderDetail(int orderId, int domainId, String status) {
        try {
            // Get domain information
            Domain domain = domainRepository.getDomainById(domainId);
            // Get order information for rental period
            Order order = orderRepository.getOrderById(orderId);

            if (domain != null && order != null) {
                OrderDetails orderDetails = new OrderDetails();
                orderDetails.setOrderId(orderId);
                orderDetails.setDomainId(domainId);
                orderDetails.setDomainName(domain.getName());
                orderDetails.setDomainExtension(domain.getExtension());

                // Try to get rental period info
                int rentalPeriodId = order.getRentalPeriodId();
                orderDetails.setRentalPeriodId(rentalPeriodId);

                // Calculate prices based on rental period
                try {
                    Optional<RentalPeriod> periodOpt = rentalPeriodRepository.findById(rentalPeriodId);
                    if (periodOpt.isPresent()) {
                        RentalPeriod period = periodOpt.get();
                        double originalPrice = domain.getPrice() * period.getMonths();
                        double discountedPrice = originalPrice * (1 - period.getDiscount());

                        orderDetails.setOriginalPrice(originalPrice);
                        orderDetails.setPrice(discountedPrice);
                        orderDetails.setExpiryDate(LocalDateTime.now().plusMonths(period.getMonths()));
                    } else {
                        // Fallback if no rental period found
                        orderDetails.setPrice(domain.getPrice());
                        orderDetails.setOriginalPrice(domain.getPrice());
                    }
                } catch (SQLException e) {
                    // Fallback on error
                    orderDetails.setPrice(domain.getPrice());
                    orderDetails.setOriginalPrice(domain.getPrice());
                }

                orderDetails.setPurchaseDate(LocalDateTime.now());
                orderDetails.setStatus(status);

                return orderDetailsRepository.insert(orderDetails);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo chi tiết đơn hàng: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Overloaded method that takes a complete OrderDetails object
    public boolean createOrderDetail(OrderDetails orderDetails) {
        return orderDetailsRepository.insert(orderDetails);
    }

    // Get all order details for a specific order
    public List<OrderDetails> getOrderDetailsByOrderId(int orderId) {
        return orderDetailsRepository.findByOrderId(orderId);
    }

    // Get all order details for a specific user
    public List<OrderDetails> getOrderDetailsByUserId(int userId) {
        return orderDetailsRepository.findByUserId(userId);
    }

    // Get all order details for a specific domain
    public List<OrderDetails> getOrderDetailsByDomainId(int domainId) {
        return orderDetailsRepository.findByDomainId(domainId);
    }

    // Update status of an order detail
    public boolean updateOrderDetailStatus(int orderDetailId, String status) {
        try {
            OrderDetails details = orderDetailsRepository.getOrderDetailById(orderDetailId);
            if (details != null) {
                details.setStatus(status);
                return orderDetailsRepository.update(details);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật trạng thái chi tiết đơn hàng: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}