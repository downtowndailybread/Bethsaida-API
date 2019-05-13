alter table event_attribute
    add column name varchar(255);

alter table client alter column metadata_id set not null;

alter table schedule_attribute
    drop column enabled, drop column end_time, drop column capacity;

alter table schedule_attribute
    add column service_parameters json;

alter table user_access drop column reset_token;
alter table user_access add column reset_token uuid;

create table bethsaida.attendance
(
    rid serial not null
        constraint attendance_pkey
            primary key,
    id uuid not null
        constraint attendance_id_key
            unique,
    event_id uuid not null
        constraint attendance_event_id_fkey
            references bethsaida.event (id),
    client_id uuid
        constraint attendance_client_id_fkey
            references bethsaida.client (id),
    metadata_id integer
        constraint attendance_metadata_id_fkey
            references bethsaida.metadata
);

alter table bethsaida.attendance owner to postgres;

create table bethsaida.attendance_attribute
(
    rid serial not null
        constraint attendance_attribute_pkey
            primary key,
    attendance_id uuid not null
        constraint attendance_attribute_attendance_id_fkey
            references bethsaida.attendance (id),
    check_in_time timestamp with time zone,
    check_out_time timestamp with time zone,
    metadata_id integer
        constraint attendance_attribute_metadata_id_fkey
            references bethsaida.metadata
);

alter table bethsaida.attendance_attribute owner to postgres;


create table bethsaida.invitee
(
    rid serial not null
        constraint invitee_pkey
            primary key,
    id uuid not null
        constraint invitee_id_key
            unique,
    client_id uuid not null
        constraint invitee_client_id_fkey
            references bethsaida.client (id),
    event_id uuid not null
        constraint invitee_event_id_fkey
            references bethsaida.event (id),
    metadata_id integer not null
        constraint invitee_metadata_id_fkey
            references bethsaida.metadata
);

alter table bethsaida.invitee owner to postgres;

create table bethsaida.invitee_attribute
(
    rid serial not null
        constraint invitee_attribute_pkey
            primary key,
    invitee_id uuid not null
        constraint invitee_attribute_invitee_id_fkey
            references bethsaida.invitee (id),
    required boolean not null,
    metadata_id integer not null
        constraint invitee_attribute_metadata_id_fkey
            references bethsaida.metadata
);

alter table bethsaida.invitee_attribute owner to postgres;



