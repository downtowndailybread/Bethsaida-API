create table note (
                      rid serial not null primary key,
                      id uuid not null,
                      note text not null
)