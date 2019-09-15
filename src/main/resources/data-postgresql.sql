DELETE FROM edges;
DELETE FROM nodes;

ALTER SEQUENCE global_seq RESTART WITH 5000;

INSERT INTO nodes (name, probability, counter) VALUES
('v1', 43, 0),
('v2', 60, 0),
('v3', 35, 0),
('v4', 56, 0),
('v5', 20, 0);

INSERT INTO edges (nodeOne, nodeTwo) VALUES
(5000, 5001),
(5000, 5002),
(5000, 5004),
(5002, 5003);