
# --- !Ups

CREATE TABLE IF NOT EXISTS example.table1 (
  id varchar primary key,
  value text
);

# --- !Downs

DROP TABLE example.table1;
