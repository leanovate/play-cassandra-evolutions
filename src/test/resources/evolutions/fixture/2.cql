
# --- !Ups

CREATE TABLE IF NOT EXISTS test.table1 (
  id varchar primary key
);

# --- !Downs

DROP TABLE test.table1;
