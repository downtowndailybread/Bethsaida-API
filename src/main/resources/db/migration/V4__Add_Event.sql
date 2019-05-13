create table bethsaida.event
(
    rid serial not null,
    id uuid not null,
    start_time timestamp with time zone not null,
    end_time timestamp with time zone not null,
    capacity integer,
    service_id uuid not null
        constraint event_service_id_fkey
            references bethsaida.service (id),
    schedule_creator uuid
        constraint event_schedule_creator_fkey
            references bethsaida.service_schedule (id),
    user_creator uuid
        constraint event_user_creator_fkey
            references bethsaida.user_account (id),
    metadata_id integer not null
        constraint event_metadata_id_fkey
            references bethsaida.metadata
);

alter table bethsaida.event owner to postgres;

