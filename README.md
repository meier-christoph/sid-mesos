# SID - Mesosphere

## Getting Started

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
  $ dcos config prepend package.sources https://github.com/mesosphere/multiverse/archive/version-1.x.zip
  $ dcos config prepend package.sources https://github.com/data-fellas/multiverse/archive/spark-notebook.zip
  $ dcos package update --validate
  
- Install Chronos (scheduler)
  $ dcos package install chronos

- Install Spark (map/reduce)
  $ dcos package install spark

- Install Kafka   
  $ dcos package install kafka

- Install Cassandra (db)
  $ dcos package install cassandra

- Install Spark Notebook
  $ dcos package install --app spark-notebook --package-version=0.0.2

## Configuration

- Start 3 Kafka brokers on the cluster
  $ dcos kafka broker add 0..2
  $ dcos kafka broker update 0..2 --options num.io.threads=16,num.partitions=6,default.replication.factor=2
  $ dcos kafka broker start 0..2

- Start Producer
  $ dcos marathon app add producer/sid-mesos-kafka-producer.json

- Start Consumer
  $ dcos marathon app add consumer/sid-mesos-kafka-consumer.json      
