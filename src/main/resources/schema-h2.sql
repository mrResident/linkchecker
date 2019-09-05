DROP TABLE IF EXISTS nodes CASCADE;
DROP TABLE IF EXISTS edges;
DROP SEQUENCE IF EXISTS global_seq;

CREATE SEQUENCE global_seq MINVALUE 5000;

CREATE TABLE nodes (
    id INT DEFAULT global_seq.nextval PRIMARY KEY,
    name VARCHAR NOT NULL,
    probability FLOAT DEFAULT 0.5 NOT NULL,
    counter INT DEFAULT 0 NOT NULL
);
CREATE UNIQUE INDEX nodes_unique_name_idx ON nodes(name);

CREATE TABLE edges (
    id INT DEFAULT global_seq.nextval PRIMARY KEY,
    nodeone INT NOT NULL,
    nodetwo INT NOT NULL,
    FOREIGN KEY (nodeone) REFERENCES nodes(id) ON DELETE CASCADE,
    FOREIGN KEY (nodetwo) REFERENCES nodes(id) ON DELETE CASCADE,
    CHECK (nodeone <> nodetwo),
    CONSTRAINT unique_edge UNIQUE (nodeone, nodetwo)
);