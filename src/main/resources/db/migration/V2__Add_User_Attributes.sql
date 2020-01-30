alter table bethsaida.client
    add column first_name varchar(100);

alter table bethsaida.client
    add column last_name varchar(100);

alter table bethsaida.client
    add column date_of_birth timestamp;

alter table bethsaida.client
    add column photo_id_tag varchar(100);


create table client_alias
(
    rid serial not null
        constraint client_alias_pkey
            primary key,
    client_id uuid not null references bethsaida.client(id),
    nickname varchar(200)
);