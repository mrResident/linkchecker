DROP TABLE IF EXISTS nodes CASCADE;
DROP TABLE IF EXISTS edges;
DROP SEQUENCE IF EXISTS global_seq CASCADE;

CREATE SEQUENCE global_seq START 5000;

CREATE TABLE nodes (
    id INTEGER PRIMARY KEY DEFAULT nextval('global_seq'),
    name VARCHAR NOT NULL,
    probability INT DEFAULT 50 NOT NULL,
    counter INTEGER DEFAULT 0 NOT NULL
);
CREATE UNIQUE INDEX nodes_unique_name_idx ON nodes(name);

CREATE TABLE edges (
    id INTEGER PRIMARY KEY DEFAULT nextval('global_seq'),
    nodeone INT NOT NULL,
    nodetwo INT NOT NULL,
    FOREIGN KEY (nodeone) REFERENCES nodes(id) ON DELETE CASCADE,
    FOREIGN KEY (nodetwo) REFERENCES nodes(id) ON DELETE CASCADE,
    CHECK (nodeone <> nodetwo),
    CONSTRAINT unique_edge UNIQUE (nodeone, nodetwo)
);