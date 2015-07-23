alter table mapper.identifier add COLUMN deleted boolean default false not null;
alter table mapper.identifier add COLUMN reason_deleted varchar(255);
alter table mapper.identifier add COLUMN updated_at timestamp with time zone;
alter table mapper.identifier add COLUMN updated_by varchar(255);

alter table mapper.match add COLUMN updated_at timestamp with time zone;
alter table mapper.match add COLUMN updated_by varchar(255);

