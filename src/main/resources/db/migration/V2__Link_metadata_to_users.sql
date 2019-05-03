alter table metadata drop column create_user;
alter table metadata add column create_user uuid;


alter table bethsaida.metadata
	add constraint metadata_create_user_fkey
		foreign key (create_user) references user_account (id);