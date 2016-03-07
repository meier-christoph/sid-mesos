
import com.datastax.spark.connector._
import com.datastax.spark.connector.streaming._
import com.datastax.spark.connector.writer._
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka.KafkaUtils
import org.json4s._
import org.json4s.jackson.JsonMethods._

object WalkerApp {
  implicit val formats = DefaultFormats

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Walker Spark Application")
        .set("spark.cassandra.connection.host", "cassandra-dcos-node.cassandra.dcos.mesos")
        .set("spark.cassandra.connection.port", "9042")
        .setMaster("local[*]")

    val ssc = new StreamingContext(conf, Seconds(1))
    ssc.checkpoint("checkpoint")

    val topics = Map("walker" -> 1) // thread per topic
    val streams = KafkaUtils.createStream(ssc, "master.mesos:2181", "spark-walker", topics).map(_._2)
    val results = streams
        .map(parse(_).extract[Data]) // parse json
        .map(it => (it.toGrid, 1)) // create tuple e.g. (A1, 1)
        .reduceByKeyAndWindow(_ + _, _ - _, Seconds(10), Seconds(2)) // sum tuples by key e.g. (A1, 5)
        .map(t => (t._1, System.currentTimeMillis(), t._2)) // add timestamp

    // FIXME: https://datastax-oss.atlassian.net/browse/SPARKC-339
    results.saveToCassandra("walker", "grid",
      SomeColumns("coord" as "_1", "ts" as "_2", "nb" as "_3"), // map tuple to columns
      WriteConf(ttl = TTLOption.constant(60))) // ttl 60 sec

    ssc.start()
    ssc.awaitTermination()
  }

  class Data(uid: String, x: Int, y: Int) {
    def toGrid: String = {
      s"${('A'.toInt + x / 10).toChar}${1 + y / 10}"
    }
  }

}
