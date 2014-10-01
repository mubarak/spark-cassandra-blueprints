package com.helenaedelson.spark.cassandra.streaming.news

import _root_.kafka.serializer._
import com.datastax.spark.connector._
import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.embedded._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils


object StreamingDemo2 {
  val kafkaParams = Map(
    "zookeeper.connect" -> ZookeeperConnectionString,
    "group.id" -> s"test-consumer-${scala.util.Random.nextInt(10000)}",
    "auto.offset.reset" -> "smallest")

  /** Configures Spark. */
  val sc = new SparkConf(true)
    .set("spark.cassandra.connection.host", "localhost")
    .set("spark.cleaner.ttl", "500")
    .setMaster("local[*]")
    .setAppName("Streaming Kafka App")

  /** Creates the keyspace and table in Cassandra. */
  CassandraConnector(sc).withSessionDo { session =>
    session.execute(s"CREATE KEYSPACE IF NOT EXISTS meetup WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 }")
    session.execute(s"DROP TABLE IF EXISTS meetup.word_counts_total")
    session.execute(s"CREATE TABLE meetup.word_counts_total (time TIMESTAMP, word TEXT, count INT, PRIMARY KEY (time, count, word)) WITH CLUSTERING ORDER BY (count DESC, word ASC)")
  }


  def main(args: Array[String]) {
    /** Creates the Spark Streaming context. */
    val ssc = new StreamingContext(sc, Milliseconds(200))

    ssc.checkpoint("/tmp")

    /** Creates an input stream that pulls messages from a Kafka Broker. */
    val stream = KafkaUtils.createStream[String, String, StringDecoder, StringDecoder](
      ssc, kafkaParams, Map("news" -> 1), StorageLevel.MEMORY_ONLY) : DStream[(String, String)]

    case class WordCount(time: Long, word: String, count: Int)

    def update(counts: Seq[Long], state: Option[Long]): Option[Long] = {
      val sum = counts.sum
      Some(state.getOrElse(0L) + sum)
    }


    val totalWords: DStream[(String, Long)] =
      stream.map { case (_, paragraph) => paragraph.toString}
        .flatMap(_.split( """\s+"""))
        .countByValue()
        .updateStateByKey(update)

    val topTotalWordCounts: DStream[WordCount] =
      totalWords.transform((rdd, time) =>
        rdd.map { case (word, count) =>
          (count, WordCount(time.milliseconds, word, count.toInt))
        }.sortByKey(ascending = false).values
      )

    topTotalWordCounts.foreachRDD(_.saveToCassandra("meetup", "word_counts_total"))
    topTotalWordCounts.print()

    ssc.start()

    Thread.sleep(30000)

    ssc.stop(stopSparkContext = true, stopGracefully = false)
    ssc.awaitTermination()

  }

}
