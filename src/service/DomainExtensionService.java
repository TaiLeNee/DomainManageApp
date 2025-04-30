package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Domain;
import repository.DatabaseConnection;
import repository.DomainExtensionRepository;
import repository.DomainRepository;

public class DomainExtensionService {
    // Danh sách các phần mở rộng tên miền phổ biến
    private static final String[] POPULAR_EXTENSIONS = {
            ".com", ".net", ".org", ".vn", ".com.vn", ".info", ".biz", ".store"
    };

    private DomainRepository domainRepository;
    private DomainExtensionRepository extensionRepository;

    public DomainExtensionService(Connection connection) {
        this.domainRepository = new DomainRepository(connection);
        this.extensionRepository = new DomainExtensionRepository(connection);
    }

    // Constructor mặc định để AdminDashboardView có thể khởi tạo
    public DomainExtensionService() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            this.domainRepository = new DomainRepository(connection);
            this.extensionRepository = new DomainExtensionRepository(connection);
        } catch (SQLException e) {
            System.err.println("Error creating DomainExtensionService: " + e.getMessage());
            e.printStackTrace();

            // Sử dụng repository mặc định nếu không thể kết nối
            this.domainRepository = new DomainRepository();
            this.extensionRepository = new DomainExtensionRepository();
        }
    }

    // Phương thức được sử dụng bởi AdminDashboardView
    public List<String> getAllExtensions() {
        try {
            return extensionRepository.getAllExtensions();
        } catch (SQLException e) {
            System.err.println("Error getting extensions from database: " + e.getMessage());
            e.printStackTrace();
            return Arrays.asList(POPULAR_EXTENSIONS); // Fallback to hardcoded list if DB fails
        }
    }

    public double getDefaultPrice(String extension) {
        try {
            return extensionRepository.getDefaultPrice(extension);
        } catch (SQLException e) {
            System.err.println("Error getting default price from database: " + e.getMessage());
            e.printStackTrace();
            return 100000.0; // Giá mặc định nếu không thể lấy từ CSDL
        }
    }

    // Phương thức tạo danh sách tất cả các tên miền với mọi phần mở rộng
    public static List<Domain> generateDomainsWithAllExtensions(String domainName) {
        List<Domain> domains = new ArrayList<>();
        DomainExtensionService service = new DomainExtensionService();
        List<String> extensions;
        Map<String, Double> priceMap = new HashMap<>();

        try {
            // Lấy tất cả các phần mở rộng từ CSDL
            DomainExtensionRepository repo = new DomainExtensionRepository();
            extensions = repo.getAllExtensions();
            priceMap = repo.getAllExtensionsWithPrices();
        } catch (SQLException e) {
            System.err.println("Error getting extensions from database: " + e.getMessage());
            e.printStackTrace();

            // Fallback to hardcoded extensions if database access fails
            extensions = Arrays.asList(POPULAR_EXTENSIONS);
        }

        for (String extension : extensions) {
            double price = priceMap.getOrDefault(extension, 100000.0);
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

    // Thêm hoặc cập nhật một phần mở rộng
    public boolean saveExtension(String extension, double price, String description) {
        try {
            extensionRepository.saveExtension(extension, price, description);
            return true;
        } catch (SQLException e) {
            System.err.println("Error saving extension: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Xóa một phần mở rộng
    public boolean deleteExtension(String extension) {
        try {
            extensionRepository.deleteExtension(extension);
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting extension: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<String[]> getAllDomainExtensions() {
        try {
            return extensionRepository.getAllDomainExtensions();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<String[]> searchDomainWithExtensions(String domainName) {
        try {
            return extensionRepository.searchDomainWithExtensions(domainName);
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}