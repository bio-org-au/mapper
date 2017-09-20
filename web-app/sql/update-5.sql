-- add version number support for trees
ALTER TABLE mapper.identifier
  DROP CONSTRAINT unique_name_space;

ALTER TABLE mapper.identifier
  ADD COLUMN version_number INT8;

ALTER TABLE mapper.identifier
  ADD CONSTRAINT unique_name_space UNIQUE (version_number, id_number, object_type, name_space);

CREATE INDEX identifier_version_index
  ON mapper.identifier (id_number, name_space, object_type, version_number);

CREATE INDEX identifier_prefuri_index
  ON mapper.identifier (preferred_uri_id);

-- version
UPDATE mapper.db_version
SET version = 5
WHERE id = 1;
