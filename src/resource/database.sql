-- Kiểm tra và xóa database nếu đã tồn tại
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'QuanLyTenMien')
BEGIN
    USE master;
    ALTER DATABASE QuanLyTenMien SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QuanLyTenMien;
END
GO

-- Tạo database
CREATE DATABASE QuanLyTenMien;
GO

USE QuanLyTenMien;
GO

-- Bảng users
CREATE TABLE users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    fullname NVARCHAR(100) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL
);
GO

-- Bảng domains
CREATE TABLE domains (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    extension VARCHAR(20) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    status NVARCHAR(20) NOT NULL, 
    expiry_date DATETIME NULL,
    CONSTRAINT UQ_domain_name_extension UNIQUE (name, extension)
);
GO

-- Bảng domain_extensions
CREATE TABLE domain_extensions (
    id INT IDENTITY(1,1) PRIMARY KEY,
    extension VARCHAR(20) UNIQUE NOT NULL,
    default_price DECIMAL(10, 2) NOT NULL,
    description NVARCHAR(255) NULL
);
GO

-- Bảng rental_periods
CREATE TABLE rental_periods (
    id INT IDENTITY(1,1) PRIMARY KEY,
    months INT NOT NULL,
    discount DECIMAL(5, 2) NOT NULL,
    description NVARCHAR(255) NOT NULL,
    CONSTRAINT UQ_rental_period_months UNIQUE (months)
);
GO

-- Bảng orders
CREATE TABLE orders (
    id INT IDENTITY(1,1) PRIMARY KEY,
    buyer_id INT NOT NULL,
    domain_id INT NOT NULL,
    rental_period_id INT NOT NULL,
    status NVARCHAR(20) NOT NULL, 
    created_at DATETIME DEFAULT GETDATE(),
    expiry_date DATETIME NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (buyer_id) REFERENCES users(id),
    FOREIGN KEY (domain_id) REFERENCES domains(id),
    FOREIGN KEY (rental_period_id) REFERENCES rental_periods(id)
);
GO

-- Bảng order_details - Lưu trữ giá riêng của từng tên miền trong đơn hàng
CREATE TABLE order_details (
    id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    domain_id INT NOT NULL,
    domain_name VARCHAR(100) NOT NULL,
    domain_extension VARCHAR(20) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    purchase_date DATETIME DEFAULT GETDATE(),
    expiry_date DATETIME NULL,
    rental_period_id INT NULL,
    status NVARCHAR(20) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (domain_id) REFERENCES domains(id),
    FOREIGN KEY (rental_period_id) REFERENCES rental_periods(id)
);
GO

-- Bảng transactions
CREATE TABLE transactions (
    id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    domain_id INT NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    timestamp DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (domain_id) REFERENCES domains(id)
);
GO

-- Bảng cart	
CREATE TABLE cart (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    domain_id INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    rental_period_id INT NULL,
    discounted_price DECIMAL(10, 2) NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (domain_id) REFERENCES domains(id),
    FOREIGN KEY (rental_period_id) REFERENCES rental_periods(id)
);
GO

-- Thêm dữ liệu mẫu

-- Thêm người dùng
INSERT INTO users (fullname, username, password, email, role)
VALUES
    (N'Lê Công Tài', '1', '1', 'admin@domain.com', 'admin'),
    (N'Nguyễn Văn A', '2', '2', 'user1@domain.com', 'user');

-- Thêm dữ liệu cho bảng domain_extensions
INSERT INTO domain_extensions (extension, default_price, description)
VALUES
    ('.com', 200000.0, N'Phần mở rộng phổ biến nhất cho trang web thương mại'),
    ('.net', 150000.0, N'Phù hợp cho các trang web về công nghệ và mạng'),
    ('.org', 180000.0, N'Dành cho các tổ chức phi lợi nhuận'),
    ('.vn', 400000.0, N'Tên miền quốc gia Việt Nam'),
    ('.com.vn', 350000.0, N'Tên miền thương mại Việt Nam'),
    ('.info', 120000.0, N'Dành cho các trang web thông tin'),
    ('.biz', 130000.0, N'Dành cho các trang web kinh doanh'),
    ('.store', 250000.0, N'Phù hợp cho các cửa hàng trực tuyến');
GO

-- Thêm dữ liệu cho bảng rental_periods
INSERT INTO rental_periods (months, discount, description)
VALUES
    (1, 0.00, N'1 tháng - Không giảm giá'),
    (3, 0.05, N'3 tháng - Giảm 5%'),
    (6, 0.10, N'6 tháng - Giảm 10%'),
    (12, 0.20, N'12 tháng - Giảm 20%'),
    (24, 0.30, N'24 tháng - Giảm 30%');
GO

-- Thủ tục tìm kiếm domain theo tên
CREATE PROCEDURE SearchDomains
    @searchTerm VARCHAR(100)
AS
BEGIN
    SELECT * FROM domains
    WHERE name LIKE '%' + @searchTerm + '%'
    ORDER BY name, extension;
END;
GO

-- Thủ tục lấy danh sách domain hết hạn
CREATE PROCEDURE GetExpiredDomains
AS
BEGIN
    SELECT * FROM domains
    WHERE status = 'Rented' AND expiry_date < GETDATE();
END;
GO

-- Thủ tục lấy danh sách domain sắp hết hạn
CREATE PROCEDURE GetExpiringDomains
    @daysThreshold INT = 30,
    @limit INT = 10
AS
BEGIN
    SELECT TOP (@limit) 
        d.id, 
        d.name, 
        d.extension, 
        d.price, 
        d.status, 
        d.expiry_date,
        DATEDIFF(day, GETDATE(), d.expiry_date) as days_remaining
    FROM domains d
    WHERE d.status = 'Rented'
        AND d.expiry_date IS NOT NULL
        AND d.expiry_date > GETDATE()
        AND DATEDIFF(day, GETDATE(), d.expiry_date) <= @daysThreshold
    ORDER BY days_remaining ASC;
END;
GO

-- Thủ tục lấy danh sách domain của người dùng
CREATE PROCEDURE GetUserDomains
    @userId INT
AS
BEGIN
    SELECT
        o.id as order_id,
        CONCAT(d.name, d.extension) as domain_name,
        od.price as price,  -- Sử dụng giá từ order_details thay vì từ orders
        od.purchase_date as purchase_date,
        od.expiry_date as expiry_date,
        o.status,
        rp.months as rental_period,
        rp.description as rental_period_description
    FROM orders o
    JOIN domains d ON o.domain_id = d.id
    JOIN rental_periods rp ON o.rental_period_id = rp.id
    LEFT JOIN order_details od ON o.id = od.order_id AND o.domain_id = od.domain_id
    WHERE o.buyer_id = @userId
    ORDER BY o.created_at DESC;
END;
GO

-- Thủ tục đặt hàng domain
CREATE PROCEDURE CreateDomainOrder
    @buyerId INT,
    @domainId INT,
    @rentalPeriodId INT
AS
BEGIN
    DECLARE @domainPrice DECIMAL(10, 2);
    DECLARE @discount DECIMAL(5, 2);
    DECLARE @months INT;
    DECLARE @totalPrice DECIMAL(10, 2);
    DECLARE @expiryDate DATETIME;
    DECLARE @orderId INT;
    DECLARE @domainName VARCHAR(100);
    DECLARE @domainExtension VARCHAR(20);

    -- Lấy thông tin giá và thời gian
    SELECT @domainPrice = price, @domainName = name, @domainExtension = extension FROM domains WHERE id = @domainId;
    SELECT @discount = discount, @months = months FROM rental_periods WHERE id = @rentalPeriodId;

    -- Tính tổng tiền và ngày hết hạn
    SET @totalPrice = @domainPrice * @months * (1 - @discount);
    SET @expiryDate = DATEADD(month, @months, GETDATE());

    -- Tạo đơn hàng
    INSERT INTO orders (buyer_id, domain_id, rental_period_id, status, created_at, expiry_date, total_price)
    VALUES (@buyerId, @domainId, @rentalPeriodId, 'Pending', GETDATE(), @expiryDate, @totalPrice);

    SET @orderId = SCOPE_IDENTITY();

    -- Tạo chi tiết đơn hàng với thông tin thời gian thuê
    INSERT INTO order_details (order_id, domain_id, domain_name, domain_extension, price, purchase_date, expiry_date, rental_period_id, status)
    VALUES (@orderId, @domainId, @domainName, @domainExtension, @totalPrice, GETDATE(), @expiryDate, @rentalPeriodId, 'Pending');

    -- Cập nhật trạng thái domain
    UPDATE domains
    SET status = 'Rented', expiry_date = @expiryDate
    WHERE id = @domainId;

    -- Trả về ID của đơn hàng vừa tạo
    SELECT @orderId AS OrderId;
END;
GO

-- Thêm thủ tục lấy giá mặc định của phần mở rộng
CREATE PROCEDURE GetExtensionDefaultPrice
    @extension VARCHAR(20)
AS
BEGIN
    SELECT default_price FROM domain_extensions WHERE extension = @extension;
END;
GO

-- Thêm thủ tục lấy chi tiết đơn hàng theo order_id
CREATE PROCEDURE GetOrderDetails
    @orderId INT
AS
BEGIN
    SELECT 
        od.id,
        od.order_id,
        od.domain_id,
        od.domain_name,
        od.domain_extension,
        od.price,
        od.purchase_date,
        od.expiry_date,
        od.rental_period_id,
        rp.description as rental_period_description,
        rp.months as rental_months,
        od.status
    FROM order_details od
    LEFT JOIN rental_periods rp ON od.rental_period_id = rp.id
    WHERE od.order_id = @orderId;
END;
GO

-- Thêm thủ tục xử lý đơn hàng từ giỏ hàng
CREATE PROCEDURE ProcessCartOrder
    @userId INT,
    @status NVARCHAR(20) = 'Pending'
AS
BEGIN
    -- Kiểm tra xem giỏ hàng có rỗng không
    IF NOT EXISTS (SELECT 1 FROM cart WHERE user_id = @userId)
    BEGIN
        RETURN -1; -- Giỏ hàng rỗng
    END
    
    -- Biến để lưu ID của đơn hàng mới
    DECLARE @orderId INT;
    DECLARE @totalOrderPrice DECIMAL(10, 2) = 0;
    
    -- Bước 1: Tạo đơn hàng mới
    INSERT INTO orders (buyer_id, domain_id, rental_period_id, status, created_at, expiry_date, total_price)
    SELECT TOP 1 
        @userId, 
        c.domain_id, 
        c.rental_period_id, 
        @status, 
        GETDATE(), 
        DATEADD(month, rp.months, GETDATE()), 
        0 -- Tạm thời đặt là 0, sẽ cập nhật sau
    FROM cart c
    JOIN rental_periods rp ON c.rental_period_id = rp.id
    WHERE c.user_id = @userId;
    
    -- Lấy ID của đơn hàng vừa tạo
    SET @orderId = SCOPE_IDENTITY();
    
    -- Bước 2: Chèn chi tiết đơn hàng từ giỏ hàng
    INSERT INTO order_details 
        (order_id, domain_id, domain_name, domain_extension, price, purchase_date, expiry_date, rental_period_id, status)
    SELECT 
        @orderId,
        d.id,
        d.name,
        d.extension,
        ISNULL(c.discounted_price, c.price),
        GETDATE(),
        DATEADD(month, rp.months, GETDATE()),
        c.rental_period_id,
        @status
    FROM cart c
    JOIN domains d ON c.domain_id = d.id
    JOIN rental_periods rp ON c.rental_period_id = rp.id
    WHERE c.user_id = @userId;
    
    -- Bước 3: Tính tổng giá trị đơn hàng và cập nhật
    SELECT @totalOrderPrice = SUM(ISNULL(discounted_price, price))
    FROM cart
    WHERE user_id = @userId;
    
    -- Cập nhật tổng giá trị đơn hàng
    UPDATE orders
    SET total_price = @totalOrderPrice
    WHERE id = @orderId;
    
    -- Bước 4: Cập nhật trạng thái domain thành đã thuê
    UPDATE d
    SET 
        d.status = 'Rented',
        d.expiry_date = DATEADD(month, rp.months, GETDATE())
    FROM domains d
    JOIN cart c ON d.id = c.domain_id
    JOIN rental_periods rp ON c.rental_period_id = rp.id
    WHERE c.user_id = @userId;
    
    -- Bước 5: Xóa giỏ hàng của người dùng
    DELETE FROM cart WHERE user_id = @userId;
    
    -- Trả về ID của đơn hàng đã tạo
    SELECT @orderId AS OrderId, @totalOrderPrice AS TotalPrice;
END;
GO