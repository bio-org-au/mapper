
    alter table mapper.identifier 
        drop constraint FK_k2o53uoslf9gwqrd80cu2al4s;

    alter table mapper.identifier_identities 
        drop constraint FK_ojfilkcwskdvvbggwsnachry2;

    alter table mapper.identifier_identities 
        drop constraint FK_mf2dsc2dxvsa9mlximsct7uau;

    drop table if exists mapper.identifier cascade;

    drop table if exists mapper.identifier_identities cascade;

    drop table if exists mapper.match cascade;

    drop sequence mapper.mapper_sequence;

    create table mapper.identifier (
        id int8 default nextval('mapper.mapper_sequence') not null,
        deleted boolean not null,
        id_number int8 not null,
        name_space varchar(255) not null,
        object_type varchar(255) not null,
        preferred_uri_id int8,
        reason_deleted varchar(255),
        updated_at timestamp with time zone,
        updated_by varchar(255),
        primary key (id)
    );

    create table mapper.identifier_identities (
        match_id int8 not null,
        identifier_id int8 not null,
        primary key (identifier_id, match_id)
    );

    create table mapper.match (
        id int8 default nextval('mapper.mapper_sequence') not null,
        deprecated boolean not null,
        updated_at timestamp with time zone,
        updated_by varchar(255),
        uri varchar(255) not null,
        primary key (id)
    );

    alter table mapper.identifier 
        add constraint unique_name_space  unique (id_number, object_type, name_space);

    create index identifier_index on mapper.identifier (id_number, name_space, object_type);

    alter table mapper.match 
        add constraint UK_2u4bey0rox6ubtvqevg3wasp9  unique (uri);

    create index identity_uri_index on mapper.match (uri);

    alter table mapper.identifier 
        add constraint FK_k2o53uoslf9gwqrd80cu2al4s 
        foreign key (preferred_uri_id) 
        references mapper.match;

    alter table mapper.identifier_identities 
        add constraint FK_ojfilkcwskdvvbggwsnachry2 
        foreign key (identifier_id) 
        references mapper.identifier;

    alter table mapper.identifier_identities 
        add constraint FK_mf2dsc2dxvsa9mlximsct7uau 
        foreign key (match_id) 
        references mapper.match;

    create sequence mapper.mapper_sequence;
