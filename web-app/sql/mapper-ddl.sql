
    alter table if exists mapper.identifier 
        drop constraint if exists FK_k2o53uoslf9gwqrd80cu2al4s;

    alter table if exists mapper.identifier_identities 
        drop constraint if exists FK_ojfilkcwskdvvbggwsnachry2;

    alter table if exists mapper.identifier_identities 
        drop constraint if exists FK_mf2dsc2dxvsa9mlximsct7uau;

    alter table if exists mapper.match_host 
        drop constraint if exists FK_3unhnjvw9xhs9l3ney6tvnioq;

    alter table if exists mapper.match_host 
        drop constraint if exists FK_iw1fva74t5r4ehvmoy87n37yr;

    drop table if exists mapper.db_version cascade;

    drop table if exists mapper.host cascade;

    drop table if exists mapper.identifier cascade;

    drop table if exists mapper.identifier_identities cascade;

    drop table if exists mapper.match cascade;

    drop table if exists mapper.match_host cascade;

    drop sequence mapper.hibernate_sequence;
    create sequence mapper.hibernate_sequence;

    drop sequence mapper.mapper_sequence;
    create sequence mapper.mapper_sequence;

    create table mapper.db_version (
        id int8 not null,
        primary key (id)
    );

    create table mapper.host (
        id int8 default nextval('mapper.mapper_sequence') not null,
        host_name varchar(512) not null,
        preferred boolean default false not null,
        primary key (id)
    );

    create table mapper.identifier (
        id int8 default nextval('mapper.mapper_sequence') not null,
        deleted boolean default false not null,
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
        deprecated boolean default false not null,
        updated_at timestamp with time zone,
        updated_by varchar(255),
        uri varchar(255) not null,
        primary key (id)
    );

    create table mapper.match_host (
        match_hosts_id int8,
        host_id int8
    );

    alter table if exists mapper.identifier 
        add constraint unique_name_space  unique (id_number, object_type, name_space);

    create index identifier_index on mapper.identifier (id_number, name_space, object_type);

    alter table if exists mapper.match 
        add constraint UK_2u4bey0rox6ubtvqevg3wasp9  unique (uri);

    create index identity_uri_index on mapper.match (uri);

    alter table if exists mapper.identifier 
        add constraint FK_k2o53uoslf9gwqrd80cu2al4s 
        foreign key (preferred_uri_id) 
        references mapper.match;

    alter table if exists mapper.identifier_identities 
        add constraint FK_ojfilkcwskdvvbggwsnachry2 
        foreign key (identifier_id) 
        references mapper.identifier;

    alter table if exists mapper.identifier_identities 
        add constraint FK_mf2dsc2dxvsa9mlximsct7uau 
        foreign key (match_id) 
        references mapper.match;

    alter table if exists mapper.match_host 
        add constraint FK_3unhnjvw9xhs9l3ney6tvnioq 
        foreign key (host_id) 
        references mapper.host;

    alter table if exists mapper.match_host 
        add constraint FK_iw1fva74t5r4ehvmoy87n37yr 
        foreign key (match_hosts_id) 
        references mapper.match;

    

    
