ALTER TABLE mapper.host_matches
  DROP CONSTRAINT FK_a8bm2k5e2sy584b4cv0jjda3n;

ALTER TABLE mapper.host_matches
  DROP CONSTRAINT FK_p3tagks9u9hyrdk2i95mv0dp9;

ALTER TABLE mapper.identifier
  DROP CONSTRAINT FK_k2o53uoslf9gwqrd80cu2al4s;

ALTER TABLE mapper.identifier_identities
  DROP CONSTRAINT FK_mf2dsc2dxvsa9mlximsct7uau;

ALTER TABLE mapper.identifier_identities
  DROP CONSTRAINT FK_ojfilkcwskdvvbggwsnachry2;

DROP TABLE IF EXISTS mapper.host CASCADE;

DROP TABLE IF EXISTS mapper.host_matches CASCADE;

DROP TABLE IF EXISTS mapper.identifier CASCADE;

DROP TABLE IF EXISTS mapper.identifier_identities CASCADE;

DROP TABLE IF EXISTS mapper.match CASCADE;

DROP SEQUENCE mapper.mapper_sequence;

CREATE TABLE mapper.host (
  id        INT8 DEFAULT nextval('mapper.mapper_sequence') NOT NULL,
  host_name VARCHAR(512)                                   NOT NULL,
  preferred BOOLEAN                                        NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id)
);

CREATE TABLE mapper.host_matches (
  match_id INT8 NOT NULL,
  host_id  INT8 NOT NULL,
  PRIMARY KEY (host_id, match_id)
);

CREATE TABLE mapper.identifier (
  id               INT8 DEFAULT nextval('mapper.mapper_sequence') NOT NULL,
  deleted          BOOLEAN                                        NOT NULL,
  id_number        INT8                                           NOT NULL,
  name_space       VARCHAR(255)                                   NOT NULL,
  object_type      VARCHAR(255)                                   NOT NULL,
  preferred_uri_id INT8,
  reason_deleted   VARCHAR(255),
  updated_at       TIMESTAMP WITH TIME ZONE,
  updated_by       VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE mapper.identifier_identities (
  identifier_id INT8 NOT NULL,
  match_id      INT8 NOT NULL,
  PRIMARY KEY (identifier_id, match_id)
);

CREATE TABLE mapper.match (
  id         INT8 DEFAULT nextval('mapper.mapper_sequence') NOT NULL,
  deprecated BOOLEAN                                        NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE,
  updated_by VARCHAR(255),
  uri        VARCHAR(255)                                   NOT NULL,
  PRIMARY KEY (id)
);

ALTER TABLE mapper.identifier
  ADD CONSTRAINT unique_name_space UNIQUE (id_number, object_type, name_space);

CREATE INDEX identifier_index
  ON mapper.identifier (id_number, name_space, object_type);

ALTER TABLE mapper.match
  ADD CONSTRAINT UK_2u4bey0rox6ubtvqevg3wasp9 UNIQUE (uri);

CREATE INDEX identity_uri_index
  ON mapper.match (uri);

ALTER TABLE mapper.host_matches
  ADD CONSTRAINT FK_a8bm2k5e2sy584b4cv0jjda3n
FOREIGN KEY (host_id)
REFERENCES mapper.host;

ALTER TABLE mapper.host_matches
  ADD CONSTRAINT FK_p3tagks9u9hyrdk2i95mv0dp9
FOREIGN KEY (match_id)
REFERENCES mapper.match;

ALTER TABLE mapper.identifier
  ADD CONSTRAINT FK_k2o53uoslf9gwqrd80cu2al4s
FOREIGN KEY (preferred_uri_id)
REFERENCES mapper.match;

ALTER TABLE mapper.identifier_identities
  ADD CONSTRAINT FK_mf2dsc2dxvsa9mlximsct7uau
FOREIGN KEY (match_id)
REFERENCES mapper.match;

ALTER TABLE mapper.identifier_identities
  ADD CONSTRAINT FK_ojfilkcwskdvvbggwsnachry2
FOREIGN KEY (identifier_id)
REFERENCES mapper.identifier;

CREATE SEQUENCE mapper.mapper_sequence;
