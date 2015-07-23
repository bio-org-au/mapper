alter table mapper.match add column deprecated boolean not null DEFAULT FALSE;
update mapper.match set deprecated = true where uri like 'apni.%';
update mapper.match set deprecated = true where uri like 'cgi-bin%';