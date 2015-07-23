--create schema mapper;


alter table if exists mapper.identifier_identities
drop constraint if exists FK_mf2dsc2dxvsa9mlximsct7uau;

alter table if exists mapper.identifier_identities
drop constraint if exists FK_ojfilkcwskdvvbggwsnachry2;

drop table if exists mapper.identifier cascade;

drop table if exists mapper.identifier_identities cascade;

drop table if exists mapper.match cascade;

drop sequence mapper.mapper_sequence;
create sequence mapper.mapper_sequence;

create table mapper.identifier (
  id int8 default nextval('mapper.mapper_sequence') not null,
  id_number int8 not null,
  name_space varchar(255) not null,
  object_type varchar(255) not null,
  primary key (id)
);

create table mapper.identifier_identities (
  identifier_id int8 not null,
  match_id int8 not null,
  primary key (identifier_id, match_id)
);

create table mapper.match (
  id int8 default nextval('mapper.mapper_sequence') not null,
  deprecated boolean not null DEFAULT FALSE ,
  uri varchar(255) not null,
  primary key (id)
);

alter table mapper.identifier
add constraint unique_name_space  unique (id_number, object_type, name_space);

create index identifier_index on mapper.identifier (id_number, name_space, object_type);

alter table mapper.match
add constraint UK_2u4bey0rox6ubtvqevg3wasp9  unique (uri);

create index identity_uri_index on mapper.match (uri);

alter table mapper.identifier_identities
add constraint FK_mf2dsc2dxvsa9mlximsct7uau
foreign key (match_id)
references mapper.match;

alter table mapper.identifier_identities
add constraint FK_ojfilkcwskdvvbggwsnachry2
foreign key (identifier_id)
references mapper.identifier;

create index Mapper_identifier_Index on mapper.identifier_identities (identifier_id);
create index Mapper_match_Index on mapper.identifier_identities (match_id);
