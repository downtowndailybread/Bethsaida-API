alter table service_schedule rename to schedule;
alter table service_schedule_details rename to schedule_attribute;


create table bethsaida.event
(
    rid serial not null
        constraint event_pkey
            primary key,
    id uuid not null
        constraint event_id_key
            unique,
    service_id uuid not null
        constraint event_service_id_fkey
            references bethsaida.service (id),
    metadata_id integer
        constraint event_metadata_id_fkey
            references bethsaida.metadata
);

alter table bethsaida.event owner to postgres;

create table bethsaida.event_attribute
(
    rid serial not null,
    event_id uuid not null
        constraint event_attribute_event_id_fkey
            references bethsaida.event (id),
    start_time timestamp with time zone not null,
    end_time timestamp with time zone not null,
    capacity integer,
    schedule_creator uuid
        constraint event_attribute_schedule_creator_fkey
            references bethsaida.schedule (id),
    user_creator uuid
        constraint event_attribute_user_creator_fkey
            references bethsaida.user_account (id),
    metadata_id integer not null
        constraint event_attribute_metadata_id_fkey
            references bethsaida.metadata
);

alter table bethsaida.event_attribute owner to postgres;

alter table bethsaida.service_attribute add column default_capacity int;

alter table bethsaida.schedule_attribute add column capacity int;
