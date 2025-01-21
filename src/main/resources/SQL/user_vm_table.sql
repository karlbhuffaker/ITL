CREATE TABLE itl.user_vm (
vm_id int unsigned auto_increment not null,
user_id varchar(32) not null,
vm_name varchar(16) not null,
primary key (vm_id)
);