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

-- Bảng domains - Tăng precision cho price
CREATE TABLE domains (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    extension VARCHAR(20) NOT NULL,
    price DECIMAL(15, 2) NOT NULL CHECK (price >= 0 AND price <= 999999999.99),
    status NVARCHAR(20) NOT NULL, 
    expiry_date DATETIME NULL,
    CONSTRAINT UQ_domain_name_extension UNIQUE (name, extension)
);
GO

-- Bảng domain_extensions - Tăng precision cho default_price
CREATE TABLE domain_extensions (
    id INT IDENTITY(1,1) PRIMARY KEY,
    extension VARCHAR(20) UNIQUE NOT NULL,
    default_price DECIMAL(15, 2) NOT NULL CHECK (default_price >= 0 AND default_price <= 999999999.99),
    description NVARCHAR(255) NULL
);
GO

-- Bảng rental_periods
CREATE TABLE rental_periods (
    id INT IDENTITY(1,1) PRIMARY KEY,
    months INT NOT NULL CHECK (months > 0 AND months <= 120),
    discount DECIMAL(5, 2) NOT NULL CHECK (discount >= 0 AND discount < 1),
    description NVARCHAR(255) NOT NULL,
    CONSTRAINT UQ_rental_period_months UNIQUE (months)
);
GO

-- Bảng orders - Tăng precision cho total_price
CREATE TABLE orders (
    id INT IDENTITY(1,1) PRIMARY KEY,
    buyer_id INT NOT NULL,
    rental_period_id INT NOT NULL,
    status NVARCHAR(20) NOT NULL, 
    created_at DATETIME DEFAULT GETDATE(),
    expiry_date DATETIME NOT NULL,
    total_price DECIMAL(15, 2) NOT NULL CHECK (total_price >= 0),
    FOREIGN KEY (buyer_id) REFERENCES users(id),
    FOREIGN KEY (rental_period_id) REFERENCES rental_periods(id)
);
GO

-- Bảng order_details - Tăng precision cho price và original_price
CREATE TABLE order_details (
    id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    domain_id INT NOT NULL,
    domain_name VARCHAR(100) NOT NULL,
    domain_extension VARCHAR(20) NOT NULL,
    price DECIMAL(15, 2) NOT NULL CHECK (price >= 0),
    original_price DECIMAL(15, 2) NULL CHECK (original_price >= 0),
    purchase_date DATETIME DEFAULT GETDATE(),
    expiry_date DATETIME NULL,
    rental_period_id INT NULL,
    status NVARCHAR(20) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (domain_id) REFERENCES domains(id),
    FOREIGN KEY (rental_period_id) REFERENCES rental_periods(id)
);
GO

-- Bảng transactions - Tăng precision cho total
CREATE TABLE transactions (
    id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    domain_id INT NOT NULL,
    total DECIMAL(15, 2) NOT NULL CHECK (total >= 0),
    timestamp DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (domain_id) REFERENCES domains(id)
);
GO

-- Bảng cart - Tăng precision cho price và discounted_price
CREATE TABLE cart (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    domain_id INT NOT NULL,
    price DECIMAL(15, 2) NOT NULL CHECK (price >= 0),
    rental_period_id INT NULL,
    discounted_price DECIMAL(15, 2) NULL CHECK (discounted_price >= 0),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (domain_id) REFERENCES domains(id),
    FOREIGN KEY (rental_period_id) REFERENCES rental_periods(id),
    CONSTRAINT UQ_cart_user_domain UNIQUE (user_id, domain_id)
);
GO

-- Thêm dữ liệu mẫu

-- Thêm người dùng
INSERT INTO users (fullname, username, password, email, role)
VALUES
    (N'Lê Công Tài', '1', '1', 'admin@domain.com', 'admin'),
    (N'Nguyễn Văn A', '2', '2', 'user1@domain.com', 'user'),
	(N'Nguyễn Văn B', '3', '3', 'user2@domain.com', 'user');

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
        CONCAT(od.domain_name, od.domain_extension) as domain_name,
        od.price as price,
        od.purchase_date as purchase_date,
        od.expiry_date as expiry_date,
        o.status,
        rp.months as rental_period,
        rp.description as rental_period_description
    FROM orders o
    JOIN order_details od ON o.id = od.order_id
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
    DECLARE @domainPrice DECIMAL(15, 2);
    DECLARE @discount DECIMAL(5, 2);
    DECLARE @months INT;
    DECLARE @totalPrice DECIMAL(15, 2);
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
    INSERT INTO orders (buyer_id, rental_period_id, status, created_at, expiry_date, total_price)
    VALUES (@buyerId, @rentalPeriodId, 'Pending', GETDATE(), @expiryDate, @totalPrice);

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
    DECLARE @totalOrderPrice DECIMAL(15, 2) = 0;
    
    -- Lấy tổng giá trị đơn hàng
    SELECT @totalOrderPrice = SUM(ISNULL(discounted_price, price))
    FROM cart
    WHERE user_id = @userId;
    
    -- Tìm gói thuê có thời hạn dài nhất trong giỏ hàng
    DECLARE @maxRentalPeriodId INT;
    DECLARE @maxMonths INT;
    
    SELECT TOP 1 @maxRentalPeriodId = c.rental_period_id, @maxMonths = rp.months
    FROM cart c 
    JOIN rental_periods rp ON c.rental_period_id = rp.id
    WHERE c.user_id = @userId
    ORDER BY rp.months DESC;
    
    -- Bước 1: Tạo đơn hàng mới (không còn domain_id)
    INSERT INTO orders (buyer_id, rental_period_id, status, created_at, expiry_date, total_price)
    VALUES (
        @userId, 
        @maxRentalPeriodId,
        @status, 
        GETDATE(), 
        DATEADD(month, @maxMonths, GETDATE()), 
        @totalOrderPrice
    );
    
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
    
    -- Bước 3: Cập nhật trạng thái domain thành đã thuê
    UPDATE d
    SET 
        d.status = 'Reserved',
        d.expiry_date = DATEADD(month, rp.months, GETDATE())
    FROM domains d
    JOIN cart c ON d.id = c.domain_id
    JOIN rental_periods rp ON c.rental_period_id = rp.id
    WHERE c.user_id = @userId;
    
    -- Bước 4: Xóa giỏ hàng của người dùng
    DELETE FROM cart WHERE user_id = @userId;
    
    -- Trả về ID của đơn hàng đã tạo
    SELECT @orderId AS OrderId, @totalOrderPrice AS TotalPrice;
END;
GO

-- Thủ tục xóa domain khỏi giỏ hàng của tất cả users (sử dụng khi admin duyệt đơn hàng)
CREATE PROCEDURE RemoveDomainFromAllCarts
    @domainName VARCHAR(100),
    @domainExtension VARCHAR(20)
AS
BEGIN
    DECLARE @domainId INT;
    DECLARE @deletedRows INT = 0;
    
    -- Tìm domain ID dựa trên tên và extension
    SELECT @domainId = id 
    FROM domains 
    WHERE name = @domainName AND extension = @domainExtension;
    
    -- Nếu tìm thấy domain, xóa khỏi tất cả cart
    IF @domainId IS NOT NULL
    BEGIN
        DELETE FROM cart WHERE domain_id = @domainId;
        SET @deletedRows = @@ROWCOUNT;
    END
    
    -- Trả về số lượng dòng đã xóa
    SELECT @deletedRows AS DeletedRows;
END;
GO

-- Thủ tục kiểm tra domain trong cart còn available không trước khi checkout
CREATE PROCEDURE ValidateCartDomains
    @userId INT
AS
BEGIN
    -- Trả về danh sách domains trong cart không còn available
    SELECT 
        d.name + d.extension as full_domain_name,
        d.status,
        CASE 
            WHEN d.status != N'Sẵn sàng' THEN N'Tên miền đã được đặt hoặc không còn khả dụng'
            ELSE N'OK'
        END as validation_message
    FROM cart c
    JOIN domains d ON c.domain_id = d.id
    WHERE c.user_id = @userId
      AND d.status != N'Sẵn sàng';
END;
GO

-- Thủ tục xóa domains không available khỏi cart của user
CREATE PROCEDURE CleanupUnavailableDomainsFromCart
    @userId INT
AS
BEGIN
    DECLARE @deletedRows INT = 0;
    
    -- Xóa các domain không còn available khỏi cart
    DELETE c
    FROM cart c
    JOIN domains d ON c.domain_id = d.id
    WHERE c.user_id = @userId
      AND d.status != N'Sẵn sàng';
    
    SET @deletedRows = @@ROWCOUNT;
    
    -- Trả về số lượng domain đã bị xóa
    SELECT @deletedRows AS DeletedRows;
END;
GO

-- Bảng log để theo dõi các conflict domains (tùy chọn - để admin theo dõi)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='domain_conflict_log' AND xtype='U')
BEGIN
    CREATE TABLE domain_conflict_log (
        id INT IDENTITY(1,1) PRIMARY KEY,
        domain_name VARCHAR(100) NOT NULL,
        domain_extension VARCHAR(20) NOT NULL,
        users_affected INT NOT NULL,
        conflict_date DATETIME DEFAULT GETDATE(),
        resolution_status NVARCHAR(50) DEFAULT N'Chờ xử lý'
    );
END;
GO

-- Thủ tục kiểm tra và sửa dữ liệu có thể gây overflow
CREATE PROCEDURE CheckAndFixOverflowData
AS
BEGIN
    DECLARE @affectedRows INT = 0;
    
    -- Kiểm tra domains có giá quá lớn
    SELECT @affectedRows = COUNT(*) 
    FROM domains 
    WHERE price > 999999999.99 OR price < 0;
    
    IF @affectedRows > 0
    BEGIN
        PRINT 'Tìm thấy ' + CAST(@affectedRows AS VARCHAR) + ' domain(s) có giá không hợp lệ.';
        
        -- Cập nhật giá quá lớn về giá mặc định
        UPDATE domains 
        SET price = 100000.00 
        WHERE price > 999999999.99 OR price < 0;
        
        PRINT 'Đã cập nhật giá domain về mức mặc định.';
    END
    
    -- Kiểm tra order_details có giá quá lớn
    SELECT @affectedRows = COUNT(*) 
    FROM order_details 
    WHERE price > 9999999999999.99 OR price < 0 
       OR (original_price IS NOT NULL AND (original_price > 9999999999999.99 OR original_price < 0));
    
    IF @affectedRows > 0
    BEGIN
        PRINT 'Tìm thấy ' + CAST(@affectedRows AS VARCHAR) + ' order detail(s) có giá không hợp lệ.';
        
        -- Cập nhật giá quá lớn
        UPDATE order_details 
        SET price = 100000.00,
            original_price = CASE WHEN original_price > 9999999999999.99 OR original_price < 0 
                                THEN 100000.00 
                                ELSE original_price END
        WHERE price > 9999999999999.99 OR price < 0 
           OR (original_price IS NOT NULL AND (original_price > 9999999999999.99 OR original_price < 0));
        
        PRINT 'Đã cập nhật giá order details về mức hợp lệ.';
    END
    
    -- Kiểm tra cart có giá quá lớn
    SELECT @affectedRows = COUNT(*) 
    FROM cart 
    WHERE price > 9999999999999.99 OR price < 0 
       OR (discounted_price IS NOT NULL AND (discounted_price > 9999999999999.99 OR discounted_price < 0));
    
    IF @affectedRows > 0
    BEGIN
        PRINT 'Tìm thấy ' + CAST(@affectedRows AS VARCHAR) + ' cart item(s) có giá không hợp lệ.';
        
        -- Cập nhật giá quá lớn trong cart
        UPDATE cart 
        SET price = 100000.00,
            discounted_price = CASE WHEN discounted_price > 9999999999999.99 OR discounted_price < 0 
                                  THEN 100000.00 
                                  ELSE discounted_price END
        WHERE price > 9999999999999.99 OR price < 0 
           OR (discounted_price IS NOT NULL AND (discounted_price > 9999999999999.99 OR discounted_price < 0));
        
        PRINT 'Đã cập nhật giá cart về mức hợp lệ.';
    END
    
    PRINT 'Hoàn thành kiểm tra và sửa dữ liệu overflow.';
END;
GO

-- =====================================================
-- MIGRATION: Cập nhật bảng cart với unique constraint
-- =====================================================

-- Bước 1: Xóa các bản ghi duplicate trong cart (giữ lại bản ghi có id nhỏ nhất)
WITH DuplicateRecords AS (
    SELECT user_id, domain_id, 
           ROW_NUMBER() OVER (PARTITION BY user_id, domain_id ORDER BY id) as rn
    FROM cart
)
DELETE FROM cart 
WHERE EXISTS (
    SELECT 1 FROM DuplicateRecords d 
    WHERE d.user_id = cart.user_id 
    AND d.domain_id = cart.domain_id 
    AND d.rn > 1
    AND cart.id IN (
        SELECT c2.id FROM cart c2
        JOIN DuplicateRecords d2 ON c2.user_id = d2.user_id AND c2.domain_id = d2.domain_id
        WHERE d2.rn > 1
    )
);
GO

-- Bước 2: Thêm unique constraint nếu chưa tồn tại
IF NOT EXISTS (SELECT * FROM sys.key_constraints WHERE name = 'UQ_cart_user_domain')
BEGIN
    ALTER TABLE cart ADD CONSTRAINT UQ_cart_user_domain UNIQUE (user_id, domain_id);
    PRINT 'Đã thêm unique constraint UQ_cart_user_domain vào bảng cart';
END
ELSE
BEGIN
    PRINT 'Unique constraint UQ_cart_user_domain đã tồn tại';
END
GO

-- Bước 3: Kiểm tra kết quả migration
DECLARE @duplicateCount INT;
SELECT @duplicateCount = COUNT(*)
FROM (
    SELECT user_id, domain_id, COUNT(*) as count
    FROM cart 
    GROUP BY user_id, domain_id 
    HAVING COUNT(*) > 1
) duplicates;

IF @duplicateCount = 0
BEGIN
    PRINT 'Migration thành công - Không còn bản ghi duplicate trong bảng cart';
END
ELSE
BEGIN
    PRINT 'CẢNH BÁO: Vẫn còn ' + CAST(@duplicateCount AS VARCHAR) + ' nhóm bản ghi duplicate';
END
GO

PRINT '=== HOÀN THÀNH MIGRATION DATABASE ===';
GO