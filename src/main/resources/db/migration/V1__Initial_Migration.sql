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

