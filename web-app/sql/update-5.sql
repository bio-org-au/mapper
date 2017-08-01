-- add version number support for trees
ALTER TABLE mapper.identifier
  DROP CONSTRAINT unique_name_space;

ALTER TABLE mapper.identifier
  ADD COLUMN version_number INT8;

ALTER TABLE mapper.identifier
  ADD CONSTRAINT unique_name_space UNIQUE (version_number, id_number, object_type, name_space);

-- version
INSERT INTO mapper.db_version (id, version) VALUES (1, 5);
