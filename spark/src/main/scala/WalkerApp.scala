import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object WalkerApp {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Walker Spark Application")
        .setMaster("local")

    val ssc = new StreamingContext(conf, Seconds(1))
    ssc.checkpoint("checkpoint")

    //    val kafkaConf = Map(
    //      "zookeeper.connect" -> "192.168.99.100:2181",
    //      "group.id" -> "walker",
    //      "zookeeper.session.timeout.ms" -> "400",
    //      "zookeeper.sync.time.ms" -> "200",
    //      "auto.commit.interval.ms" -> "1000"
    //    )

    val topics = Map("walker" -> 1)
    val streams = KafkaUtils.createStream(ssc, "192.168.99.100:2181", "spark-walker", topics).map(_._2)
    val messages = streams // .map(m => Map("uid" -> "abc", "x" -> 10, "y" -> 20))
    streams.print()

    //    val wordCounts = words.map(x => (x, 1L))
    //        .reduceByKeyAndWindow(_ + _, _ - _, Seconds(10), Seconds(2), 2)

    ssc.start()
    ssc.awaitTermination()
  }
}
