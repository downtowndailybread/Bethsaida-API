create table user_info
(
	rid serial not null
		constraint user_info_pkey
			primary key,
	id uuid not null
		constraint user_info_id_key
			unique,
	metadata_id integer not null
		constraint user_info_metadata_id_fkey
			references metadata
)
;

alter table user_info owner to postgres
;

create table user_login_information
(
	rid serial not null
		constraint user_login_information_pkey
			primary key,
	user_id uuid
		constraint user_login_information_user_id_fkey
			references user_info (id),
	email varchar(500),
	hash_algorithm varchar(30),
	salt varchar(64),
	hash varchar(64),
	failed_attempts integer,
	failed_reset_token uuid,
	metadata_id integer not null
		constraint user_login_information_metadata_id_fkey
			references metadata
)
;

alter table user_login_information owner to postgres
;

create table user_login_attempt
(
	rid serial not null
		constraint user_login_attempt_pkey
			primary key,
	email varchar(500),
	success boolean not null,
	metadata_id integer not null
		constraint user_login_attempt_metadata_id_fkey
			references metadata
)
;

alter table user_login_attempt owner to postgres
;

