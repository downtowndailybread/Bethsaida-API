
with metaRows as (insert into metadata (is_valid, when_entered, create_user)
values (true, localtimestamp, 0)
returning rid)
insert into client_attribute_type (id, name, metadata_id)
values ('d183c1b3-3e30-464b-9b7e-651e95d18edc', 'name', (select rid from metaRows limit 1));


with metaRows as (insert into metadata (is_valid, when_entered, create_user)
values (true, localtimestamp, 0)
returning rid)
insert into client_attribute_type_attrib (client_attribute_type_id,
                                          display_name,
                                          type,
                                          required,
                                          required_for_onboarding,
                                          metadata_id,
                                          ordering)
values ('d183c1b3-3e30-464b-9b7e-651e95d18edc',
        'Full Name',
        'string',
        true,
        true,
        (select rid from metaRows limit 1),
        1)
;

with metaRows as (insert into metadata (is_valid, when_entered, create_user)
values (true, localtimestamp, 0)
returning rid)
insert into client_attribute_type (id, name, metadata_id)
values ('db201da3-67f7-4c18-b240-47ef1b1cdb8d', 'date_of_birth', (select rid from metaRows limit 1));


with metaRows as (insert into metadata (is_valid, when_entered, create_user)
values (true, localtimestamp, 0)
returning rid)
insert into client_attribute_type_attrib (client_attribute_type_id,
                                          display_name,
                                          type,
                                          required,
                                          required_for_onboarding,
                                          metadata_id,
                                          ordering)
values ('db201da3-67f7-4c18-b240-47ef1b1cdb8d',
        'Date of Birth',
        'date',
        true,
        true,
        (select rid from metaRows limit 1),
        2)
;