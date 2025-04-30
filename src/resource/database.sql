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
    status VARCHAR(20) NOT NULL, -- Available, Rented, Reserved
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
    status VARCHAR(20) NOT NULL, -- Pending, Approved, Completed, Cancelled
    created_at DATETIME DEFAULT GETDATE(),
    expiry_date DATETIME NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (buyer_id) REFERENCES users(id),
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


CREATE TABLE cart (
    id INT IDENTITY(1,1) PRIMARY KEY,
    domain_name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);


-- Thêm dữ liệu mẫu

-- Thêm người dùng
INSERT INTO users (fullname, username, password, email, role)
VALUES
    (N'Lê Công Tài', '1', '1', 'admin@domain.com', 'admin'),
    (N'Đặng Phan Duy', 'admin2', 'admin2@', 'admin2@domain.com', 'admin'),
    (N'Trần Minh Đại', 'admin3', 'admin3@', 'admin3@domain.com', 'admin'),
    (N'Nguyễn Văn A', 'user1', 'user1@', 'user1@domain.com', 'user');

-- Thêm domain
INSERT INTO domains (name, extension, price, status)
VALUES
    ('example', '.com', 200000, 'Available'),
    ('mywebsite', '.com', 250000, 'Available'),
    ('company', '.net', 180000, 'Available'),
    ('blog', '.org', 150000, 'Available'),
    ('online', '.store', 300000, 'Available');
GO

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

-- Thêm gói thuê
INSERT INTO rental_periods (months, discount, description)
VALUES
    (1, 0.00, N'1 tháng - Không giảm giá'),
    (6, 0.10, N'6 tháng - Giảm 10%'),
    (12, 0.20, N'12 tháng - Giảm 20%');
GO

-- Thêm đơn đặt hàng mẫu
INSERT INTO orders (buyer_id, domain_id, rental_period_id, status, created_at, expiry_date, total_price)
VALUES
    (2, 1, 3, 'Completed', DATEADD(month, -2, GETDATE()), DATEADD(month, 4, GETDATE()), 190000),
    (3, 3, 3, 'Completed', DATEADD(month, -1, GETDATE()), DATEADD(month, 11, GETDATE()), 183600);
GO

-- Cập nhật trạng thái domain đã thuê
UPDATE domains SET status = 'Rented', expiry_date = DATEADD(month, 4, GETDATE()) WHERE id = 1;
UPDATE domains SET status = 'Rented', expiry_date = DATEADD(month, 11, GETDATE()) WHERE id = 3;
GO

-- Thêm giao dịch mẫu
INSERT INTO transactions (order_id, domain_id, total, timestamp)
VALUES
    (1, 1, 190000, DATEADD(month, -2, GETDATE())),
    (2, 3, 183600, DATEADD(month, -1, GETDATE()));
GO

-- Tạo các thủ tục lưu trữ (stored procedures) cho các tác vụ phổ biến

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

-- Thủ tục lấy danh sách domain của người dùng
CREATE PROCEDURE GetUserDomains
    @userId INT
AS
BEGIN
    SELECT
        o.id as order_id,
        CONCAT(d.name, d.extension) as domain_name,
        o.total_price as price,
        o.created_at as purchase_date,
        o.expiry_date,
        o.status,
        rp.months as rental_period
    FROM orders o
    JOIN domains d ON o.domain_id = d.id
    JOIN rental_periods rp ON o.rental_period_id = rp.id
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

    -- Lấy thông tin giá và thời gian
    SELECT @domainPrice = price FROM domains WHERE id = @domainId;
    SELECT @discount = discount, @months = months FROM rental_periods WHERE id = @rentalPeriodId;

    -- Tính tổng tiền và ngày hết hạn
    SET @totalPrice = @domainPrice * @months * (1 - @discount);
    SET @expiryDate = DATEADD(month, @months, GETDATE());

    -- Tạo đơn hàng
    INSERT INTO orders (buyer_id, domain_id, rental_period_id, status, created_at, expiry_date, total_price)
    VALUES (@buyerId, @domainId, @rentalPeriodId, 'Pending', GETDATE(), @expiryDate, @totalPrice);

    SET @orderId = SCOPE_IDENTITY();

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