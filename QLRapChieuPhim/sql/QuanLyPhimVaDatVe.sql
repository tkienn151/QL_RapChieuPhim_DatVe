USE master;
GO
IF DB_ID(N'QuanLyPhimVaDatVe') IS NOT NULL
BEGIN
    ALTER DATABASE QuanLyPhimVaDatVe SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QuanLyPhimVaDatVe;
END
GO
CREATE DATABASE QuanLyPhimVaDatVe;
GO
USE QuanLyPhimVaDatVe;
GO
SET QUOTED_IDENTIFIER ON;
GO

CREATE TABLE NguoiDung (
    Id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    TenDangNhap   NVARCHAR(50)  NOT NULL UNIQUE,
    MatKhau       NVARCHAR(100) NOT NULL,
    HoTen         NVARCHAR(100) NOT NULL,
    Email         NVARCHAR(100) NULL,
    SoDienThoai   NVARCHAR(15)  NULL,
    VaiTro        NVARCHAR(20)  NOT NULL
        CONSTRAINT CK_NguoiDung_VaiTro CHECK (VaiTro IN (N'QUAN_TRI', N'NHAN_VIEN', N'KHACH_HANG')),
    HoatDong      BIT NOT NULL DEFAULT 1
);

CREATE TABLE TheLoai (
    Id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    TenTheLoai    NVARCHAR(50)  NOT NULL UNIQUE,
    MoTa          NVARCHAR(255) NULL
);

CREATE TABLE Phim (
    Id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    MaPhim         NVARCHAR(20)   NOT NULL UNIQUE,
    TenPhim        NVARCHAR(150)  NOT NULL,
    MoTa           NVARCHAR(1000) NULL,
    ThoiLuong      INT NOT NULL CONSTRAINT CK_Phim_ThoiLuong CHECK (ThoiLuong > 0),
    NgayKhoiChieu  DATE NULL,
    DaoDien        NVARCHAR(100) NULL,
    DienVien       NVARCHAR(255) NULL,
    Poster         NVARCHAR(255) NULL,
    TrangThai      NVARCHAR(20) NOT NULL DEFAULT N'DANG_CHIEU'
        CONSTRAINT CK_Phim_TrangThai CHECK (TrangThai IN (N'DANG_CHIEU', N'SAP_CHIEU', N'NGUNG_CHIEU')),
    TheLoaiId      BIGINT NOT NULL CONSTRAINT FK_Phim_TheLoai REFERENCES TheLoai(Id)
);

CREATE TABLE PhongChieu (
    Id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    MaPhong   NVARCHAR(10) NOT NULL UNIQUE,
    TenPhong  NVARCHAR(50) NOT NULL,
    SoHang    INT NOT NULL CHECK (SoHang > 0),
    SoCot     INT NOT NULL CHECK (SoCot > 0)
);

CREATE TABLE Ghe (
    Id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    PhongChieuId   BIGINT NOT NULL CONSTRAINT FK_Ghe_PhongChieu REFERENCES PhongChieu(Id),
    Hang           NVARCHAR(2) NOT NULL,
    SoGhe          INT NOT NULL,
    LoaiGhe        NVARCHAR(10) NOT NULL DEFAULT N'THUONG'
        CONSTRAINT CK_Ghe_LoaiGhe CHECK (LoaiGhe IN (N'THUONG', N'VIP')),
    CONSTRAINT UQ_Ghe_Phong_Hang_So UNIQUE (PhongChieuId, Hang, SoGhe)
);

CREATE TABLE SuatChieu (
    Id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    PhimId         BIGINT NOT NULL CONSTRAINT FK_SuatChieu_Phim REFERENCES Phim(Id),
    PhongChieuId   BIGINT NOT NULL CONSTRAINT FK_SuatChieu_PhongChieu REFERENCES PhongChieu(Id),
    GioBatDau      DATETIME2 NOT NULL,
    GioKetThuc     DATETIME2 NOT NULL,
    GiaThuong      DECIMAL(12,0) NOT NULL CONSTRAINT CK_SuatChieu_GiaThuong CHECK (GiaThuong >= 0),
    GiaVip         DECIMAL(12,0) NOT NULL CONSTRAINT CK_SuatChieu_GiaVip CHECK (GiaVip >= 0),
    TrangThai      NVARCHAR(20) NOT NULL DEFAULT N'MO_BAN'
        CONSTRAINT CK_SuatChieu_TrangThai CHECK (TrangThai IN (N'MO_BAN', N'DA_DONG', N'DA_HUY')),
    CONSTRAINT CK_SuatChieu_Gio CHECK (GioKetThuc > GioBatDau)
);

CREATE TABLE DonDat (
    Id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    MaDon         NVARCHAR(20) NOT NULL UNIQUE,
    NguoiDungId   BIGINT NOT NULL CONSTRAINT FK_DonDat_NguoiDung REFERENCES NguoiDung(Id),
    HoTenKhach    NVARCHAR(100) NULL,
    SdtKhach      NVARCHAR(15)  NULL,
    NgayDat       DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    HanThanhToan  DATETIME2 NULL,
    TongTien      DECIMAL(12,0) NOT NULL DEFAULT 0,
    TrangThai     NVARCHAR(20) NOT NULL DEFAULT N'CHO_THANH_TOAN'
        CONSTRAINT CK_DonDat_TrangThai CHECK (TrangThai IN (N'CHO_THANH_TOAN', N'DA_THANH_TOAN', N'DA_HUY', N'HET_HAN')),
    KenhDat       NVARCHAR(10) NOT NULL DEFAULT N'WEB'
        CONSTRAINT CK_DonDat_KenhDat CHECK (KenhDat IN (N'WEB', N'QUAY'))
);

CREATE TABLE Ve (
    Id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    MaVe          NVARCHAR(20) NOT NULL UNIQUE,
    DonDatId      BIGINT NOT NULL CONSTRAINT FK_Ve_DonDat REFERENCES DonDat(Id),
    SuatChieuId   BIGINT NOT NULL CONSTRAINT FK_Ve_SuatChieu REFERENCES SuatChieu(Id),
    GheId         BIGINT NOT NULL CONSTRAINT FK_Ve_Ghe REFERENCES Ghe(Id),
    GiaVe         DECIMAL(12,0) NOT NULL,
    TrangThai     NVARCHAR(20) NOT NULL DEFAULT N'HOP_LE'
        CONSTRAINT CK_Ve_TrangThai CHECK (TrangThai IN (N'HOP_LE', N'DA_SOAT', N'DA_HUY'))
);
GO

CREATE UNIQUE INDEX UX_Ve_Suat_Ghe ON Ve (SuatChieuId, GheId) WHERE TrangThai <> N'DA_HUY';
GO

USE QuanLyPhimVaDatVe;
GO
SET QUOTED_IDENTIFIER ON;
GO

INSERT INTO TheLoai (TenTheLoai, MoTa) VALUES
    (N'Hành động', N'Phim hành động, võ thuật, truy đuổi'),
    (N'Tình cảm', N'Phim tình cảm, lãng mạn'),
    (N'Hài', N'Phim hài hước, giải trí'),
    (N'Kinh dị', N'Phim kinh dị, giật gân'),
    (N'Hoạt hình', N'Phim hoạt hình cho mọi lứa tuổi'),
    (N'Khoa học viễn tưởng', N'Phim khoa học, tương lai, vũ trụ'),
    (N'Phiêu lưu', N'Phim phiêu lưu, khám phá'),
    (N'Tâm lý', N'Phim tâm lý, đời sống'),
    (N'Hình sự', N'Phim điều tra, tội phạm'),
    (N'Chiến tranh', N'Phim về đề tài chiến tranh'),
    (N'Âm nhạc', N'Phim ca nhạc, âm nhạc'),
    (N'Gia đình', N'Phim về tình cảm gia đình'),
    (N'Thể thao', N'Phim về thể thao, thi đấu'),
    (N'Lịch sử', N'Phim về đề tài lịch sử'),
    (N'Giả tưởng', N'Phim thế giới giả tưởng, phép thuật'),
    (N'Tài liệu', N'Phim tài liệu'),
    (N'Võ thuật', N'Phim võ thuật, kung fu'),
    (N'Bí ẩn', N'Phim bí ẩn, trinh thám'),
    (N'Thiếu nhi', N'Phim dành cho thiếu nhi'),
    (N'Viễn Tây', N'Phim cao bồi miền Tây'),
    (N'Học đường', N'Phim thanh xuân, tuổi học trò'),
    (N'Trinh thám', N'Phim phá án, suy luận'),
    (N'Chính kịch', N'Phim chính kịch, xã hội'),
    (N'Kỳ ảo', N'Phim thần thoại, thế giới kỳ ảo'),
    (N'Siêu anh hùng', N'Phim siêu anh hùng, đa vũ trụ');
GO

INSERT INTO Phim (MaPhim, TenPhim, MoTa, ThoiLuong, NgayKhoiChieu, DaoDien, DienVien, Poster, TrangThai, TheLoaiId) VALUES
    (N'P001', N'Mai', N'Câu chuyện tâm lý tình cảm sâu sắc về Mai - người phụ nữ làm nghề massage trị liệu với nhiều tổn thương quá khứ và mối tình cùng Dương.', 131, DATEADD(DAY, -20, CAST(GETDATE() AS DATE)), N'Trấn Thành', N'Phương Anh Đào, Tuấn Trần, Hồng Đào', N'phim01.jpg', N'DANG_CHIEU', 8),
    (N'P002', N'Lật Mặt 7: Một Điều Ước', N'Hành trình cảm động của bà Hai 73 tuổi lần lượt đến thăm 5 người con trưởng thành ở khắp mọi miền đất nước.', 138, DATEADD(DAY, -15, CAST(GETDATE() AS DATE)), N'Lý Hải', N'Thanh Hiền, Trương Minh Cường, Đinh Y Nhung, Quách Ngọc Tuyên', N'phim02.jpg', N'DANG_CHIEU', 12),
    (N'P003', N'Đào, Phở và Piano', N'Bản hùng ca lãng mạn và bi tráng về những người dân Hà Nội trong cuộc chiến 60 ngày đêm bảo vệ Thủ đô cuối năm 1946.', 100, DATEADD(DAY, -12, CAST(GETDATE() AS DATE)), N'Phi Tiến Sơn', N'Doãn Quốc Đam, Cao Thị Thùy Linh, NSND Trung Hiếu, Trần Lực', N'phim03.jpg', N'DANG_CHIEU', 14),
    (N'P004', N'Quỷ Cẩu', N'Nam cùng bạn gái trở về quê chịu tang cha và dần phát hiện những bí mật tâm linh rùng rợn xoay quanh lò mổ gia truyền.', 100, DATEADD(DAY, -10, CAST(GETDATE() AS DATE)), N'Lưu Thành Luân', N'Quang Tuấn, NSND Kim Xuân, Vân Dung, Quốc Quân', N'phim04.jpg', N'DANG_CHIEU', 4),
    (N'P005', N'Những Mảnh Ghép Cảm Xúc 2 (Inside Out 2)', N'Cô bé Riley bước vào tuổi dậy thì với sự xuất hiện của các cảm xúc mới như Lo Âu, Ganh Tị, Chán Nản và Xấu Hổ.', 96, DATEADD(DAY, -30, CAST(GETDATE() AS DATE)), N'Kelsey Mann', N'Amy Poehler, Maya Hawke, Kensington Tallman', N'phim05.jpg', N'DANG_CHIEU', 5),
    (N'P006', N'Hành Tinh Cát: Phần Hai (Dune: Part Two)', N'Paul Atreides hợp lực cùng Chani và tộc người Fremen trên hành tinh sa mạc Arrakis để trả thù những kẻ đã hủy hoại gia tộc mình.', 166, DATEADD(DAY, -8, CAST(GETDATE() AS DATE)), N'Denis Villeneuve', N'Timothée Chalamet, Zendaya, Rebecca Ferguson, Austin Butler', N'phim06.jpg', N'DANG_CHIEU', 6),
    (N'P007', N'Deadpool & Wolverine', N'Wade Wilson tái xuất cùng Wolverine trong hành trình xuyên đa vũ trụ đầy hài hước và kịch tính để giải cứu thế giới của mình.', 128, DATEADD(DAY, -6, CAST(GETDATE() AS DATE)), N'Shawn Levy', N'Ryan Reynolds, Hugh Jackman, Emma Corrin', N'phim07.jpg', N'DANG_CHIEU', 1),
    (N'P008', N'Đất Rừng Phương Nam', N'Hành trình phiêu lưu đầy cảm xúc của cậu bé An đi tìm cha khắp lục tỉnh miền Tây Nam Bộ đầu thế kỷ 20.', 110, DATEADD(DAY, -5, CAST(GETDATE() AS DATE)), N'Nguyễn Quang Dũng', N'Hạo Khang, Tuấn Trần, Trấn Thành, Hứa Vĩ Văn', N'phim08.jpg', N'DANG_CHIEU', 7),
    (N'P009', N'Godzilla x Kong: Đế Chế Mới', N'Hai siêu thú huyền thoại Godzilla và Kong buộc phải liên minh để chống lại một mối đe dọa khổng lồ ẩn sâu trong lòng Trái Đất.', 115, DATEADD(DAY, -18, CAST(GETDATE() AS DATE)), N'Adam Wingard', N'Rebecca Hall, Brian Tyree Henry, Dan Stevens', N'phim09.jpg', N'DANG_CHIEU', 1),
    (N'P010', N'Mắt Biếc', N'Chuyện tình đơn phương trong trẻo nhưng day dứt của chàng trai Ngạn dành cho Hà Lan - cô gái có đôi mắt biếc từ làng Đo Đo.', 117, DATEADD(DAY, -9, CAST(GETDATE() AS DATE)), N'Victor Vũ', N'Trần Nghĩa, Trúc Anh, Trần Phong, Khánh Vân', N'phim10.jpg', N'DANG_CHIEU', 2),
    (N'P011', N'Kung Fu Panda 4', N'Thần Long Đại Hiệp Po bước vào hành trình tìm kiếm người kế vị và đối đầu với ác nhân Tắc Kè Hoa có khả năng biến hình.', 94, DATEADD(DAY, -14, CAST(GETDATE() AS DATE)), N'Mike Mitchell', N'Jack Black, Awkwafina, Viola Davis, Dustin Hoffman', N'phim11.jpg', N'DANG_CHIEU', 5),
    (N'P012', N'Kẻ Ăn Hồn', N'Chuỗi án mạng bí ẩn tại làng Địa Ngục gắn liền với cổ thuật luyện rượu sọ người và những oán khuất truyền đời.', 109, DATEADD(DAY, -4, CAST(GETDATE() AS DATE)), N'Trần Hữu Tấn', N'Hoàng Hà, Võ Điền Gia Huy, Huỳnh Thanh Trực, NSƯT Chiều Xuân', N'phim12.jpg', N'DANG_CHIEU', 4),
    (N'P013', N'Kẻ Trộm Mặt Trăng 4 (Despicable Me 4)', N'Gia đình Gru chào đón thành viên nhí Gru Jr. cùng những màn quậy phá cười ra nước mắt của biệt đội Minions.', 95, DATEADD(DAY, -25, CAST(GETDATE() AS DATE)), N'Chris Renaud', N'Steve Carell, Kristen Wiig, Pierre Coffin', N'phim13.jpg', N'DANG_CHIEU', 5),
    (N'P014', N'Avatar: Dòng Chảy Của Nước', N'Jake Sully và Neytiri cùng các con tìm nơi trú ẩn tại bộ tộc biển Metkayina trên hành tinh Pandora để chống lại cuộc xâm lăng mới.', 192, DATEADD(DAY, -3, CAST(GETDATE() AS DATE)), N'James Cameron', N'Sam Worthington, Zoe Saldaña, Sigourney Weaver, Kate Winslet', N'phim14.jpg', N'DANG_CHIEU', 6),
    (N'P015', N'Cám', N'Dị bản kinh dị cổ trang lấy cảm hứng từ truyện cổ tích Tấm Cám với những nghi lễ hiến tế bí ẩn trong gia tộc họ Hoàng.', 122, DATEADD(DAY, 10, CAST(GETDATE() AS DATE)), N'Trần Hữu Tấn', N'Quốc Cường, Thúy Diễm, Rima Thanh Vy, Lâm Thanh Mỹ', N'phim15.jpg', N'SAP_CHIEU', 4),
    (N'P016', N'Nhà Bà Nữ', N'Câu chuyện đời thường hài hước và sâu sắc về mối quan hệ nhiều thế hệ trong gia đình bà Nữ bán bánh canh cua.', 102, DATEADD(DAY, 14, CAST(GETDATE() AS DATE)), N'Trấn Thành', N'Lê Giang, Uyển Ân, Song Luân, Khả Như, Trấn Thành', N'phim16.jpg', N'SAP_CHIEU', 3),
    (N'P017', N'Oppenheimer', N'Bộ phim tiểu sử lịch sử đoạt giải Oscar kể về nhà vật lý lý thuyết J. Robert Oppenheimer và dự án Manhattan chế tạo bom nguyên tử.', 180, DATEADD(DAY, 21, CAST(GETDATE() AS DATE)), N'Christopher Nolan', N'Cillian Murphy, Emily Blunt, Matt Damon, Robert Downey Jr.', N'phim17.jpg', N'SAP_CHIEU', 14),
    (N'P018', N'Hành Trình Của Moana 2 (Moana 2)', N'Moana và á thần Maui tái hợp trong chuyến hải trình vượt đại dương xa xôi sau khi nhận được tiếng gọi từ tổ tiên.', 100, DATEADD(DAY, 28, CAST(GETDATE() AS DATE)), N'David Derrick Jr.', N'Auli''i Cravalho, Dwayne Johnson', N'phim18.jpg', N'SAP_CHIEU', 7),
    (N'P019', N'Bố Già', N'Tình phụ tử cảm động giữa ông Ba Sang làm nghề chở hàng thuê trong con hẻm nhỏ Sài Gòn và cậu con trai cá tính Quắn.', 128, DATEADD(DAY, -120, CAST(GETDATE() AS DATE)), N'Trấn Thành, Vũ Ngọc Đãng', N'Trấn Thành, Tuấn Trần, Ngân Chi, NSND Ngọc Giàu', N'phim19.jpg', N'NGUNG_CHIEU', 12),
    (N'P020', N'Avengers: Hồi Kết (Avengers: Endgame)', N'Trận chiến lịch sử cuối cùng của biệt đội siêu anh hùng Avengers nhằm đảo ngược cú búng tay của Thanos và cứu lấy vũ trụ.', 181, DATEADD(DAY, -150, CAST(GETDATE() AS DATE)), N'Anthony & Joe Russo', N'Robert Downey Jr., Chris Evans, Scarlett Johansson, Mark Ruffalo', N'phim20.jpg', N'NGUNG_CHIEU', 1),
    (N'P021', N'Thám Tử Kiên: Kỳ Án Không Đầu', N'Chuỗi vụ mất tích bí ẩn tại một ngôi làng cổ khiến Thám tử Kiên phải lần theo từng dấu vết tâm linh và manh mối trinh thám.', 131, DATEADD(DAY, -7, CAST(GETDATE() AS DATE)), N'Victor Vũ', N'Quốc Huy, Đinh Ngọc Diệp, Quốc Anh, Anh Phạm', N'phim01.jpg', N'DANG_CHIEU', 22),
    (N'P022', N'Thanh Xuân 18x2: Lữ Khách Phương Trời', N'Chàng trai 36 tuổi lần giở quyển nhật ký thanh xuân để tìm lại mối tình đầu tuổi 18 qua chuyến hành trình xuyên Nhật Bản.', 123, DATEADD(DAY, -11, CAST(GETDATE() AS DATE)), N'Fujii Michihito', N'Hứa Quang Hán, Kiyohara Kaya, Trương Hiếu Toàn', N'phim02.jpg', N'DANG_CHIEU', 21),
    (N'P023', N'Ký Sinh Trùng (Parasite)', N'Gia đình họ Kim nghèo khó từng bước thâm nhập vào cuộc sống của gia tộc giàu có họ Park với những bí mật khôn lường.', 132, DATEADD(DAY, -16, CAST(GETDATE() AS DATE)), N'Bong Joon-ho', N'Song Kang-ho, Lee Sun-kyun, Cho Yeo-jeong, Choi Woo-shik', N'phim03.jpg', N'DANG_CHIEU', 23),
    (N'P024', N'Vùng Đất Kỳ Ảo (Wicked)', N'Câu chuyện trước khi Dorothy đặt chân đến xứ Oz xoay quanh tình bạn đặc biệt giữa hai phù thủy trẻ Elphaba và Glinda.', 160, DATEADD(DAY, 18, CAST(GETDATE() AS DATE)), N'Jon M. Chu', N'Cynthia Erivo, Ariana Grande, Jonathan Bailey, Michelle Yeoh', N'phim04.jpg', N'SAP_CHIEU', 24),
    (N'P025', N'Người Nhện: Du Hành Vũ Trụ Nhện', N'Miles Morales dấn thân vào đa vũ trụ Người Nhện để bảo vệ thực tại khỏi hiểm họa mới đe dọa toàn bộ dòng thời gian.', 140, DATEADD(DAY, -95, CAST(GETDATE() AS DATE)), N'Joaquim Dos Santos', N'Shameik Moore, Hailee Steinfeld, Oscar Isaac', N'phim05.jpg', N'NGUNG_CHIEU', 25);
GO

INSERT INTO PhongChieu (MaPhong, TenPhong, SoHang, SoCot) VALUES
    (N'P01', N'Phòng 1', 6, 8),
    (N'P02', N'Phòng 2', 6, 8),
    (N'P03', N'Phòng 3', 6, 8),
    (N'P04', N'Phòng 4', 6, 8),
    (N'P05', N'Phòng 5', 6, 8),
    (N'P06', N'Phòng 6', 6, 8),
    (N'P07', N'Phòng 7', 6, 8),
    (N'P08', N'Phòng 8', 6, 8),
    (N'P09', N'Phòng 9', 6, 8),
    (N'P10', N'Phòng 10', 6, 8),
    (N'P11', N'Phòng 11', 6, 8),
    (N'P12', N'Phòng 12', 6, 8),
    (N'P13', N'Phòng 13', 6, 8),
    (N'P14', N'Phòng 14', 6, 8),
    (N'P15', N'Phòng 15', 6, 8),
    (N'P16', N'Phòng 16', 6, 8),
    (N'P17', N'Phòng 17', 6, 8),
    (N'P18', N'Phòng 18', 6, 8),
    (N'P19', N'Phòng 19', 6, 8),
    (N'P20', N'Phòng 20', 6, 8),
    (N'P21', N'Phòng 21', 6, 8),
    (N'P22', N'Phòng 22', 6, 8),
    (N'P23', N'Phòng 23', 6, 8),
    (N'P24', N'Phòng 24', 6, 8),
    (N'P25', N'Phòng 25', 6, 8);
GO

DECLARE @p INT, @h INT, @c INT;
SET @p = 1;
WHILE @p <= 25
BEGIN
    SET @h = 0;
    WHILE @h < 6
    BEGIN
        SET @c = 1;
        WHILE @c <= 8
        BEGIN
            INSERT INTO Ghe (PhongChieuId, Hang, SoGhe, LoaiGhe)
            VALUES (@p, CHAR(65 + @h), @c, CASE WHEN @h >= 4 THEN N'VIP' ELSE N'THUONG' END);
            SET @c = @c + 1;
        END
        SET @h = @h + 1;
    END
    SET @p = @p + 1;
END
GO

INSERT INTO NguoiDung (TenDangNhap, MatKhau, HoTen, Email, SoDienThoai, VaiTro, HoatDong) VALUES
    (N'admin', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Quản Trị Hệ Thống', N'admin@email.com', N'0901000001', N'QUAN_TRI', 1),
    (N'nhanvien1', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Phạm Trung Kiên', N'nhanvien1@email.com', N'0901000002', N'NHAN_VIEN', 1),
    (N'nhanvien2', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Phạm Trần Ngọc Nhân', N'nhanvien2@email.com', N'0901000003', N'NHAN_VIEN', 1),
    (N'nhanvien3', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Phạm Quốc Đạt', N'nhanvien3@email.com', N'0901000004', N'NHAN_VIEN', 1),
    (N'khach01', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Nguyễn Trọng Nhân', N'khach01@email.com', N'0901000005', N'KHACH_HANG', 1),
    (N'khach02', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Lê Huy Quốc Bảo', N'khach02@email.com', N'0901000006', N'KHACH_HANG', 1),
    (N'khach03', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Nguyễn Thanh Huyền', N'khach03@email.com', N'0901000007', N'KHACH_HANG', 1),
    (N'khach04', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Phạm Thu Dung', N'khach04@email.com', N'0901000008', N'KHACH_HANG', 1),
    (N'khach05', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Hoàng Minh Đức', N'khach05@email.com', N'0901000009', N'KHACH_HANG', 1),
    (N'khach06', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Vũ Thị Giang', N'khach06@email.com', N'0901000010', N'KHACH_HANG', 1),
    (N'khach07', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Đặng Quốc Huy', N'khach07@email.com', N'0901000011', N'KHACH_HANG', 1),
    (N'khach08', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Bùi Thanh Hà', N'khach08@email.com', N'0901000012', N'KHACH_HANG', 1),
    (N'khach09', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Ngô Gia Khánh', N'khach09@email.com', N'0901000013', N'KHACH_HANG', 1),
    (N'khach10', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Dương Thị Lan', N'khach10@email.com', N'0901000014', N'KHACH_HANG', 1),
    (N'khach11', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Đỗ Văn Long', N'khach11@email.com', N'0901000015', N'KHACH_HANG', 1),
    (N'khach12', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Lý Mỹ Linh', N'khach12@email.com', N'0901000016', N'KHACH_HANG', 1),
    (N'khach13', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Phan Anh Minh', N'khach13@email.com', N'0901000017', N'KHACH_HANG', 1),
    (N'khach14', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Võ Thị Ngọc', N'khach14@email.com', N'0901000018', N'KHACH_HANG', 1),
    (N'khach15', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Huỳnh Tấn Phát', N'khach15@email.com', N'0901000019', N'KHACH_HANG', 1),
    (N'khach16', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Mai Thanh Trúc', N'khach16@email.com', N'0901000020', N'KHACH_HANG', 1),
    (N'khach17', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Tạ Minh Quân', N'khach17@email.com', N'0901000021', N'KHACH_HANG', 1),
    (N'khach18', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Trịnh Phương Thảo', N'khach18@email.com', N'0901000022', N'KHACH_HANG', 1),
    (N'khach19', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Lưu Gia Bảo', N'khach19@email.com', N'0901000023', N'KHACH_HANG', 1),
    (N'khach20', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Kiều Hải Yến', N'khach20@email.com', N'0901000024', N'KHACH_HANG', 1),
    (N'khach21', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Châu Thành Đạt', N'khach21@email.com', N'0901000025', N'KHACH_HANG', 1);
GO

DECLARE @s INT, @gio INT, @bd DATETIME2, @phim INT, @tl INT, @gt DECIMAL(12,0);
SET @s = 1;
WHILE @s <= 40
BEGIN
    SET @gio = 9 + ((@s - 1) % 5) * 3;
    SET @bd = DATEADD(HOUR, @gio, CAST(CAST(DATEADD(DAY, ((@s - 1) / 5) + 1, GETDATE()) AS DATE) AS DATETIME2));
    IF @s = 1 SET @bd = DATEADD(MINUTE, 30, SYSDATETIME());
    SET @phim = ((@s - 1) % 14) + 1;
    SELECT @tl = ThoiLuong FROM Phim WHERE Id = @phim;
    SET @gt = CASE WHEN @gio >= 18 THEN 90000 ELSE 75000 END;
    INSERT INTO SuatChieu (PhimId, PhongChieuId, GioBatDau, GioKetThuc, GiaThuong, GiaVip, TrangThai)
    VALUES (@phim, ((@s - 1) % 20) + 1, @bd, DATEADD(MINUTE, @tl, @bd), @gt, @gt + 20000, N'MO_BAN');
    SET @s = @s + 1;
END
WHILE @s <= 45
BEGIN
    SET @gio = 9 + ((@s - 1) % 5) * 3;
    SET @bd = DATEADD(HOUR, @gio, CAST(CAST(DATEADD(DAY, ((@s - 1) / 5) + 1, GETDATE()) AS DATE) AS DATETIME2));
    SET @phim = 21 + ((@s - 41) % 3);
    SELECT @tl = ThoiLuong FROM Phim WHERE Id = @phim;
    SET @gt = CASE WHEN @gio >= 18 THEN 90000 ELSE 75000 END;
    INSERT INTO SuatChieu (PhimId, PhongChieuId, GioBatDau, GioKetThuc, GiaThuong, GiaVip, TrangThai)
    VALUES (@phim, @s - 20, @bd, DATEADD(MINUTE, @tl, @bd), @gt, @gt + 20000, N'MO_BAN');
    SET @s = @s + 1;
END
GO

DECLARE @i INT, @gia DECIMAL(12,0), @g1 INT, @tt NVARCHAR(20), @kenh NVARCHAR(10), @uid BIGINT, @ttVe NVARCHAR(20), @scId INT;
DECLARE @ngay DATETIME2, @han DATETIME2, @ten NVARCHAR(100), @sdt NVARCHAR(15);
SET @i = 1;
WHILE @i <= 25
BEGIN
    SET @scId = CASE WHEN @i <= 20 THEN @i ELSE @i + 20 END;
    IF @i % 5 = 0
    BEGIN
        SELECT @gia = GiaVip FROM SuatChieu WHERE Id = @scId;
        SET @g1 = (@i - 1) * 48 + 32 + (@i / 5);
    END
    ELSE
    BEGIN
        SELECT @gia = GiaThuong FROM SuatChieu WHERE Id = @scId;
        SET @g1 = CASE WHEN @i <= 20 THEN (@i - 1) * 48 + @i ELSE (@i - 1) * 48 + (@i - 20) END;
    END
    SET @tt = CASE WHEN @i IN (9, 18) THEN N'DA_HUY' WHEN @i = 13 THEN N'HET_HAN' WHEN @i = 7 THEN N'CHO_THANH_TOAN' ELSE N'DA_THANH_TOAN' END;
    SET @ttVe = CASE WHEN @i IN (9, 13, 18) THEN N'DA_HUY' ELSE N'HOP_LE' END;
    SET @kenh = CASE WHEN (@i <= 16 OR @i BETWEEN 21 AND 24) THEN N'WEB' ELSE N'QUAY' END;
    SET @uid = CASE WHEN @i <= 16 THEN 4 + @i WHEN @i <= 20 THEN 2 + (@i % 3) WHEN @i <= 24 THEN @i ELSE 2 END;
    SET @ngay = DATEADD(DAY, -(@i % 7), SYSDATETIME());
    SET @han = CASE WHEN @i IN (7, 13) THEN DATEADD(MINUTE, 5, @ngay) ELSE NULL END;
    SET @ten = CASE WHEN (@i BETWEEN 17 AND 20 OR @i = 25) THEN N'Khách lẻ ' + CAST(@i AS NVARCHAR(10)) ELSE NULL END;
    SET @sdt = CASE WHEN (@i BETWEEN 17 AND 20 OR @i = 25) THEN N'09020000' + RIGHT(N'00' + CAST(@i AS NVARCHAR(10)), 2) ELSE NULL END;

    INSERT INTO DonDat (MaDon, NguoiDungId, HoTenKhach, SdtKhach, NgayDat, HanThanhToan, TongTien, TrangThai, KenhDat)
    VALUES (N'DD' + RIGHT(N'0000' + CAST(@i AS NVARCHAR(10)), 4), @uid, @ten, @sdt,
            @ngay, @han, @gia * 2, @tt, @kenh);

    INSERT INTO Ve (MaVe, DonDatId, SuatChieuId, GheId, GiaVe, TrangThai) VALUES
        (N'VE' + RIGHT(N'000000' + CAST(2 * @i - 1 AS NVARCHAR(10)), 6), @i, @scId, @g1,     @gia, @ttVe),
        (N'VE' + RIGHT(N'000000' + CAST(2 * @i AS NVARCHAR(10)), 6),     @i, @scId, @g1 + 1, @gia, @ttVe);

    SET @i = @i + 1;
END
GO

SELECT N'NguoiDung' AS Bang, COUNT(*) AS SoDong FROM NguoiDung UNION ALL
SELECT N'TheLoai', COUNT(*) FROM TheLoai UNION ALL
SELECT N'Phim', COUNT(*) FROM Phim UNION ALL
SELECT N'PhongChieu', COUNT(*) FROM PhongChieu UNION ALL
SELECT N'Ghe', COUNT(*) FROM Ghe UNION ALL
SELECT N'SuatChieu', COUNT(*) FROM SuatChieu UNION ALL
SELECT N'DonDat', COUNT(*) FROM DonDat UNION ALL
SELECT N'Ve', COUNT(*) FROM Ve;
GO

SELECT COUNT(*) AS VeTrungGhe FROM (
    SELECT SuatChieuId, GheId FROM Ve WHERE TrangThai <> N'DA_HUY'
    GROUP BY SuatChieuId, GheId HAVING COUNT(*) > 1) AS x;
SELECT COUNT(*) AS VeSaiPhong
FROM Ve v JOIN SuatChieu s ON s.Id = v.SuatChieuId JOIN Ghe g ON g.Id = v.GheId
WHERE g.PhongChieuId <> s.PhongChieuId;
SELECT COUNT(*) AS DonSaiTongTien
FROM DonDat d WHERE d.TongTien <> (SELECT SUM(v.GiaVe) FROM Ve v WHERE v.DonDatId = d.Id);
GO
SELECT COUNT(*) AS VeSaiGia
FROM Ve v JOIN SuatChieu s ON s.Id = v.SuatChieuId JOIN Ghe g ON g.Id = v.GheId
WHERE v.GiaVe <> CASE WHEN g.LoaiGhe = N'VIP' THEN s.GiaVip ELSE s.GiaThuong END;
GO
