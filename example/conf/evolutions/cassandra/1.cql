
# --- !Ups

CREATE KEYSPACE IF NOT EXISTS example WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};

# --- !Downs

DROP KEYSPACE example;
