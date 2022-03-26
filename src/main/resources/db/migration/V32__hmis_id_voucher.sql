alter table client
    drop column id_voucher;

alter table client
    add column id_voucher timestamp;

alter table client
    drop column hmis;

alter table client
    add column hmis int;

