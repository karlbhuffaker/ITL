CREATE TABLE itl.user_profile (
profile_id int unsigned auto_increment not null,
user_id varchar(32) not null,
user_name varchar(50) not null,
user_group varchar(50) not null,
email_address varchar(50) not null,
status varchar(32) not null,
create_timestamp timestamp not null,
update_timestamp timestamp,
update_user_id varchar(32),
allowed_vm_total int,
current_vm_total int,
primary key (profile_id)
);