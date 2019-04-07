

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
			references metadata,
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







with metaRows as (insert into metadata (is_valid, when_entered, create_user)
values (true, localtimestamp, 0)
returning rid)
insert into client_attribute_type (id, name, metadata_id)
values ('d183c1b3-3e30-464b-9b7e-651e95d18edc', 'name', (select rid from metaRows limit 1));


with metaRows as (insert into metadata (is_valid, when_entered, create_user)
values (true, localtimestamp, 0)
returning rid)
insert into client_attribute_type_attrib (client_attribute_type_id,
                                          display_name,
                                          type,
                                          required,
                                          required_for_onboarding,
                                          metadata_id,
                                          ordering)
values ('d183c1b3-3e30-464b-9b7e-651e95d18edc',
        'Full Name',
        'string',
        true,
        true,
        (select rid from metaRows limit 1),
        1)
;

with metaRows as (insert into metadata (is_valid, when_entered, create_user)
values (true, localtimestamp, 0)
returning rid)
insert into client_attribute_type (id, name, metadata_id)
values ('db201da3-67f7-4c18-b240-47ef1b1cdb8d', 'date_of_birth', (select rid from metaRows limit 1));


with metaRows as (insert into metadata (is_valid, when_entered, create_user)
values (true, localtimestamp, 0)
returning rid)
insert into client_attribute_type_attrib (client_attribute_type_id,
                                          display_name,
                                          type,
                                          required,
                                          required_for_onboarding,
                                          metadata_id,
                                          ordering)
values ('db201da3-67f7-4c18-b240-47ef1b1cdb8d',
        'Date of Birth',
        'date',
        true,
        true,
        (select rid from metaRows limit 1),
        2)
;