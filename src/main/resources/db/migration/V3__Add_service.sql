create table bethsaida.service
(
    rid serial not null
        constraint service_pkey
            primary key,
    id uuid not null
        constraint service_id_key
            unique,
    metadata_id integer
        constraint service_metadata_id_fkey
            references bethsaida.metadata
);

alter table bethsaida.service owner to postgres;

create table bethsaida.service_attribute
(
    rid serial not null
        constraint service_attribute_pkey
            primary key,
    service_id uuid not null
        constraint service_attribute_service_id_fkey
            references bethsaida.service (id),
    name varchar(255) not null,
    type varchar(255) not null,
    metadata_id integer
        constraint service_attribute_metadata_id_fkey
            references bethsaida.metadata
);

alter table bethsaida.service_attribute owner to postgres;


create table bethsaida.service_schedule
(
    rid serial not null
        constraint service_schedule_pkey
            primary key,
    id uuid not null
        constraint service_schedule_id_key
            unique,
    service_id uuid not null
        constraint service_schedule_service_id_fkey
            references bethsaida.service (id),
    metadata_id integer not null
        constraint service_schedule_metadata_id_fkey
            references bethsaida.metadata
);

alter table bethsaida.service_schedule owner to postgres;

create table bethsaida.service_schedule_details
(
    rid serial not null
        constraint service_schedule_details_pkey
            primary key,
    schedule_id uuid not null
        constraint service_schedule_details_schedule_id_key
            unique
        constraint service_schedule_details_schedule_id_fkey
            references bethsaida.service_schedule (id),
    rrule varchar(500) not null,
    enabled boolean not null,
    metadata_id integer not null
        constraint service_schedule_details_metadata_id_fkey
            references bethsaida.metadata
);

alter table bethsaida.service_schedule_details owner to postgres;



