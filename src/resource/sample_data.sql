-- ================================================
-- SAMPLE DATA FOR REPORTS PANEL
-- Insert dữ liệu giả để báo cáo đẹp hơn
-- ================================================

USE QuanLyTenMien;
GO

-- Thêm một số domain mẫu nếu chưa có
IF NOT EXISTS (SELECT 1 FROM domains WHERE name = 'example')
BEGIN
    INSERT INTO domains (name, extension, price, status, expiry_date)
    VALUES 
        ('example', '.com', 200000, N'Sẵn sàng', NULL),
        ('techviet', '.vn', 400000, N'Sẵn sàng', NULL),
        ('shopbanhang', '.com.vn', 350000, N'Sẵn sàng', NULL),
        ('mywebsite', '.net', 150000, N'Sẵn sàng', NULL),
        ('business', '.org', 180000, N'Sẵn sàng', NULL),
        ('ecommerce', '.store', 250000, N'Sẵn sàng', NULL),
        ('blog', '.info', 120000, N'Sẵn sàng', NULL),
        ('company', '.biz', 130000, N'Sẵn sàng', NULL),
        ('portfolio', '.com', 200000, N'Sẵn sàng', NULL),
        ('startup', '.vn', 400000, N'Sẵn sàng', NULL);
END;
GO

-- Thêm đơn hàng với dữ liệu trong 30 ngày qua để tạo biểu đồ đẹp
DECLARE @StartDate DATE = DATEADD(day, -30, GETDATE());
DECLARE @CurrentDate DATE = @StartDate;
DECLARE @OrderId INT;
DECLARE @BuyerId INT = 2; -- User ID 2 (Nguyễn Văn A)
DECLARE @DomainCounter INT = 1;

WHILE @CurrentDate <= GETDATE()
BEGIN
    -- Tạo 1-3 đơn hàng mỗi ngày (ngẫu nhiên)
    DECLARE @OrdersPerDay INT = 1 + (ABS(CHECKSUM(NEWID())) % 3); -- 1-3 đơn hàng
    DECLARE @OrderIndex INT = 1;
    
    WHILE @OrderIndex <= @OrdersPerDay
    BEGIN
        -- Tính toán giá ngẫu nhiên cho đơn hàng
        DECLARE @BasePrice DECIMAL(15,2) = 150000 + (ABS(CHECKSUM(NEWID())) % 300000); -- 150K - 450K
        DECLARE @RentalPeriodId INT = 1 + (ABS(CHECKSUM(NEWID())) % 5); -- 1-5 (rental period)
        DECLARE @Discount DECIMAL(5,2);
        DECLARE @Months INT;
        
        -- Lấy thông tin rental period
        SELECT @Discount = discount, @Months = months 
        FROM rental_periods 
        WHERE id = @RentalPeriodId;
        
        DECLARE @TotalPrice DECIMAL(15,2) = @BasePrice * @Months * (1 - @Discount);
        
        -- Tạo đơn hàng
        INSERT INTO orders (buyer_id, rental_period_id, status, created_at, expiry_date, total_price)
        VALUES (
            @BuyerId,
            @RentalPeriodId,
            N'Hoàn thành', -- Đặt status là hoàn thành để tính vào báo cáo
            @CurrentDate,
            DATEADD(month, @Months, @CurrentDate),
            @TotalPrice
        );
        
        SET @OrderId = SCOPE_IDENTITY();
        
        -- Tạo 1-2 domain cho mỗi đơn hàng
        DECLARE @DomainsPerOrder INT = 1 + (ABS(CHECKSUM(NEWID())) % 2); -- 1-2 domains
        DECLARE @DomainIndex INT = 1;
        
        WHILE @DomainIndex <= @DomainsPerOrder
        BEGIN
            DECLARE @DomainId INT = 1 + (@DomainCounter % 10); -- Cycle through domains 1-10
            DECLARE @DomainPrice DECIMAL(15,2) = @TotalPrice / @DomainsPerOrder;
            
            -- Lấy thông tin domain
            DECLARE @DomainName VARCHAR(100), @DomainExt VARCHAR(20);
            SELECT @DomainName = name, @DomainExt = extension 
            FROM domains 
            WHERE id = @DomainId;
            
            -- Tạo order details
            INSERT INTO order_details (
                order_id, domain_id, domain_name, domain_extension, 
                price, original_price, purchase_date, expiry_date, 
                rental_period_id, status
            )
            VALUES (
                @OrderId,
                @DomainId,
                @DomainName,
                @DomainExt,
                @DomainPrice,
                @DomainPrice / (1 - @Discount), -- Original price before discount
                @CurrentDate,
                DATEADD(month, @Months, @CurrentDate),
                @RentalPeriodId,
                N'Hoàn thành'
            );
            
            SET @DomainIndex = @DomainIndex + 1;
            SET @DomainCounter = @DomainCounter + 1;
        END;
        
        SET @OrderIndex = @OrderIndex + 1;
        -- Thay đổi buyer_id để có nhiều khách hàng khác nhau
        SET @BuyerId = CASE WHEN @BuyerId = 2 THEN 3 ELSE 2 END;
    END;
    
    SET @CurrentDate = DATEADD(day, 1, @CurrentDate);
END;

-- Thêm một số đơn hàng cao điểm trong tuần gần đây để tạo spike đẹp
DECLARE @HighVolumeStart DATE = DATEADD(day, -7, GETDATE());
DECLARE @HighVolumeDay DATE = @HighVolumeStart;

WHILE @HighVolumeDay <= DATEADD(day, -1, GETDATE())
BEGIN
    -- Tạo 3-5 đơn hàng mỗi ngày trong tuần gần đây
    DECLARE @HighOrdersPerDay INT = 3 + (ABS(CHECKSUM(NEWID())) % 3); -- 3-5 đơn hàng
    DECLARE @HighOrderIndex INT = 1;
    
    WHILE @HighOrderIndex <= @HighOrdersPerDay
    BEGIN
        DECLARE @HighBasePrice DECIMAL(15,2) = 200000 + (ABS(CHECKSUM(NEWID())) % 400000); -- 200K - 600K
        DECLARE @HighRentalPeriodId INT = 2 + (ABS(CHECKSUM(NEWID())) % 4); -- 2-5 (longer periods)
        DECLARE @HighDiscount DECIMAL(5,2);
        DECLARE @HighMonths INT;
        
        SELECT @HighDiscount = discount, @HighMonths = months 
        FROM rental_periods 
        WHERE id = @HighRentalPeriodId;
        
        DECLARE @HighTotalPrice DECIMAL(15,2) = @HighBasePrice * @HighMonths * (1 - @HighDiscount);
        
        INSERT INTO orders (buyer_id, rental_period_id, status, created_at, expiry_date, total_price)
        VALUES (
            2, -- User 2
            @HighRentalPeriodId,
            N'Hoàn thành',
            @HighVolumeDay,
            DATEADD(month, @HighMonths, @HighVolumeDay),
            @HighTotalPrice
        );
        
        SET @OrderId = SCOPE_IDENTITY();
        
        -- Thêm order details
        INSERT INTO order_details (
            order_id, domain_id, domain_name, domain_extension, 
            price, original_price, purchase_date, expiry_date, 
            rental_period_id, status
        )
        VALUES (
            @OrderId,
            1, -- Domain 1
            'example',
            '.com',
            @HighTotalPrice,
            @HighTotalPrice / (1 - @HighDiscount),
            @HighVolumeDay,
            DATEADD(month, @HighMonths, @HighVolumeDay),
            @HighRentalPeriodId,
            N'Hoàn thành'
        );
        
        SET @HighOrderIndex = @HighOrderIndex + 1;
    END;
    
    SET @HighVolumeDay = DATEADD(day, 1, @HighVolumeDay);
END;

-- Thêm một số đơn hàng hôm nay để có dữ liệu fresh
DECLARE @TodayOrderCount INT = 2;
DECLARE @TodayIndex INT = 1;

WHILE @TodayIndex <= @TodayOrderCount
BEGIN
    DECLARE @TodayPrice DECIMAL(15,2) = 300000 + (ABS(CHECKSUM(NEWID())) % 500000); -- 300K - 800K
    
    INSERT INTO orders (buyer_id, rental_period_id, status, created_at, expiry_date, total_price)
    VALUES (
        3, -- User 3
        4, -- 12 tháng
        N'Hoàn thành',
        GETDATE(),
        DATEADD(month, 12, GETDATE()),
        @TodayPrice
    );
    
    SET @OrderId = SCOPE_IDENTITY();
    
    INSERT INTO order_details (
        order_id, domain_id, domain_name, domain_extension, 
        price, original_price, purchase_date, expiry_date, 
        rental_period_id, status
    )
    VALUES (
        @OrderId,
        @TodayIndex + 1,
        CASE @TodayIndex WHEN 1 THEN 'techviet' ELSE 'business' END,
        CASE @TodayIndex WHEN 1 THEN '.vn' ELSE '.org' END,
        @TodayPrice,
        @TodayPrice / 0.8, -- 20% discount
        GETDATE(),
        DATEADD(month, 12, GETDATE()),
        4,
        N'Hoàn thành'
    );
    
    SET @TodayIndex = @TodayIndex + 1;
END;

-- Tạo một số transactions tương ứng
INSERT INTO transactions (order_id, domain_id, total, timestamp)
SELECT 
    o.id as order_id,
    od.domain_id,
    o.total_price as total,
    o.created_at as timestamp
FROM orders o
JOIN order_details od ON o.id = od.order_id
WHERE o.status = N'Hoàn thành'
AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.order_id = o.id);

PRINT 'Đã insert thành công dữ liệu mẫu cho báo cáo!';
PRINT 'Dữ liệu bao gồm:';
PRINT '- Đơn hàng trong 30 ngày qua với xu hướng tăng';
PRINT '- Spike doanh thu trong 7 ngày gần đây';
PRINT '- Dữ liệu hôm nay';
PRINT '- Transactions tương ứng';

-- Kiểm tra kết quả
SELECT 
    CAST(created_at AS DATE) as NgayTao,
    COUNT(*) as SoDonHang,
    SUM(total_price) as TongDoanhThu,
    AVG(total_price) as DoanhThuTrungBinh
FROM orders 
WHERE status = N'Hoàn thành'
AND created_at >= DATEADD(day, -30, GETDATE())
GROUP BY CAST(created_at AS DATE)
ORDER BY NgayTao DESC; 