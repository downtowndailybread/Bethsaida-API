drop schema bethsaida cascade;

create schema bethsaida;

alter schema bethsaida owner to postgres;

create table flyway_schema_history
(
    installed_rank integer not null
        constraint flyway_schema_history_pk
            primary key,
    version varchar(50),
    description varchar(200) not null,
    type varchar(20) not null,
    script varchar(1000) not null,
    checksum integer,
    installed_by varchar(100) not null,
    installed_on timestamp default now() not null,
    execution_time integer not null,
    success boolean not null
);

alter table flyway_schema_history owner to postgres;

create index flyway_schema_history_s_idx
    on flyway_schema_history (success);


create table client
(
    rid serial not null
        constraint client_pkey
            primary key,
    id uuid not null
        constraint client_id_key
            unique,
    active boolean not null
);

alter table client owner to postgres;

create table user_account
(
    rid serial not null
        constraint user_account_pkey
            primary key,
    id uuid not null
        constraint user_account_id_key
            unique,
    email varchar(500) not null
        constraint user_account_email_key
            unique,
    name varchar(500) not null,
    salt varchar(90),
    hash varchar(90),
    confirmed boolean not null,
    admin_lock boolean not null,
    user_lock boolean not null,
    reset_token uuid
);

alter table user_account owner to postgres;

create table service
(
    rid serial not null
        constraint service_pkey
            primary key,
    id uuid not null
        constraint service_id_key
            unique,
    name varchar(255) not null,
    type varchar(255) not null,
    default_capacity integer
);

alter table service owner to postgres;

create table schedule
(
    rid serial not null
        constraint schedule_pkey
            primary key,
    id uuid not null
        constraint schedule_id_key
            unique,
    service_id uuid not null
        constraint schedule_service_id_fkey
            references service (id),
    rrule varchar(500) not null,
    start_time time not null,
    service_parameters json
);

alter table schedule owner to postgres;

create table event
(
    rid serial not null
        constraint event_pkey
            primary key,
    id uuid not null
        constraint event_id_key
            unique,
    start_time timestamp with time zone not null,
    end_time timestamp with time zone not null,
    capacity integer,
    service_id uuid not null
        constraint event_service_id_fkey
            references service (id),
    schedule_creator uuid
        constraint event_schedule_creator_fkey
            references schedule (id),
    user_creator uuid
        constraint event_user_creator_fkey
            references user_account (id),
    name varchar(255)
);

alter table event owner to postgres;

create table attendance
(
    rid serial not null
        constraint attendance_pkey
            primary key,
    id uuid not null
        constraint attendance_id_key
            unique,
    check_in_time timestamp with time zone,
    check_out_time timestamp with time zone,
    event_id uuid not null
        constraint attendance_event_id_fkey
            references event (id),
    client_id uuid
        constraint attendance_client_id_fkey
            references client (id)
);

alter table attendance owner to postgres;

create table invitee
(
    rid serial not null
        constraint invitee_pkey
            primary key,
    id uuid not null
        constraint invitee_id_key
            unique,
    client_id uuid not null
        constraint invitee_client_id_fkey
            references client (id),
    event_id uuid not null
        constraint invitee_event_id_fkey
            references event (id),
    required boolean not null
);

alter table invitee owner to postgres;

