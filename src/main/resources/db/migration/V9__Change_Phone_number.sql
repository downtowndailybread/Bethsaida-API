alter table bethsaida.client
    drop column phone;

alter table bethsaida.client
    add column phone varchar(10);