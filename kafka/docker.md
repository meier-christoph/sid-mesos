## Using Docker (local)

$ docker exec -it kafka bash

Create topic
$ cd /opt/kafka_2.11-0.8.2.1/bin
$ ./kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic walker

List topics (optional)
$ ./kafka-topics.sh --list --zookeeper localhost:2181

Produce messages from shell
$ ./kafka-console-producer.sh --broker-list localhost:9092 --topic walker

Monitor incoming messages
$ ./kafka-console-consumer.sh --zookeeper localhost:2181 --topic walker --from-beginning

Sample message:
{"uid":"foobar","x":1,"y":1}
