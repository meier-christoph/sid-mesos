# SID - Mesosphere

## Getting Started

Note: when testing locally with Docker make sure that you add the following entries into your /etc/hosts file.

```
192.168.99.100 master.mesos
192.168.99.100 cassandra-dcos-node.cassandra.dcos.mesos
192.168.99.100 broker-0.kafka.mesos
```

### Installation

- Install Mesosphere on AWS
  (https://mesosphere.com/amazon/)

    Note: this demo requires at least 3 slaves.

- Install DCOS

```
mkdir -p dcos && cd dcos && \
  curl -O https://downloads.mesosphere.io/dcos-cli/install.sh && \
  bash ./install.sh . http://mesos-elasticloadb-xxx.elb.amazonaws.com && \
  source ./bin/env-setup
```

- Add community packages

```
  dcos config prepend package.sources https://github.com/mesosphere/multiverse/archive/version-1.x.zip
  dcos config prepend package.sources https://github.com/data-fellas/multiverse/archive/spark-notebook.zip
  dcos package update --validate
```

- Install Chronos (scheduler)

```
  dcos package install chronos
```

- Install Spark (map/reduce)

```
  dcos package install spark
```

- Install Kafka 
  
```
  dcos package install kafka
```

- Install Cassandra (db)

```
  dcos package install cassandra
```

- Install Spark Notebook

```
  dcos package install --app spark-notebook --package-version=0.0.2
```

## Configuration

- Start 3 Kafka brokers on the cluster

```
  dcos kafka broker add 0..2
  dcos kafka broker update 0..2 --options num.io.threads=16,num.partitions=6,default.replication.factor=2
  dcos kafka broker start 0..2
```

- Create Cassandra table/keyspace

```
  dcos node ssh --master-proxy --master

  docker run -it --net=host --rm --entrypoint=/usr/bin/cqlsh spotify/cassandra cassandra-dcos-node.cassandra.dcos.mesos 9160

  CREATE KEYSPACE walker WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 2 };

  CREATE TABLE walker.grid (
  coord text,
  ts timestamp,
  nb int,
  PRIMARY KEY (coord,ts)
  ) WITH CLUSTERING ORDER BY (ts DESC);
```

- Start Producer

```
  dcos marathon app add producer/sid-mesos-kafka-producer.json
```

- Start Consumer

```
  dcos marathon app add consumer/sid-mesos-kafka-consumer.json      
```

- Start Spark Driver

```
  dcos spark run --submit-args='--class WalkerApp https://s3.eu-central-1.amazonaws.com/sid-mesos-spark-apps/spark-walker-app-assembly-1.0.jar'
```

- Start Grid

```
  dcos marathon app add grid/sid-mesos-cassandra-grid.json
```
