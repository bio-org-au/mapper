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

ALTER TABLE mapper.host_matches
  ADD CONSTRAINT FK_a8bm2k5e2sy584b4cv0jjda3n
FOREIGN KEY (host_id)
REFERENCES mapper.host;

ALTER TABLE mapper.host_matches
  ADD CONSTRAINT FK_p3tagks9u9hyrdk2i95mv0dp9
FOREIGN KEY (match_id)
REFERENCES mapper.match;

INSERT INTO mapper.host (host_name) VALUES ('biodiversity.org.au');
INSERT INTO mapper.host (host_name) VALUES ('biodiversity.org.au/boa');
INSERT INTO mapper.host (host_name, preferred) VALUES ('id.biodiversity.org.au', true);
INSERT INTO mapper.host (host_name) VALUES ('www.anbg.gov.au');

INSERT INTO mapper.host_matches (
  SELECT
    m.id AS match_id,
    h.id AS host_id
  FROM mapper.match m, mapper.host h
  WHERE h.host_name = 'biodiversity.org.au'
        AND m.uri LIKE 'apni.%'
);

INSERT INTO mapper.host_matches (
  SELECT
    m.id AS match_id,
    h.id AS host_id
  FROM mapper.match m, mapper.host h
  WHERE h.host_name = 'biodiversity.org.au/boa'
        AND m.uri LIKE '%/apni/%'
);

INSERT INTO mapper.host_matches (
  SELECT
    m.id AS match_id,
    h.id AS host_id
  FROM mapper.match m, mapper.host h
  WHERE h.host_name = 'id.biodiversity.org.au'
        AND m.uri LIKE '%/apni/%'
);

INSERT INTO mapper.host_matches (
  SELECT
    m.id AS match_id,
    h.id AS host_id
  FROM mapper.match m, mapper.host h
  WHERE h.host_name = 'www.anbg.gov.au'
        AND m.uri LIKE 'cgi-bin/apni?taxon_id=%'
);

INSERT INTO mapper.host_matches (
  SELECT
    DISTINCT
    (m.id) AS match_id,
    h.id   AS host_id
  FROM mapper.match m
    JOIN mapper.identifier_identities ii ON m.id = ii.match_id
    JOIN mapper.identifier i ON ii.identifier_id = i.id
    , mapper.host h
  WHERE h.host_name = 'id.biodiversity.org.au'
        AND m.uri = i.id_number :: TEXT
);

INSERT INTO mapper.host_matches (
  SELECT
    m.id AS match_id,
    h.id AS host_id
  FROM mapper.match m, mapper.host h
  WHERE h.host_name = 'id.biodiversity.org.au'
        AND NOT exists(SELECT 1
                       FROM mapper.host_matches hm
                       WHERE hm.match_id = m.id)
);