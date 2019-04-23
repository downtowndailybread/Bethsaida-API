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

create table canonical_type
(
	rid serial not null
		constraint canonical_type_pkey
			primary key,
	id uuid not null,
	type varchar(255) not null,
	metadata_id integer not null
		constraint canonical_type_metadata_id_fkey
			references metadata
)
;

alter table canonical_type owner to postgres
;

create unique index canonical_type_id
	on canonical_type (id)
;

create table canonical_id
(
	rid serial not null
		constraint canonical_id_pkey
			primary key,
	id uuid,
	type uuid
		constraint canonical_id_type_fkey
			references canonical_type (id),
	metadata_id integer not null
		constraint canonical_id_metadata_id_fkey
			references metadata
)
;

alter table canonical_id owner to postgres
;

create unique index canonical_id_id
	on canonical_id (id)
;

create table client_attribute_type
(
	rid serial not null
		constraint client_attribute_type_pkey
			primary key,
	id uuid
		constraint client_attribute_type_id_fkey
			references canonical_id (id),
	name varchar(255) not null,
	display_name varchar(255),
	type varchar(255) not null,
	required boolean not null,
	required_for_onboarding boolean,
	ordering integer default 0 not null,
	metadata_id integer not null
		constraint client_attribute_type_metadata_id_fkey
			references metadata
)
;

alter table client_attribute_type owner to postgres
;


with rows as (
     insert into metadata (is_valid, when_entered, create_user) VALUES (true, current_timestamp, 0) returning rid
    )
INSERT INTO canonical_type (id, type, metadata_id)
VALUES ('1490fa65-4851-45f3-a47d-d88c9cc92b61', 'client', (select rid from rows limit 1));


with rows as (
     insert into metadata (is_valid, when_entered, create_user) VALUES (true, current_timestamp, 0) returning rid
    )
INSERT INTO canonical_type (id, type, metadata_id)
VALUES ('7dc661f9-6739-4f89-99f9-bfd099915666', 'client_attribute_type', (select rid from rows limit 1));




create table client_attribute
(
	rid serial not null
		constraint client_attribute_pkey
			primary key,
	id uuid
		constraint client_attribute_id_fkey
			references canonical_id (id),
	client_id uuid
		constraint client_attribute_client_id_fkey
			references canonical_id (id),
	type_id uuid,
	metadata_id integer not null
		constraint client_attribute_metadata_id_fkey
			references metadata,
	value json
)
;

alter table client_attribute owner to postgres
;

