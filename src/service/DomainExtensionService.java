package service;

import model.Domain;
import repository.DatabaseConnection;
import repository.DomainRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class DomainExtensionService {
    // Danh sách các phần mở rộng tên miền phổ biến
    private static final String[] POPULAR_EXTENSIONS = {
            ".com", ".net", ".org", ".vn", ".com.vn", ".info", ".biz", ".store"
    };

    // Giá mặc định cho từng loại phần mở rộng
    private static final Map<String, Double> DEFAULT_PRICES = new HashMap<>();
    static {
        DEFAULT_PRICES.put(".com", 200000.0);
        DEFAULT_PRICES.put(".net", 150000.0);
        DEFAULT_PRICES.put(".org", 180000.0);
        DEFAULT_PRICES.put(".vn", 400000.0);
        DEFAULT_PRICES.put(".com.vn", 350000.0);
        DEFAULT_PRICES.put(".info", 120000.0);
        DEFAULT_PRICES.put(".biz", 130000.0);
        DEFAULT_PRICES.put(".store", 250000.0);
    }

    private DomainRepository domainRepository;

    public DomainExtensionService(Connection connection) {
        this.domainRepository = new DomainRepository(connection);
    }

    // Constructor mặc định để AdminDashboardView có thể khởi tạo
    public DomainExtensionService() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            this.domainRepository = new DomainRepository(connection);
        } catch (SQLException e) {
            System.err.println("Error creating DomainExtensionService: " + e.getMessage());
            e.printStackTrace();

            // Sử dụng repository mặc định nếu không thể kết nối
            this.domainRepository = new DomainRepository();
        }
    }

    // Phương thức được sử dụng bởi AdminDashboardView
    public List<String> getAllExtensions() {
        return Arrays.asList(POPULAR_EXTENSIONS);
    }

    public double getDefaultPrice(String extension) {
        return DEFAULT_PRICES.getOrDefault(extension, 100000.0); // Giá mặc định nếu không tìm thấy
    }

    // Phương thức tạo danh sách tất cả các tên miền với mọi phần mở rộng
    public static List<Domain> generateDomainsWithAllExtensions(String domainName) {
        List<Domain> domains = new ArrayList<>();

        for (String extension : POPULAR_EXTENSIONS) {
            double price = DEFAULT_PRICES.getOrDefault(extension, 100000.0);
            Domain domain = new Domain();
            domain.setName(domainName);
            domain.setExtension(extension);
            domain.setPrice(price);
            domain.setStatus("Khả dụng"); // Mặc định là khả dụng

            domains.add(domain);
        }

        return domains;
    }

    // Kiểm tra xem tên miền có khả dụng không
    public boolean isDomainAvailable(String name, String extension) {
        try {
            return !domainRepository.existsByNameAndExtension(name, extension);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Lấy các phần mở rộng phổ biến nhất
    public List<String> getPopularExtensions() {
        return Arrays.asList(".com", ".net", ".org", ".vn");
    }
}