package backEnd.service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class DomainExtensionService {
    private static final Map<String, Double> extensionPrices = new HashMap<>();

    static {
        // Khởi tạo giá cho các đuôi tên miền phổ biến
        extensionPrices.put(".com", 12.99);
        extensionPrices.put(".net", 11.99);
        extensionPrices.put(".org", 10.99);
        extensionPrices.put(".vn", 20.00);
        extensionPrices.put(".io", 39.99);
        extensionPrices.put(".co", 25.99);
        extensionPrices.put(".info", 8.99);
    }

    // Lấy giá của một đuôi tên miền
    public static double getExtensionPrice(String extension) {
        return extensionPrices.getOrDefault(extension, 0.0);
    }

    // Lấy tất cả các đuôi tên miền
    public static List<String> getAllExtensions() {
        return new ArrayList<>(extensionPrices.keySet());
    }

    // Thêm hoặc cập nhật đuôi tên miền mới
    public static void setExtensionPrice(String extension, double price) {
        extensionPrices.put(extension, price);
    }

    // Tạo danh sách các domain với tất cả các đuôi tên miền cho một tên nhất định
    public static List<entity.Domain> generateDomainsWithAllExtensions(String domainName) {
        List<entity.Domain> domains = new ArrayList<>();

        for (Map.Entry<String, Double> entry : extensionPrices.entrySet()) {
            String extension = entry.getKey();
            double price = entry.getValue();

            entity.Domain domain = new entity.Domain();
            domain.setName(domainName);
            domain.setExtension(extension);
            domain.setPrice(price);
            domain.setStatus("Available");

            domains.add(domain);
        }

        return domains;
    }
}