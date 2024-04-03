create table meal (
                      rid serial not null primary key,
                      id uuid not null,
                        date timestamp not null,
                        breakfast int not null,
                        lunch int not null
)