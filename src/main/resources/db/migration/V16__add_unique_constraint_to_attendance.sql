create unique index attendance_unique_event_and_client
    on bethsaida.attendance(client_id, event_id);

alter table bethsaida.attendance
    add constraint unique_event_and_client
        unique using index attendance_unique_event_and_client