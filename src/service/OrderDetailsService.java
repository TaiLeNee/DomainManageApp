package service;

import model.Domain;
import model.Order;
import model.OrderDetails;
import repository.DomainRepository;
import repository.OrderDetailsRepository;
import repository.OrderRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDetailsService {
    private final OrderDetailsRepository orderDetailsRepository;
    private final DomainRepository domainRepository;
    private final OrderRepository orderRepository;

    public OrderDetailsService() {
        this.orderDetailsRepository = new OrderDetailsRepository();
        this.domainRepository = new DomainRepository();
        this.orderRepository = new OrderRepository();

        // Ensure the order_details table exists
        orderDetailsRepository.createTableIfNotExists();
    }

    // Create order details for a domain purchase
    public boolean createOrderDetail(int orderId, int domainId, String status) {
        try {
            // Get domain information
            Domain domain = domainRepository.getDomainById(domainId);

            if (domain != null) {
                OrderDetails orderDetails = new OrderDetails();
                orderDetails.setOrderId(orderId);
                orderDetails.setDomainId(domainId);
                orderDetails.setDomainName(domain.getName());
                orderDetails.setDomainExtension(domain.getExtension());
                orderDetails.setPrice(domain.getPrice());
                orderDetails.setPurchaseDate(LocalDateTime.now());
                orderDetails.setStatus(status);

                return orderDetailsRepository.insert(orderDetails);
            }
        } catch (Exception e) {
            System.err.println("Error creating order detail: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
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
            OrderDetails details = orderDetailsRepository.findByOrderId(orderDetailId).stream()
                    .filter(d -> d.getId() == orderDetailId)
                    .findFirst()
                    .orElse(null);

            if (details != null) {
                details.setStatus(status);
                return orderDetailsRepository.update(details);
            }
        } catch (Exception e) {
            System.err.println("Error updating order detail status: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}