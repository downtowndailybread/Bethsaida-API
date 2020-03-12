create unique index note_id
    on bethsaida.note(id);

alter table bethsaida.note
    add constraint unique_id
        unique using index note_id