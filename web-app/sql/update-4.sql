ALTER TABLE mapper.host_matches
  DROP CONSTRAINT FK_a8bm2k5e2sy584b4cv0jjda3n;

ALTER TABLE mapper.host_matches
  DROP CONSTRAINT FK_p3tagks9u9hyrdk2i95mv0dp9;

DROP INDEX IF EXISTS mapper.match_host_index;

CREATE TABLE mapper.db_version (
  id INT8 NOT NULL,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS mapper.host_matches CASCADE;

CREATE TABLE mapper.match_host (
  match_hosts_id INT8,
  host_id        INT8
);

ALTER TABLE mapper.match_host
  ADD CONSTRAINT FK_3unhnjvw9xhs9l3ney6tvnioq
FOREIGN KEY (host_id)
REFERENCES mapper.host;

ALTER TABLE mapper.match_host
  ADD CONSTRAINT FK_iw1fva74t5r4ehvmoy87n37yr
FOREIGN KEY (match_hosts_id)
REFERENCES mapper.match;

CREATE INDEX match_host_index
  ON mapper.match_host (match_hosts_id);

INSERT INTO mapper.match_host (
  SELECT
    m.id AS match_hosts_id,
    h.id AS host_id
  FROM mapper.match m, mapper.host h
  WHERE h.host_name = 'biodiversity.org.au'
        AND m.uri LIKE 'apni.%'
);

INSERT INTO mapper.match_host (
  SELECT
    m.id AS match_hosts_id,
    h.id AS host_id
  FROM mapper.match m, mapper.host h
  WHERE h.host_name = 'biodiversity.org.au/boa'
        AND m.uri LIKE '%/apni/%'
);

INSERT INTO mapper.match_host (
  SELECT
    m.id AS match_hosts_id,
    h.id AS host_id
  FROM mapper.match m, mapper.host h
  WHERE h.host_name = 'id.biodiversity.org.au'
        AND m.uri LIKE '%/apni/%'
);

INSERT INTO mapper.match_host (
  SELECT
    m.id AS match_hosts_id,
    h.id AS host_id
  FROM mapper.match m, mapper.host h
  WHERE h.host_name = 'www.anbg.gov.au'
        AND m.uri LIKE 'cgi-bin/apni?taxon_id=%'
);

INSERT INTO mapper.match_host (
  SELECT
    DISTINCT
    (m.id) AS match_hosts_id,
    h.id   AS host_id
  FROM mapper.match m
    JOIN mapper.identifier_identities ii ON m.id = ii.match_id
    JOIN mapper.identifier i ON ii.identifier_id = i.id
    , mapper.host h
  WHERE h.host_name = 'id.biodiversity.org.au'
        AND m.uri = i.id_number :: TEXT
);

INSERT INTO mapper.match_host (
  SELECT
    m.id AS match_hosts_id,
    h.id AS host_id
  FROM mapper.match m, mapper.host h
  WHERE h.host_name = 'id.biodiversity.org.au'
        AND NOT exists(SELECT 1
                       FROM mapper.match_host hm
                       WHERE hm.match_hosts_id = m.id)
);

-- version
INSERT INTO db_version (id, version) VALUES (1, 4);
