drop table if exists t_user;
drop table if exists t_user_pet;

create table t_user (
 user_id varchar(4)
,user_nm varchar(20)
,email varchar(50)
,country_cs varchar(2)
,gender_cs varchar(1)
,spouse_cs varchar(1)
,income_am numeric(10)
,birth_dt date
,upd_ts timestamp(6)
,primary key (user_id)
);

create table t_user_pet (
 user_id varchar(4)
,pet_no numeric(2)
,pet_nm varchar(10)
,type_cs varchar(2)
,gender_cs varchar(1)
,vaccine_cs varchar(1)
,weight_kg numeric(3,1)
,birth_dt date
,upd_ts timestamp(6)
,primary key (user_id, pet_no)
);

delete from t_user;
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U001', 'Mike Davis', 'mike.davis@example.com', 'US', 'M', 'N', 10000000, '1975-02-10', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U002', 'IKEDA Ken', 'ikeda.ken@example.jp', 'JP', 'M', 'N', 8000000, '1999-05-15', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U003', 'OKADA Yumi', 'okada.yumi@example.jp', 'JP', 'F', 'N', 6000000, '1999-08-20', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U004', 'Lucy Smith', 'lucy.smith@example.us', 'US', 'F', 'N', 7500000, '1997-12-24', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U005', 'Judy Brown', 'judy.brown@example.us', 'US', 'F', 'N', 7200000, '1999-04-15', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U006', 'Ellen Baker', 'ellen.baker@example.us', 'US', 'F', 'Y', 9000000, '1985-07-25', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U007', 'Mike Baker', 'mike.baker@example.us', 'US', 'M', 'N', 9500000, '1988-11-30', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U008', 'Anne Green', 'anne.green@example.us', 'US', 'F', 'Y', 8500000, '1980-02-15', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U009', 'GOTO Saki', 'goto.saki@example.jp', 'JP', 'F', 'N', 6000000, '1988-08-20', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U010', 'TAMURA Daichi', 'tamura.daichi@example.jp', 'JP', 'M', 'N', 8000000, '1988-05-15', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U011', 'Lucas Costa', 'lucas.costa@example.br', 'BR', 'M', 'N', 9000000, '1988-12-10', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U012', 'Sophia Jones', 'sophia.jones@example.au', 'AU', 'F', 'N', 7500000, '1989-03-05', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U013', 'DAIDOJI Michiko', 'daidoji.michiko@example.jp', 'JP', 'F', 'Y', 8000000, '1974-08-03', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U014', 'MIKAMI Shunpei', 'mikami.shunpei@example.jp', 'JP', 'M', 'Y', 8000000, '1972-06-06', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U015', 'SUGAIKE Tadao', 'sugaike.tadao@example.jp', 'JP', 'M', 'Y', 8000000, '2000-01-01', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U016', 'SAKAGAMI Shinobu', 'sakagami.shinobu@example.jp', 'JP', 'M', 'Y', 100000000, '1987-06-01', '2025-01-01 12:00:00.000000');

delete from t_user_pet;
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U001', 1, 'Buddy', 'DG', 'M', 'Y', 5.0, '2015-01-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U001', 2, 'Whiskers', 'CT', 'F', 'N', 2.5, '2016-01-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U002', 1, 'Mittens', 'CT', 'F', 'Y', 4.1, '2017-01-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U003', 1, 'Snowy', 'DG', 'M', 'Y', 6.3, '2014-01-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U004', 1, 'Leo', 'DG', 'M', 'N', 7.2, '2013-01-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U005', 1, 'Peach', 'CT', 'F', 'Y', 3.8, '2016-01-01', '2025-01-01 12:00:00.000000'); 
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U006', 1, 'Daisy', 'DG', 'F', 'Y', 4.5, '2015-01-01', '2025-01-01 12:00:00.000000'); 
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U007', 1, 'Max', 'DG', 'M', 'N', 8.0, '2014-01-01', '2025-01-01 12:00:00.000000'); 
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U008', 1, 'Luna', 'CT', 'F', 'Y', 2.9, '2017-01-01', '2025-01-01 12:00:00.000000'); 
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U009', 1, 'Tiny', 'DG', 'M', 'Y', 5.4, '2015-01-01', '2025-01-01 12:00:00.000000'); 
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U010', 1, 'Shadow', 'CT', 'M', 'N', 3.6, '2016-01-01', '2025-01-01 12:00:00.000000'); 
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U011', 1, 'Bobby', 'DG', 'M', 'Y', 7.5, '2014-01-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U012', 1, 'Mimi', 'CT', 'F', 'Y', 2.7, '2017-01-01', '2025-01-01 12:00:00.000000'); 
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U013', 1, 'Sakura', 'DG', 'F', 'Y', 4.0, '2015-01-01', '2025-01-01 12:00:00.000000'); 
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U014', 1, 'Rocky', 'DG', 'M', 'N', 6.8, '2014-01-01', '2025-01-01 12:00:00.000000'); 
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U015', 1, 'Caro', 'DG', 'F', 'Y', 2.6, '2009-01-07', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U015', 2, 'Nico', 'DG', 'F', 'Y', 3.2, '2012-03-20', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 1, 'Charlie', 'DG', 'M', 'Y', 3.2, '2010-01-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 2, 'Cooper', 'DG', 'M', 'Y', 3.2, '2011-02-11', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 3, 'Duke', 'DG', 'M', 'Y', 3.2, '2012-03-21', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 4, 'Tucker', 'DG', 'M', 'Y', 3.2, '2013-04-30', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 5, 'Bella', 'DG', 'F', 'Y', 3.2, '2014-05-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 6, 'Oliver', 'CT', 'M', 'N', 3.2, '2015-06-11', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 7, 'Simba', 'CT', 'M', 'N', 3.2, '2016-07-21', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 8, 'Cleo', 'CT', 'F', 'N', 3.2, '2017-08-31', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 9, 'Willow', 'CT', 'F', 'N', 3.2, '2018-09-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 10, 'Oscar', 'CT', 'M', 'N', 3.2, '2019-10-11', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 11, 'Felix', 'CT', 'M', 'N', 3.2, '2020-11-21', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 12, 'Jasper', 'CT', 'M', 'N', 3.2, '2021-12-31', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 13, 'Ginger', 'CT', 'M', 'N', 3.2, '2022-01-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 14, 'Misty', 'CT', 'F', 'N', 3.2, '2023-02-11', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 15, 'Sky', 'CT', 'M', 'N', 3.2, '2024-03-21', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 16, 'Tiger', 'CT', 'M', 'N', 3.2, '2025-04-30', '2025-01-01 12:00:00.000000');
