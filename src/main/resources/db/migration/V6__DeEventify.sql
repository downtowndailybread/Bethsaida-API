alter table bethsaida.metadata
    drop constraint metadata_create_user_fkey;

drop table bethsaida.client_attribute_type_attrib;

drop table bethsaida.client_attribute;

drop table bethsaida.client_attribute_type;

drop table bethsaida.client_meta_info;

drop table bethsaida.user_attribute;

drop table bethsaida.user_access;

drop table bethsaida.service_attribute;

drop table bethsaida.schedule_attribute;

drop table bethsaida.event_attribute;

drop table bethsaida.user_account;

drop table bethsaida.schedule;

drop table bethsaida.attendance_attribute;

drop table bethsaida.attendance;

drop table bethsaida.invitee_attribute;

drop table bethsaida.invitee;

drop table bethsaida.client;

drop table bethsaida.event;

drop table bethsaida.service;

drop table bethsaida.metadata;



create table metadata
(
    rid serial not null
        constraint metadata_pkey
            primary key,
    is_valid boolean not null,
    when_entered timestamp not null,
    create_user uuid
);

alter table metadata owner to postgres;

create table client_attribute_type
(
    rid serial not null primary key,
    id uuid not null unique,
    name varchar(255) not null unique,
    display_name varchar(255) not null,
    type varchar(255) not null,
    required boolean,
    required_for_onboarding boolean,
    ordering integer default 0 not null,
    metadata_id integer not null references metadata(rid)
);

create table client
(
    rid serial not null primary key,
    id uuid not null unique,
    active boolean not null,
    metadata_id integer not null references metadata(rid)
);

alter table client owner to postgres;

create table client_attribute
(
    rid serial not null	primary key,
    client_id uuid not null references client(id),
    client_attribute_type_id uuid not null references client_attribute_type (id),
    metadata_id integer not null references metadata(rid),
    value json
);

CREATE UNIQUE INDEX client_attribute_client_and_type_unique ON client_attribute (client_id, client_attribute_type_id);

ALTER TABLE client_attribute
    ADD CONSTRAINT client_attribute_client_and_type_unique_constraint
        UNIQUE USING INDEX client_attribute_client_and_type_unique;

create table user_account
(
    rid serial not null	primary key,
    id uuid not null unique,
    email varchar(500) not null unique,
    name varchar(500) not null,
    salt varchar(90),
    hash varchar(90),
    confirmed boolean not null,
    admin_lock boolean not null,
    user_lock boolean not null,
    reset_token uuid,
    metadata_id integer not null references metadata(rid)
);


create table service
(
    rid serial not null primary key,
    id uuid not null unique,
    name varchar(255) not null,
    type varchar(255) not null,
    default_capacity integer,
    metadata_id integer references metadata(rid)
);

create table schedule
(
    rid serial not null primary key,
    id uuid not null unique,
    service_id uuid not null references service (id),
    rrule varchar(500) not null,
    start_time time not null,
    service_parameters json,
    metadata_id integer not null references metadata(rid)
);

create table event
(
    rid serial not null primary key,
    id uuid not null unique,
    start_time timestamp with time zone not null,
    end_time timestamp with time zone not null,
    capacity integer,
    service_id uuid not null references service (id),
    schedule_creator uuid references schedule (id),
    user_creator uuid references user_account (id),
    name varchar(255),
    metadata_id integer references bethsaida.metadata
);


create table attendance
(
    rid serial not null primary key,
    id uuid not null unique,
    check_in_time timestamp with time zone,
    check_out_time timestamp with time zone,
    event_id uuid not null references bethsaida.event (id),
    client_id uuid references bethsaida.client (id),
    metadata_id integer references metadata(rid)
);

create table invitee
(
    rid serial not null primary key,
    id uuid not null unique,
    client_id uuid not null references client (id),
    event_id uuid not null references event (id),
    required boolean not null,
    metadata_id integer not null references bethsaida.metadata
);


