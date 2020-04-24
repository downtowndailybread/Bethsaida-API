alter table attendance
add column user_id uuid references user_account(id);

with a as(
select
id,
ROW_NUMBER () OVER (
    ORDER BY rid
) as row
from user_account
limit 1)
update attendance at
set user_id = a.id
from a;

alter table attendance alter column user_id set not null