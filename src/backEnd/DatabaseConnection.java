package backEnd;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static Properties properties;

    static {
        properties = new Properties();
        try {
            // Đường dẫn đến file cấu hình
            FileInputStream fis = new FileInputStream("src/resource/database.properties");
            properties.load(fis);
            fis.close();

            // Tải driver JDBC
            Class.forName(properties.getProperty("driver"));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty("url").replace("yourDatabaseName", "QuanLyTenMien"),
                properties.getProperty("username"),
                properties.getProperty("password")
        );
    }
}