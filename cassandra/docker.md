## Run CQLSH

$ docker run -it --link cassandra --rm --entrypoint=sh spotify/cassandra:latest -c 'exec cqlsh "$CASSANDRA_PORT_9160_TCP_ADDR"'

## Create Table

```
DESC KEYSPACES;

CREATE KEYSPACE walker WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 2 };

CREATE TABLE walker.grid (
coord text,
ts timestamp,
nb int,
PRIMARY KEY (coord,ts)
) WITH CLUSTERING ORDER BY (ts DESC);

DESC SCHEMA;
```

## Insert some data and start using it

```
USE walker;
INSERT INTO grid(coord,ts,nb) VALUES ('A1',toTimestamp(now()),15) USING TTL 20;
INSERT INTO grid(coord,ts,nb) VALUES ('A2',toTimestamp(now()),2) USING TTL 20;
INSERT INTO grid(coord,ts,nb) VALUES ('B1',toTimestamp(now()),7) USING TTL 20;
INSERT INTO grid(coord,ts,nb) VALUES ('C1',toTimestamp(now()),18) USING TTL 20;

INSERT INTO grid(coord,ts,nb) VALUES ('A1',dateOf(now()),15) USING TTL 20;
INSERT INTO grid(coord,ts,nb) VALUES ('A2',dateOf(now()),2) USING TTL 20;
INSERT INTO grid(coord,ts,nb) VALUES ('B1',dateOf(now()),7) USING TTL 20;
INSERT INTO grid(coord,ts,nb) VALUES ('C1',dateOf(now()),18) USING TTL 20;

SELECT * from grid;
```
