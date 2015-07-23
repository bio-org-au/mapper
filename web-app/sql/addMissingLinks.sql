INSERT INTO mapper.identifier (name_space, object_type, id_number)
  SELECT
    'apni',
    'name',
    n.id
  FROM name n where not exists (select 1 from mapper.identifier i WHERE n.id = i.id_number);

INSERT INTO mapper.identifier (name_space, object_type, id_number)
  SELECT
    'apni',
    'instance',
    n.id
  FROM instance n where not exists (select 1 from mapper.identifier i WHERE n.id = i.id_number);

INSERT INTO mapper.identifier (name_space, object_type, id_number)
  SELECT
    'apni',
    'author',
    n.id
  FROM author n where not exists (select 1 from mapper.identifier i WHERE n.id = i.id_number);

INSERT INTO mapper.identifier (name_space, object_type, id_number)
  SELECT
    'apni',
    'reference',
    n.id
  FROM reference n where not exists (select 1 from mapper.identifier i WHERE n.id = i.id_number);

INSERT INTO mapper.identifier (name_space, object_type, id_number)
  SELECT
    'apni',
    'instanceNote',
    n.id
  FROM instance_note n where not exists (select 1 from mapper.identifier i WHERE n.id = i.id_number);


INSERT INTO mapper.match (uri)
  SELECT object_type || '/' || name_space || '/' || id_number
  FROM mapper.identifier where not exists (select 1 from mapper.match WHERE uri = object_type || '/' || name_space || '/' || id_number);

INSERT INTO mapper.identifier_identities (match_id, identifier_id)
  (SELECT
     m.id,
     i.id
   FROM mapper.match m, mapper.identifier i
   WHERE m.uri = i.object_type || '/' || i.name_space || '/' || i.id_number
         and not exists (select 1 from mapper.identifier_identities ii where ii.identifier_id = i.id and ii.match_id = m.id)
  );

INSERT INTO mapper.match (uri)
  SELECT id_number :: TEXT
  FROM mapper.identifier where not exists (select 1 from mapper.match WHERE uri = id_number :: TEXT);

INSERT INTO mapper.identifier_identities (match_id, identifier_id)
  (SELECT
     m.id,
     i.id
   FROM mapper.match m, mapper.identifier i
   WHERE m.uri = i.id_number :: TEXT
         and not exists (select 1 from mapper.identifier_identities ii where ii.identifier_id = i.id and ii.match_id = m.id)
  );
