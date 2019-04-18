with rows as (
     insert into metadata (is_valid, when_entered, create_user) VALUES (true, current_timestamp, 0) returning rid
    )
INSERT INTO canonical_type (id, type, metadata_id)
VALUES ('1490fa65-4851-45f3-a47d-d88c9cc92b61', 'client', (select rid from rows limit 1));


with rows as (
     insert into metadata (is_valid, when_entered, create_user) VALUES (true, current_timestamp, 0) returning rid
    )
INSERT INTO canonical_type (id, type, metadata_id)
VALUES ('7dc661f9-6739-4f89-99f9-bfd099915666', 'client_attribute_type', (select rid from rows limit 1));

