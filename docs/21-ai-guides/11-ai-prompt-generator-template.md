Generate a business screen prompt for a header-detail structure from the following table definitions.
Refer to sections 3.3 and 7.1 of docs/21-ai-guides/01-ai-prompt-guide.md for the prompt format.

Header table:
create table t_order (
  order_id varchar(10)
 ,order_dt date
 ,customer_id varchar(6)
 ,upd_ts timestamp(6)
 ,primary key (order_id)
);

Detail table:
create table t_order_detail (
  order_id varchar(10)
 ,line_no numeric(3)
 ,product_id varchar(6)
 ,quantity numeric(5)
 ,unit_price_am numeric(8)
 ,upd_ts timestamp(6)
 ,primary key (order_id, line_no)
);
