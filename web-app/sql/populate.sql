-- specific NSL name identifiers
INSERT INTO mapper.identifier (name_space, object_type, id_number)
  SELECT
    'apni',
    'name',
    id
  FROM name;

-- specific NSL author identifiers
INSERT INTO mapper.identifier (name_space, object_type, id_number)
  SELECT
    'apni',
    'author',
    id
  FROM author;

-- specific NSL reference identifiers
INSERT INTO mapper.identifier (name_space, object_type, id_number)
  SELECT
    'apni',
    'reference',
    id
  FROM reference;

-- specific NSL instance identifiers
INSERT INTO mapper.identifier (name_space, object_type, id_number)
  SELECT
    'apni',
    'instance',
    id
  FROM instance;

-- specific NSL instance_note identifiers
INSERT INTO mapper.identifier (name_space, object_type, id_number)
  SELECT
    'apni',
    'instancenote',
    id
  FROM instance_note;

-- Specific mapping
INSERT INTO mapper.match (uri)
  SELECT object_type || '/' || name_space || '/' || id_number
  FROM mapper.identifier;

INSERT INTO mapper.identifier_identities (match_id, identifier_id)
  (SELECT
     m.id,
     i.id
   FROM mapper.match m, mapper.identifier i
   WHERE m.uri = i.object_type || '/' || i.name_space || '/' || i.id_number
  );

-- map distinct full names to apni objects
INSERT INTO mapper.match (uri)
  SELECT DISTINCT (full_name)
  FROM name;

-- full name to name objects
INSERT INTO mapper.identifier_identities (match_id, identifier_id)
  (SELECT
     m.id,
     i.id
   FROM mapper.match m, mapper.identifier i, name n
   WHERE n.full_name = m.uri
         AND n.id = i.id_number
         AND i.name_space = 'apni'
         AND i.object_type = 'name'
  );

-- full name to instance objects
INSERT INTO mapper.identifier_identities (match_id, identifier_id)
  (SELECT
     m.id,
     i.id
   FROM mapper.match m, mapper.identifier i, name n, instance inst
   WHERE n.full_name = m.uri
         AND inst.name_id = n.id
         AND inst.id = i.id_number
         AND i.name_space = 'apni'
         AND i.object_type = 'instance'
  );

-- NSL ID to resource mapping
INSERT INTO mapper.match (uri)
  SELECT id_number :: TEXT
  FROM mapper.identifier;

INSERT INTO mapper.identifier_identities (match_id, identifier_id)
  (SELECT
     m.id,
     i.id
   FROM mapper.match m, mapper.identifier i
   WHERE m.uri = i.id_number :: TEXT
  );

-- cgi-bin/apni?taxon_id=old_id
INSERT INTO mapper.match (uri)
  SELECT DISTINCT ('cgi-bin/apni?taxon_id=' || from_id)
  FROM id_mapper
  WHERE system = 'PLANT_NAME';

INSERT INTO mapper.identifier_identities (identifier_id, match_id)
  (SELECT
     DISTINCT
     (i.id),
     m.id
   FROM mapper.match m, mapper.identifier i, id_mapper idmapper
   WHERE m.uri = 'cgi-bin/apni?taxon_id=' || idmapper.from_id
         AND idmapper.system = 'PLANT_NAME'
         AND i.id_number = idmapper.to_id
  );

-- apni.name/old-id

INSERT INTO mapper.match (uri)
  SELECT DISTINCT ('apni.name/' || from_id)
  FROM id_mapper
  WHERE system = 'PLANT_NAME';

INSERT INTO mapper.identifier_identities (identifier_id, match_id)
  (SELECT
     DISTINCT
     (i.id),
     m.id
   FROM mapper.match m, mapper.identifier i, id_mapper idmapper
   WHERE m.uri = 'apni.name/' || idmapper.from_id
         AND idmapper.system = 'PLANT_NAME'
         AND i.id_number = idmapper.to_id
  );

-- apni.reference/old-id

INSERT INTO mapper.match (uri)
  SELECT DISTINCT ('apni.reference/' || from_id)
  FROM id_mapper
  WHERE system = 'REFERENCE';

INSERT INTO mapper.identifier_identities (identifier_id, match_id)
  (SELECT
     DISTINCT
     (i.id),
     m.id
   FROM mapper.match m, mapper.identifier i, id_mapper idmapper
   WHERE m.uri = 'apni.reference/' || idmapper.from_id
         AND idmapper.system = 'REFERENCE'
         AND i.id_number = idmapper.to_id
  );

-- apni.publication/old-id

INSERT INTO mapper.match (uri)
  SELECT DISTINCT ('apni.publication/' || from_id)
  FROM id_mapper
  WHERE system = 'PUBLICATION';

INSERT INTO mapper.identifier_identities (identifier_id, match_id)
  (SELECT
     DISTINCT
     (i.id),
     m.id
   FROM mapper.match m, mapper.identifier i, id_mapper idmapper
   WHERE m.uri = 'apni.publication/' || idmapper.from_id
         AND idmapper.system = 'PUBLICATION'
         AND i.id_number = idmapper.to_id
  );

-- apni.taxon/old-id

INSERT INTO mapper.match (uri)
  SELECT DISTINCT ('apni.taxon/' || from_id)
  FROM id_mapper
  WHERE system = 'PLANT_NAME_REFERENCE';

INSERT INTO mapper.identifier_identities (identifier_id, match_id)
  (SELECT
     DISTINCT
     (i.id),
     m.id
   FROM mapper.match m, mapper.identifier i, id_mapper idmapper
   WHERE m.uri = 'apni.taxon/' || idmapper.from_id
         AND idmapper.system = 'PLANT_NAME_REFERENCE'
         AND i.id_number = idmapper.to_id
  );

create index Mapper_identifier_Index on mapper.identifier_identities (identifier_id);
create index Mapper_match_Index on mapper.identifier_identities (match_id);
