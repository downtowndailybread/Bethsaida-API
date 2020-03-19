create table image
(
    rid serial not null primary key,
    id uuid,
    created_by uuid references user_account(id),
    created_time timestamp,
    nickname varchar(200)
);