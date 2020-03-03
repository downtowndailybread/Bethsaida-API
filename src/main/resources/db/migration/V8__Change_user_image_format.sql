alter table bethsaida.client
    add column tmp_client_photo uuid;
alter table bethsaida.client
    add column tmp_photo_id uuid;


update bethsaida.client
    set client_photo = null
where client_photo = '';

update bethsaida.client
    set client_photo_id = null
where client_photo_id = '';

update bethsaida.client
set tmp_client_photo = cast(client_photo as uuid)
where client_photo is not null;

update bethsaida.client
set
    tmp_photo_id = cast(client_photo_id as uuid)
where client_photo_id is not null;

alter table bethsaida.client
    drop column client_photo;

alter table bethsaida.client
    drop column client_photo_id;

alter table bethsaida.client
    rename column tmp_client_photo to client_photo;

alter table bethsaida.client
    rename column tmp_photo_id to photo_id;
