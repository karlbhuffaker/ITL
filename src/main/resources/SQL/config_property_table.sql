CREATE TABLE itl.config_property (
property_id int unsigned auto_increment not null,
property_group varchar(256) not null,
property_name varchar(256) not null,
property_value varchar(256) not null,
property_order int not null,
primary key (property_id)
);