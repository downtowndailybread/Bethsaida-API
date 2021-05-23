create table report (
                      rid serial not null primary key,
                      id uuid not null,
                      report_name varchar not null,
                      report_title varchar not null,
                      report_sql varchar not null
)

