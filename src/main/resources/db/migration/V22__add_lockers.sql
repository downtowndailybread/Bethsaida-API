create table locker (
    rid serial not null primary key,
    id uuid not null,
    client_id uuid not null references client(id),
    locker_number varchar(8) not null,
    start_date timestamp not null,
    expected_end_date timestamp not null,
    end_date timestamp,
    input_user uuid references user_account(id)
)