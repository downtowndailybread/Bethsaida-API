create table mail (
    rid serial not null primary key,
    id uuid not null,
    client_id uuid not null references client(id),
    start_date timestamp not null,
    end_date timestamp,
    input_user uuid references user_account(id)
)