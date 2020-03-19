alter table user_account
add column create_time timestamp not null default now();

alter table user_account
add column latest_activity timestamp not null default now();