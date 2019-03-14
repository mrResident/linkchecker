DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS nodes;
DROP TABLE IF EXISTS edges;

CREATE TABLE book (
  id INTEGER PRIMARY KEY DEFAULT nextval('global_seq'),
  title VARCHAR NOT NULL,
  author VARCHAR NOT NULL
);

CREATE TABLE nodes (
  id INT PRIMARY KEY DEFAULT nextval('global_seq'),
  name VARCHAR NOT NULL,
  probability FLOAT DEFAULT 0.5 NOT NULL,
  counter INT DEFAULT 0 NOT NULL
);

CREATE TABLE edges (
  node1_id INT NOT NULL,
  node2_id INT NOT NULL,
  FOREIGN KEY (node1_id) REFERENCES nodes(id),
  FOREIGN KEY (node2_id) REFERENCES nodes(id),
  CHECK (node1_id <> node2_id),
  CONSTRAINT unique_edge UNIQUE (node1_id, node2_id)
);

INSERT INTO book(title, author)
VALUES ('Мастер и Маргарита', 'Булгаков'),
       ('Пикник у обочины', 'Братья Стругацкие'),
       ('Хроники Амбера', 'Роджер Желязны');