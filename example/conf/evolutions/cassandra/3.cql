
# --- !Ups

ALTER TABLE example.table1 add value2 text;

# --- !Downs

ALTER TABLE example.table1 drop value2;
