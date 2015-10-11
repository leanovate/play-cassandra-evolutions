
# --- !Ups

CREATE KEYSPACE IF NOT EXISTS test WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};

# --- !Downs

DROP KEYSPACE test;
