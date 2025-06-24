package service;

import model.Domain;
import model.Order;
import model.OrderDetails;
import model.Transaction;
import model.User;
import repository.DatabaseConnection;
import repository.DomainRepository;
import repository.OrderRepository;
import repository.OrderDetailsRepository;
import repository.TransactionRepository;
import repository.UserRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReportService {
    private Connection connection;
    private OrderRepository orderRepository;
    private OrderDetailsRepository orderDetailsRepository;
    private TransactionRepository transactionRepository;
    private DomainRepository domainRepository;
    private UserRepository userRepository;

    public ReportService() {
        try {
            this.connection = DatabaseConnection.getConnection();
            this.orderRepository = new OrderRepository(connection);
            this.orderDetailsRepository = new OrderDetailsRepository(connection);
            this.transactionRepository = new TransactionRepository(connection);
            this.domainRepository = new DomainRepository(connection);
            this.userRepository = new UserRepository(connection);
        } catch (SQLException e) {
            System.err.println("Error creating ReportService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // DTO classes for report data
    public static class RevenueData {
        private LocalDate date;
        private double revenue;
        private int orderCount;
        private int domainCount;

        public RevenueData(LocalDate date, double revenue, int orderCount, int domainCount) {
            this.date = date;
            this.revenue = revenue;
            this.orderCount = orderCount;
            this.domainCount = domainCount;
        }

        // Getters
        public LocalDate getDate() { return date; }
        public double getRevenue() { return revenue; }
        public int getOrderCount() { return orderCount; }
        public int getDomainCount() { return domainCount; }
        public double getConversionRate() { 
            return orderCount > 0 ? (double) domainCount / orderCount * 100 : 0; 
        }
    }

    public static class MetricData {
        private double totalRevenue;
        private double revenueChange;
        private int totalOrders;
        private double orderChange;
        private int totalDomains;
        private double domainChange;
        private int totalCustomers;
        private double customerChange;

        // Constructor
        public MetricData(double totalRevenue, double revenueChange, int totalOrders, 
                         double orderChange, int totalDomains, double domainChange,
                         int totalCustomers, double customerChange) {
            this.totalRevenue = totalRevenue;
            this.revenueChange = revenueChange;
            this.totalOrders = totalOrders;
            this.orderChange = orderChange;
            this.totalDomains = totalDomains;
            this.domainChange = domainChange;
            this.totalCustomers = totalCustomers;
            this.customerChange = customerChange;
        }

        // Getters
        public double getTotalRevenue() { return totalRevenue; }
        public double getRevenueChange() { return revenueChange; }
        public int getTotalOrders() { return totalOrders; }
        public double getOrderChange() { return orderChange; }
        public int getTotalDomains() { return totalDomains; }
        public double getDomainChange() { return domainChange; }
        public int getTotalCustomers() { return totalCustomers; }
        public double getCustomerChange() { return customerChange; }
    }

    // Get revenue data for a specific period
    public List<RevenueData> getRevenueData(int days) {
        List<RevenueData> revenueData = new ArrayList<>();
        String sql = """
            SELECT 
                CAST(o.created_at AS DATE) as order_date,
                SUM(o.total_price) as daily_revenue,
                COUNT(DISTINCT o.id) as order_count,
                COUNT(od.id) as domain_count
            FROM orders o
            LEFT JOIN order_details od ON o.id = od.order_id
            WHERE o.created_at >= DATEADD(day, -?, GETDATE())
                AND o.status IN ('Completed', 'Hoàn thành', 'Đã duyệt')
            GROUP BY CAST(o.created_at AS DATE)
            ORDER BY order_date ASC
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, days);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("order_date").toLocalDate();
                    double revenue = rs.getDouble("daily_revenue");
                    int orderCount = rs.getInt("order_count");
                    int domainCount = rs.getInt("domain_count");
                    
                    revenueData.add(new RevenueData(date, revenue, orderCount, domainCount));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting revenue data: " + e.getMessage());
            e.printStackTrace();
        }

        return revenueData;
    }

    // Get metrics with comparison to previous period
    public MetricData getMetricsData(int days) {
        try {
            // Current period metrics
            double currentRevenue = getTotalRevenue(days);
            int currentOrders = getTotalOrders(days);
            int currentDomains = getNewDomains(days);
            int currentCustomers = getNewCustomers(days);

            // Previous period metrics for comparison
            double previousRevenue = getTotalRevenuePeriod(days * 2, days);
            int previousOrders = getTotalOrdersPeriod(days * 2, days);
            int previousDomains = getNewDomainsPeriod(days * 2, days);
            int previousCustomers = getNewCustomersPeriod(days * 2, days);

            // Calculate percentage changes
            double revenueChange = calculatePercentageChange(previousRevenue, currentRevenue);
            double orderChange = calculatePercentageChange(previousOrders, currentOrders);
            double domainChange = calculatePercentageChange(previousDomains, currentDomains);
            double customerChange = calculatePercentageChange(previousCustomers, currentCustomers);

            return new MetricData(currentRevenue, revenueChange, currentOrders, orderChange,
                                currentDomains, domainChange, currentCustomers, customerChange);

        } catch (Exception e) {
            System.err.println("Error getting metrics data: " + e.getMessage());
            e.printStackTrace();
            return new MetricData(0, 0, 0, 0, 0, 0, 0, 0);
        }
    }

    // Get detailed table data
    public List<Map<String, Object>> getDetailedReportData(int days) {
        List<Map<String, Object>> tableData = new ArrayList<>();
        String sql = """
            SELECT 
                CAST(o.created_at AS DATE) as order_date,
                COUNT(DISTINCT o.id) as order_count,
                SUM(o.total_price) as daily_revenue,
                COUNT(od.id) as domain_count,
                CASE 
                    WHEN COUNT(DISTINCT o.id) > 0 
                    THEN CAST(COUNT(od.id) * 100.0 / COUNT(DISTINCT o.id) AS DECIMAL(5,1))
                    ELSE 0 
                END as conversion_rate
            FROM orders o
            LEFT JOIN order_details od ON o.id = od.order_id
            WHERE o.created_at >= DATEADD(day, -?, GETDATE())
                AND o.status IN ('Completed', 'Hoàn thành', 'Đã duyệt')
            GROUP BY CAST(o.created_at AS DATE)
            ORDER BY order_date DESC
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, days);
            try (ResultSet rs = stmt.executeQuery()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    
                    LocalDate date = rs.getDate("order_date").toLocalDate();
                    row.put("date", date.format(formatter));
                    row.put("orders", rs.getInt("order_count"));
                    row.put("revenue", formatCurrency(rs.getDouble("daily_revenue")));
                    row.put("domains", rs.getInt("domain_count"));
                    row.put("rate", rs.getDouble("conversion_rate") + "%");
                    
                    tableData.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting detailed report data: " + e.getMessage());
            e.printStackTrace();
        }

        return tableData;
    }

    // Helper methods for metrics calculation
    private double getTotalRevenue(int days) {
        String sql = "SELECT ISNULL(SUM(total_price), 0) FROM orders WHERE created_at >= DATEADD(day, -?, GETDATE()) AND status IN ('Completed', 'Hoàn thành', 'Đã duyệt')";
        return executeScalarDoubleQuery(sql, days);
    }

    private double getTotalRevenuePeriod(int startDays, int endDays) {
        String sql = "SELECT ISNULL(SUM(total_price), 0) FROM orders WHERE created_at >= DATEADD(day, -?, GETDATE()) AND created_at < DATEADD(day, -?, GETDATE()) AND status IN ('Completed', 'Hoàn thành', 'Đã duyệt')";
        return executeScalarDoubleQuery(sql, startDays, endDays);
    }

    private int getTotalOrders(int days) {
        String sql = "SELECT COUNT(*) FROM orders WHERE created_at >= DATEADD(day, -?, GETDATE()) AND status IN ('Completed', 'Hoàn thành', 'Đã duyệt')";
        return executeScalarIntQuery(sql, days);
    }

    private int getTotalOrdersPeriod(int startDays, int endDays) {
        String sql = "SELECT COUNT(*) FROM orders WHERE created_at >= DATEADD(day, -?, GETDATE()) AND created_at < DATEADD(day, -?, GETDATE()) AND status IN ('Completed', 'Hoàn thành', 'Đã duyệt')";
        return executeScalarIntQuery(sql, startDays, endDays);
    }

    private int getNewDomains(int days) {
        String sql = "SELECT COUNT(DISTINCT od.domain_id) FROM order_details od JOIN orders o ON od.order_id = o.id WHERE o.created_at >= DATEADD(day, -?, GETDATE()) AND o.status IN ('Completed', 'Hoàn thành', 'Đã duyệt')";
        return executeScalarIntQuery(sql, days);
    }

    private int getNewDomainsPeriod(int startDays, int endDays) {
        String sql = "SELECT COUNT(DISTINCT od.domain_id) FROM order_details od JOIN orders o ON od.order_id = o.id WHERE o.created_at >= DATEADD(day, -?, GETDATE()) AND o.created_at < DATEADD(day, -?, GETDATE()) AND o.status IN ('Completed', 'Hoàn thành', 'Đã duyệt')";
        return executeScalarIntQuery(sql, startDays, endDays);
    }

    private int getNewCustomers(int days) {
        String sql = "SELECT COUNT(DISTINCT buyer_id) FROM orders WHERE created_at >= DATEADD(day, -?, GETDATE()) AND status IN ('Completed', 'Hoàn thành', 'Đã duyệt')";
        return executeScalarIntQuery(sql, days);
    }

    private int getNewCustomersPeriod(int startDays, int endDays) {
        String sql = "SELECT COUNT(DISTINCT buyer_id) FROM orders WHERE created_at >= DATEADD(day, -?, GETDATE()) AND created_at < DATEADD(day, -?, GETDATE()) AND status IN ('Completed', 'Hoàn thành', 'Đã duyệt')";
        return executeScalarIntQuery(sql, startDays, endDays);
    }

    // Helper methods for database queries
    private double executeScalarDoubleQuery(String sql, int... params) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setInt(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing scalar double query: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    private int executeScalarIntQuery(String sql, int... params) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setInt(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing scalar int query: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // Utility methods
    private double calculatePercentageChange(double oldValue, double newValue) {
        if (oldValue == 0) {
            return newValue > 0 ? 100.0 : 0.0;
        }
        return ((newValue - oldValue) / oldValue) * 100.0;
    }

    private String formatCurrency(double amount) {
        return String.format("%,.0f ₫", amount);
    }

    // Get chart data for different periods
    public Map<String, Object> getChartData(String period, String reportType) {
        Map<String, Object> chartData = new HashMap<>();
        
        int days = getPeriodDays(period);
        
        switch (reportType.toLowerCase()) {
            case "doanh thu":
                chartData = getRevenueChartData(days);
                break;
            case "đơn hàng":
                chartData = getOrderChartData(days);
                break;
            case "tên miền":
                chartData = getDomainChartData(days);
                break;
            case "khách hàng":
                chartData = getCustomerChartData(days);
                break;
            default:
                chartData = getRevenueChartData(days);
        }
        
        return chartData;
    }

    private Map<String, Object> getRevenueChartData(int days) {
        Map<String, Object> data = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        
        List<RevenueData> revenueData = getRevenueData(days);
        
        for (RevenueData rd : revenueData) {
            if (days <= 7) {
                labels.add(rd.getDate().format(DateTimeFormatter.ofPattern("dd/MM")));
            } else if (days <= 30) {
                labels.add(rd.getDate().format(DateTimeFormatter.ofPattern("dd/MM")));
            } else {
                labels.add(rd.getDate().format(DateTimeFormatter.ofPattern("MM/yyyy")));
            }
            values.add(rd.getRevenue());
        }
        
        data.put("labels", labels);
        data.put("values", values);
        data.put("title", "Doanh thu " + days + " ngày qua");
        
        return data;
    }

    private Map<String, Object> getOrderChartData(int days) {
        Map<String, Object> data = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        
        List<RevenueData> revenueData = getRevenueData(days);
        
        for (RevenueData rd : revenueData) {
            if (days <= 7) {
                labels.add(rd.getDate().format(DateTimeFormatter.ofPattern("dd/MM")));
            } else {
                labels.add(rd.getDate().format(DateTimeFormatter.ofPattern("dd/MM")));
            }
            values.add(rd.getOrderCount());
        }
        
        data.put("labels", labels);
        data.put("values", values);
        data.put("title", "Đơn hàng " + days + " ngày qua");
        
        return data;
    }

    private Map<String, Object> getDomainChartData(int days) {
        Map<String, Object> data = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        
        List<RevenueData> revenueData = getRevenueData(days);
        
        for (RevenueData rd : revenueData) {
            labels.add(rd.getDate().format(DateTimeFormatter.ofPattern("dd/MM")));
            values.add(rd.getDomainCount());
        }
        
        data.put("labels", labels);
        data.put("values", values);
        data.put("title", "Tên miền mới " + days + " ngày qua");
        
        return data;
    }

    private Map<String, Object> getCustomerChartData(int days) {
        Map<String, Object> data = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        
        // Get daily customer registrations
        String sql = """
            SELECT 
                CAST(o.created_at AS DATE) as order_date,
                COUNT(DISTINCT o.buyer_id) as daily_customers
            FROM orders o
            WHERE o.created_at >= DATEADD(day, -?, GETDATE())
            GROUP BY CAST(o.created_at AS DATE)
            ORDER BY order_date ASC
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, days);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("order_date").toLocalDate();
                    int customers = rs.getInt("daily_customers");
                    
                    labels.add(date.format(DateTimeFormatter.ofPattern("dd/MM")));
                    values.add(customers);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting customer chart data: " + e.getMessage());
            e.printStackTrace();
        }
        
        data.put("labels", labels);
        data.put("values", values);
        data.put("title", "Khách hàng " + days + " ngày qua");
        
        return data;
    }

    private int getPeriodDays(String period) {
        switch (period) {
            case "7 ngày qua": return 7;
            case "30 ngày qua": return 30;
            case "3 tháng qua": return 90;
            case "6 tháng qua": return 180;
            case "1 năm qua": return 365;
            default: return 7;
        }
    }

    // Export report data to file (for future implementation)
    public boolean exportReport(String period, String reportType, String filePath) {
        // TODO: Implement export functionality
        return true;
    }

    // Get top performing domains
    public List<Map<String, Object>> getTopDomains(int limit) {
        List<Map<String, Object>> topDomains = new ArrayList<>();
        String sql = """
            SELECT TOP (?) 
                CONCAT(od.domain_name, od.domain_extension) as full_domain,
                COUNT(od.id) as purchase_count,
                SUM(od.price) as total_revenue,
                AVG(od.price) as avg_price
            FROM order_details od
            JOIN orders o ON od.order_id = o.id
            WHERE o.status IN ('Completed', 'Hoàn thành', 'Đã duyệt')
            GROUP BY od.domain_name, od.domain_extension
            ORDER BY total_revenue DESC
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> domain = new HashMap<>();
                    domain.put("name", rs.getString("full_domain"));
                    domain.put("count", rs.getInt("purchase_count"));
                    domain.put("revenue", formatCurrency(rs.getDouble("total_revenue")));
                    domain.put("avgPrice", formatCurrency(rs.getDouble("avg_price")));
                    topDomains.add(domain);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting top domains: " + e.getMessage());
            e.printStackTrace();
        }

        return topDomains;
    }
} 