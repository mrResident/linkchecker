DELETE FROM edges;
DELETE FROM nodes;

ALTER SEQUENCE global_seq RESTART WITH 5000;

INSERT INTO nodes (name) VALUES
('v1'),
('v2'),
('v3'),
('v4'),
('v5');

INSERT INTO edges (nodeOne, nodeTwo) VALUES
(5000, 5001),
(5000, 5002),
(5000, 5004),
(5002, 5003);