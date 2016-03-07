
// edit metadata

{
  "name": "walker",
  "user_save_timestamp": "1970-01-01T00:00:00.000Z",
  "auto_save_timestamp": "1970-01-01T00:00:00.000Z",
  "language_info": {
    "name": "scala",
    "file_extension": "scala",
    "codemirror_mode": "text/x-scala"
  },
  "trusted": true,
  "customLocalRepo": "/tmp/repo",
  "customRepos": null,
  "customDeps": [
    "org.apache.spark %% spark-streaming-kafka % 1.6.0",
    "org.apache.spark %% spark-sql % 1.6.0",
    "com.datastax.spark %% spark-cassandra-connector % 1.5.0",
    "com.google.guava % guava % 19.0"
  ],
  "customImports": null,
  "customArgs": null,
  "customSparkConf": {
    "spark.cassandra.connection.host": "cassandra-dcos-node.cassandra.dcos.mesos"
  },
  "kernelspec": {
    "name": "spark",
    "display_name": "Scala [2.10.4] Spark [1.5.0] Hadoop [2.5.0-cdh5.3.3]  {Hive ✓}  {Parquet ✓}"
  }
}

// ============================================================

import org.apache.spark.streaming._
val ssc = new StreamingContext(sc,  Seconds(1))
ssc.checkpoint("checkpoint")


// serialization errors ???
object model extends Serializable { 
  object Data {
    def parse(s: String): Data = {
      Data("foobar", 1, 1)
    }
  }
  case class Data(uid: String, x: Int, y: Int) {
    def toGrid: String = {
      s"${('A'.toInt + x / 10).toChar}${1 + y / 10}"
    }
  }
}
import model._


import org.apache.spark.streaming.kafka.KafkaUtils
val topics = Map("walker" -> 1)
val streams = KafkaUtils.createStream(ssc, "master.mesos:2181", "spark-walker", topics).map(_._2)


val results = streams
  .map(it => ("A1", 1)) // workaround
  .reduceByKeyAndWindow(_ + _, _ - _, Seconds(30), Seconds(10))
  .map(t => (t._1, System.currentTimeMillis(), t._2))


import com.datastax.spark.connector._
import com.datastax.spark.connector.streaming._
import com.datastax.spark.connector.writer._
results.saveToCassandra("walker", "grid",
  SomeColumns("coord" as "_1", "ts" as "_2", "nb" as "_3"),
  WriteConf(ttl = TTLOption.constant(60)))


ssc.start()
ssc.awaitTermination()
