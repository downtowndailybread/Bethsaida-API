create table bethsaida.event_attendance
(
    rid serial not null
        constraint event_attendance_pkey
            primary key,
    id uuid not null
        constraint event_attendance_id_key
            unique,
    event_id uuid not null
        constraint event_attendance_event_id_fkey
            references bethsaida.event (id),
    client_id uuid not null
        constraint event_attendance_client_id_fkey
            references bethsaida.client (id),
    metadata_id integer
        constraint event_attendance_metadata_id_fkey
            references bethsaida.metadata
);

alter table bethsaida.event_attendance owner to postgres;

create table bethsaida.event_attendance_attribute
(
    rid serial not null
        constraint event_attendance_attribute_pkey
            primary key,
    event_attendance_id uuid not null
        constraint event_attendance_attribute_event_attendance_id_fkey
            references bethsaida.event_attendance (id),
    check_in_time timestamp with time zone,
    metadata_id integer
        constraint event_attendance_attribute_metadata_id_fkey
            references bethsaida.metadata
);

alter table bethsaida.event_attendance_attribute owner to postgres;

create table bethsaida.appointment_attendance
(
    rid serial not null
        constraint appointment_attendance_pkey
            primary key,
    id uuid not null
        constraint appointment_attendance_id_key
            unique,
    service_id uuid not null
        constraint appointment_attendance_service_id_fkey
            references bethsaida.event (id),
    client_id uuid not null
        constraint appointment_attendance_client_id_fkey
            references bethsaida.client (id),
    metadata_id integer
        constraint appointment_attendance_metadata_id_fkey
            references bethsaida.metadata
);

alter table bethsaida.appointment_attendance owner to postgres;

create table bethsaida.appointment_attendance_attribute
(
    rid serial not null
        constraint appointment_attendance_attribute_pkey
            primary key,
    event_attendance_id uuid not null
        constraint appointment_attendance_attribute_event_attendance_id_fkey
            references bethsaida.appointment_attendance (id),
    start_time timestamp with time zone,
    end_time timestamp with time zone,
    metadata_id integer
        constraint appointment_attendance_attribute_metadata_id_fkey
            references bethsaida.metadata
);

alter table bethsaida.appointment_attendance_attribute owner to postgres;

