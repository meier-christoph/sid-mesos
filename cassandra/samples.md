## Run CQLSH

$ docker run -it --link cassandra --rm cassandra cqlsh cassandra

## Create Table

```
DESC KEYSPACES;

CREATE KEYSPACE walker WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 2 };

USE walker;

CREATE TABLE grid (
coord text,
ts timestamp,
nb int,
PRIMARY KEY (coord,ts)
) WITH CLUSTERING ORDER BY (ts DESC);

DESC SCHEMA;
```

## Insert some data and start using it

```
INSERT INTO grid(coord,ts,nb) VALUES ('A1',toTimestamp(now()),15) USING TTL 20;
INSERT INTO grid(coord,ts,nb) VALUES ('A2',toTimestamp(now()),2) USING TTL 20;
INSERT INTO grid(coord,ts,nb) VALUES ('B1',toTimestamp(now()),7) USING TTL 20;
INSERT INTO grid(coord,ts,nb) VALUES ('C1',toTimestamp(now()),18) USING TTL 20;


SELECT * from grid;
```
