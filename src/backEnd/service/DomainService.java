package backEnd.service;

import entity.Domain;
import entity.Order;
import entity.RentalPeriod;
import backEnd.dao.DomainDAO;
import backEnd.dao.OrderDAO;
import backEnd.dao.RentalPeriodDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class DomainService {
    private final DomainDAO domainDAO;
    private final OrderDAO orderDAO;
    private final RentalPeriodDAO rentalPeriodDAO;

    public DomainService(Connection connection) {
        this.domainDAO = new DomainDAO(connection);
        this.orderDAO = new OrderDAO(connection);
        this.rentalPeriodDAO = new RentalPeriodDAO(connection);
    }

    // Phương thức để lấy tất cả các gói thuê
    public List<RentalPeriod> getAllRentalPeriods() throws SQLException {
        return rentalPeriodDAO.getAllRentalPeriods();
    }

    // Phương thức để tạo một đơn hàng thuê tên miền
    public Order createRentalOrder(int userId, int domainId, int rentalPeriodId) throws SQLException {
        // Lấy thông tin tên miền
        Domain domain = domainDAO.getDomainById(domainId);
        if (domain == null || !domain.getStatus().equalsIgnoreCase("Available")) {
            throw new IllegalArgumentException("Tên miền không khả dụng");
        }

        // Lấy thông tin gói thuê
        RentalPeriod period = rentalPeriodDAO.getRentalPeriodById(rentalPeriodId);
        if (period == null) {
            throw new IllegalArgumentException("Gói thuê không hợp lệ");
        }

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
        orderDAO.addOrder(order);

        // Cập nhật trạng thái tên miền
        domain.setStatus("Reserved");
        domainDAO.updateDomain(domain);

        return order;
    }

    // Phương thức để hiển thị các lựa chọn thuê cho tên miền
    public List<Domain> getDomainRentalOptions(String domainName) throws SQLException {
        List<Domain> options = DomainExtensionService.generateDomainsWithAllExtensions(domainName);

        // Kiểm tra xem tên miền nào đã được thuê
        for (Domain option : options) {
            if (domainDAO.isDomainExists(option.getName(), option.getExtension())) {
                Domain existingDomain = domainDAO.getDomainByNameAndExtension(option.getName(), option.getExtension());
                if (existingDomain != null) {
                    option.setId(existingDomain.getId());
                    option.setStatus(existingDomain.getStatus());
                    option.setExpiryDate(existingDomain.getExpiryDate());
                }
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