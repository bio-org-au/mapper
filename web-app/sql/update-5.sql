-- add version number support for trees
ALTER TABLE mapper.identifier
  DROP CONSTRAINT unique_name_space;

ALTER TABLE mapper.identifier
  ADD COLUMN version_number INT8;

ALTER TABLE mapper.identifier
  ADD CONSTRAINT unique_name_space UNIQUE (version_number, id_number, object_type, name_space);

-- version
update mapper.db_version set version = 5 where id = 1;
