alter table bethsaida.client
    add column race varchar(100) null;

alter table bethsaida.client
    add column phone bigint null;

alter table bethsaida.client
    add column gender varchar(100) null;

alter table bethsaida.client
    rename column photo_id_tag to client_photo;

alter table bethsaida.client
    add column client_photo_id varchar(100) null;

