package com.helenaedelson.spark.cassandra.streaming.news

import _root_.kafka.serializer.StringDecoder
import com.datastax.spark.connector._
import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.embedded._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka.KafkaUtils

object StreamingDemo {
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
    session.execute(s"DROP TABLE IF EXISTS meetup.word_counts")
    session.execute(s"CREATE TABLE meetup.word_counts (time TIMESTAMP, word TEXT, count INT, PRIMARY KEY (time, count, word)) WITH CLUSTERING ORDER BY (count DESC, word ASC)")
  }


  def main(args: Array[String]) {
    /** Creates the Spark Streaming context. */
    val ssc = new StreamingContext(sc, Milliseconds(200))

    ssc.checkpoint("/tmp")

    /** Creates an input stream that pulls messages from a Kafka Broker. */
    val stream = KafkaUtils.createStream[String, String, StringDecoder, StringDecoder](
      ssc, kafkaParams, Map("news" -> 1), StorageLevel.MEMORY_ONLY)

    case class WordCount(time: Long, word: String, count: Int)

    val topWordsStream = stream.map { case (_, paragraph) => paragraph}
      .flatMap(_.split( """\s+"""))
      .countByValueAndWindow(Seconds(5), Seconds(1))
      .transform((rdd, time) => rdd
        .map { case (word, count) =>
          (count.toInt, WordCount(time.milliseconds, word, count.toInt)) }
        .sortByKey(ascending = false)
        .values
      )

    topWordsStream.foreachRDD(_.saveToCassandra("meetup", "word_counts"))
    topWordsStream.print()

    ssc.start()

    Thread.sleep(30000)

    ssc.stop(stopSparkContext = true, stopGracefully = false)
    ssc.awaitTermination()

  }

}
