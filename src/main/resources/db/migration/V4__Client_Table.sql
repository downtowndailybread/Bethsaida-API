
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

