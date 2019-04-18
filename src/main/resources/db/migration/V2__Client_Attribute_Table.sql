
create table client_attribute_type
(
	rid serial not null
		constraint client_attribute_type_pkey
			primary key,
	id uuid
		constraint client_attribute_type_id_fkey
			references canonical_id (id),
	name varchar(255) not null,
	type varchar(255) not null,
	required boolean not null,
	ordering integer default 0 not null,
	metadata_id integer not null
		constraint client_attribute_type_metadata_id_fkey
			references metadata
)
;

alter table client_attribute_type owner to postgres
;
