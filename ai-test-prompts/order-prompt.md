## Business Screen Development Request

### Feature Name
Order Management

### Module Name
ordermng

### Header Table
```sql
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
```

### Detail Table
```sql
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
```

### Code Value Definitions
- status_cs: 10=Draft, 20=Ordered, 30=Received, 90=Cancelled

### Screen Type
list + edit (header-detail structure)

### Search Conditions (List Screen)
- Order No: prefix match
- Supplier Code: exact match
- Supplier Name: partial match
- Order Date From-To: range search
- Status: exact match (select box)

### List Display Items
- Order No
- Order Date
- Supplier Code
- Supplier Name
- Delivery Date
- Status
- Total Amount

### Edit Screen (Header Section)
- Order No (auto-generated for new, read-only for edit)
- Order Date (required, date)
- Supplier Code (required)
- Supplier Name (required)
- Delivery Date (date)
- Status (required, select box)
- Remarks (textarea)

### Edit Screen (Detail Section)
- Line No (row number, auto-generated)
- Goods Code (required)
- Goods Name (required)
- Quantity (required, numeric)
- Unit Price (required, numeric)
- Amount (auto-calculated: Quantity × Unit Price)
- Delete Row button
- Add Row button

### Validation (Edit Screen)
- Order Date: required
- Supplier Code: required, alphanumeric 6 digits
- Supplier Name: required
- Status: required
- Goods Code: required, alphanumeric up to 10 digits
- Goods Name: required
- Quantity: required, positive integer
- Unit Price: required, positive number

### Business Logic
- Total Amount is auto-calculated by summing detail amounts
- Detail Amount is auto-calculated as Quantity × Unit Price
- At least 1 detail row is required

### Generation Request
Generate the following files with the above requirements:
- pages/app/ordermng/listpage.html
- pages/app/ordermng/listpage.js
- pages/app/ordermng/editpage.html
- pages/app/ordermng/editpage.js
- src/com/example/app/service/ordermng/package-info.java
- src/com/example/app/service/ordermng/OrderListInit.java
- src/com/example/app/service/ordermng/OrderListSearch.java
- src/com/example/app/service/ordermng/OrderLoad.java
- src/com/example/app/service/ordermng/OrderUpsert.java
- src/com/example/app/service/ordermng/OrderDelete.java

Make the detail table editable together with the header on the edit screen.
Follow the implementation patterns in docs/02-develop-standards/21-event-coding-pattern.md.
