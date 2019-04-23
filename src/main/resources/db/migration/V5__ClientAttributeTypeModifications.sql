alter table client_attribute_type
    add column display_name varchar(200),
    add column required_for_onboarding boolean;