create unique index event_unique_service_and_date
    on bethsaida.event(service_id, date);

alter table bethsaida.event
    add constraint unique_event_date
        unique using index event_unique_service_and_date