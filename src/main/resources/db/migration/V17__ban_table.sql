create table ban (
                     rid serial not null primary key,
                     id uuid not null,
                     client_id uuid not null,
                     type varchar(2) not null,
                     until timestamp null,
                     user_id uuid not null,
                     notes text
)
