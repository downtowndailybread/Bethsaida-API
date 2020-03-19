alter table ban
add column start timestamp not null;

alter table ban
rename column until to stop;