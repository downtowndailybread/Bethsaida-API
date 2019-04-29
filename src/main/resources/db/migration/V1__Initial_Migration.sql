create table metadata
(
	rid serial not null
		constraint metadata_pkey
			primary key,
	is_valid boolean not null,
	when_entered timestamp not null,
	create_user integer not null
)
;

alter table metadata owner to postgres
;

create table client_attribute_type
(
	rid serial not null
		constraint client_attribute_type_pkey
			primary key,
	id uuid not null
		constraint client_attribute_type_id_key
			unique,
	name varchar(255) not null,
	metadata_id integer not null
		constraint client_attribute_type_metadata_id_fkey
			references metadata
)
;

alter table client_attribute_type owner to postgres
;

create unique index client_attribute_type_name_uindex
	on client_attribute_type (name)
;





create table client_attribute_type_attrib
(
	rid serial not null
		constraint client_attribute_type_attribs_pkey
			primary key,
	client_attribute_type_id uuid
		constraint client_attribute_type_attribs_client_attribute_type_fkey
			references client_attribute_type (id) not null,
	display_name varchar(255),
	type varchar(255) not null,
	required boolean not null,
	required_for_onboarding boolean,
	ordering integer default 0 not null,
	metadata_id integer not null
		constraint client_attribute_type_attrib_metadata_id_fkey
			references metadata
)
;

alter table client_attribute_type_attrib owner to postgres
;

create table client
(
	rid serial not null
		constraint client_pkey
			primary key,
	id uuid not null
		constraint client_id_key
			unique,
	metadata_id integer
		constraint client_metadata_id_fkey
			references metadata
)
;

alter table client owner to postgres
;

create table client_attribute
(
	rid serial not null
		constraint client_attribute_pkey
			primary key,
	client_id uuid not null
		constraint client_attribute_client_id_fkey
			references client (id),
	client_attribute_type_id uuid not null
		constraint client_attribute_client_attribute_type_id_fkey
			references client_attribute_type (id),
	metadata_id integer not null
		constraint client_attribute_metadata_id_fkey
			references metadata(rid),
	value json
)
;

alter table client_attribute owner to postgres
;



create table client_meta_info
(
	rid serial not null
		constraint client_meta_info_pkey
			primary key,
	client_id uuid
		constraint client_meta_info_client_id_fkey
			references client (id),
	active boolean not null
)
;

alter table client_meta_info owner to postgres
;

create table b_user
(
	rid serial not null
		constraint user_info_pkey
			primary key,
	id uuid not null
		constraint user_info_id_key
			unique,
	metadata_id integer not null references metadata(rid)
)
;

alter table b_user owner to postgres
;

create table user_attribute
(
	rid serial not null
		constraint user_attribute_pkey
			primary key,
	user_id uuid not null
		constraint user_attribute_user_id_fkey
			references b_user (id),
	email varchar(500) not null,
	name varchar(500) not null,
	metadata_id int not null references metadata(rid)
)
;

alter table user_attribute owner to postgres
;

create table user_access
(
	rid serial not null
		constraint user_access_pkey
			primary key,
	user_id uuid not null
		constraint user_access_user_id_fkey
			references b_user (id),
	salt varchar(90),
	hash varchar(90),
	confirmed boolean not null,
	admin_lock boolean not null,
	user_lock boolean not null,
	reset_token uuid not null,
	metadata_id int not null references metadata(rid)
)
;

alter table user_access owner to postgres
;

