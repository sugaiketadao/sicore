-- Order Management Test Data
drop table if exists t_order;
drop table if exists t_order_detail;

create table t_order (
  order_no varchar(10)
 ,order_dt date
 ,supplier_cd varchar(6)
 ,supplier_nm varchar(50)
 ,delivery_dt date
 ,status_cs varchar(2)
 ,total_am numeric(12)
 ,remarks varchar(200)
 ,upd_ts timestamp(6)
 ,primary key (order_no)
);

create table t_order_detail (
  order_no varchar(10)
 ,line_no numeric(3)
 ,goods_cd varchar(10)
 ,goods_nm varchar(50)
 ,quantity numeric(6)
 ,unit_price numeric(10)
 ,amount numeric(12)
 ,upd_ts timestamp(6)
 ,primary key (order_no, line_no)
);

delete from t_order;
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD001', '2025-12-01', 'SUP001', 'Yamada Trading Co., Ltd.', '2025-12-10', '10', 150000, 'Urgent order', '2025-01-01 12:00:00.000000');
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD002', '2025-12-02', 'SUP002', 'Tanaka Products Co., Ltd.', '2025-12-15', '20', 280000, '', '2025-01-01 12:00:00.000000');
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD003', '2025-12-03', 'SUP001', 'Yamada Trading Co., Ltd.', '2025-12-20', '30', 95000, 'Received', '2025-01-01 12:00:00.000000');
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD004', '2025-11-15', 'SUP003', 'Sato Industries Co., Ltd.', '2025-11-25', '90', 50000, 'Cancelled', '2025-01-01 12:00:00.000000');
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD005', '2025-12-04', 'SUP004', 'Suzuki Electric Co., Ltd.', '2025-12-25', '10', 420000, 'Year-end delivery requested', '2025-01-01 12:00:00.000000');
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD006', '2025-11-20', 'SUP002', 'Tanaka Products Co., Ltd.', '2025-12-01', '30', 180000, '', '2025-01-01 12:00:00.000000');
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD007', '2025-12-01', 'SUP005', 'Takahashi Metal Industries', '2025-12-18', '20', 320000, 'Partial delivery acceptable', '2025-01-01 12:00:00.000000');
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD008', '2025-11-28', 'SUP003', 'Sato Industries Co., Ltd.', '2025-12-05', '30', 75000, '', '2025-01-01 12:00:00.000000');
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD009', '2025-12-02', 'SUP001', 'Yamada Trading Co., Ltd.', '2025-12-12', '10', 560000, 'Large order', '2025-01-01 12:00:00.000000');
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD010', '2025-12-03', 'SUP006', 'ABC Trading Co.', '2025-12-28', '10', 890000, 'Overseas supplier', '2025-01-01 12:00:00.000000');

delete from t_order_detail;
-- ORD001 Details
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD001', 1, 'GOODS001', 'Ballpoint Pen (Black)', 100, 150, 15000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD001', 2, 'GOODS002', 'Notebook A4', 50, 300, 15000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD001', 3, 'GOODS003', 'Clear File Folder', 200, 600, 120000, '2025-01-01 12:00:00.000000');

-- ORD002 Details
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD002', 1, 'GOODS004', 'Copy Paper A4', 20, 3500, 70000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD002', 2, 'GOODS005', 'Toner Cartridge', 5, 12000, 60000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD002', 3, 'GOODS006', 'USB Flash Drive 32GB', 10, 1500, 15000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD002', 4, 'GOODS007', 'Mouse', 15, 2000, 30000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD002', 5, 'GOODS008', 'Keyboard', 10, 3500, 35000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD002', 6, 'GOODS009', 'Monitor Stand', 10, 7000, 70000, '2025-01-01 12:00:00.000000');

-- ORD003 Details
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD003', 1, 'GOODS010', 'Sticky Notes Set', 100, 200, 20000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD003', 2, 'GOODS011', 'Whiteboard Marker', 50, 250, 12500, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD003', 3, 'GOODS012', 'Eraser', 100, 50, 5000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD003', 4, 'GOODS013', 'Mechanical Pencil', 50, 350, 17500, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD003', 5, 'GOODS014', 'Ruler 30cm', 100, 400, 40000, '2025-01-01 12:00:00.000000');

-- ORD004 Details (Cancelled)
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD004', 1, 'GOODS015', 'Desk Lamp', 10, 5000, 50000, '2025-01-01 12:00:00.000000');

-- ORD005 Details
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD005', 1, 'GOODS016', 'Laptop PC', 5, 80000, 400000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD005', 2, 'GOODS017', 'PC Bag', 5, 4000, 20000, '2025-01-01 12:00:00.000000');

-- ORD006 Details
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD006', 1, 'GOODS018', 'Printer', 2, 45000, 90000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD006', 2, 'GOODS019', 'Printer Cable', 2, 1500, 3000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD006', 3, 'GOODS004', 'Copy Paper A4', 20, 3500, 70000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD006', 4, 'GOODS020', 'Ink Cartridge Set', 2, 8500, 17000, '2025-01-01 12:00:00.000000');

-- ORD007 Details
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD007', 1, 'GOODS021', 'Steel Rack', 4, 25000, 100000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD007', 2, 'GOODS022', 'File Cabinet', 2, 35000, 70000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD007', 3, 'GOODS023', 'Office Chair', 5, 30000, 150000, '2025-01-01 12:00:00.000000');

-- ORD008 Details
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD008', 1, 'GOODS024', 'Power Strip', 15, 2500, 37500, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD008', 2, 'GOODS025', 'LAN Cable 5m', 25, 1500, 37500, '2025-01-01 12:00:00.000000');

-- ORD009 Details
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD009', 1, 'GOODS026', 'Desk', 10, 35000, 350000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD009', 2, 'GOODS027', 'Desk Mat', 10, 3000, 30000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD009', 3, 'GOODS028', 'Pen Holder', 20, 800, 16000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD009', 4, 'GOODS029', 'Document Tray', 10, 1200, 12000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD009', 5, 'GOODS030', 'Trash Can', 10, 1500, 15000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD009', 6, 'GOODS031', 'Calendar', 10, 500, 5000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD009', 7, 'GOODS032', 'Clock', 5, 8000, 40000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD009', 8, 'GOODS033', 'Indoor Plant', 4, 23000, 92000, '2025-01-01 12:00:00.000000');

-- ORD010 Details
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD010', 1, 'GOODS034', 'Server Rack', 1, 250000, 250000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD010', 2, 'GOODS035', 'UPS Unit', 2, 180000, 360000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD010', 3, 'GOODS036', 'Network Switch', 4, 70000, 280000, '2025-01-01 12:00:00.000000');
