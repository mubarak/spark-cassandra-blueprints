package com.helenaedelson.weather

import scala.concurrent.duration._
import akka.actor._
import akka.util.Timeout
import kafka.serializer.StringDecoder
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.StreamingContext.toPairDStreamFunctions
import org.apache.spark.streaming.kafka.KafkaUtils
import com.datastax.spark.connector.embedded.EmbeddedKafka
import com.datastax.spark.connector.streaming._ 

class NodeGuardian(ssc: StreamingContext, kafka: EmbeddedKafka, settings: WeatherSettings) extends Actor {
  import Weather._
  import settings._

  implicit val timeout = Timeout(5.seconds)

  context.actorOf(Props(new KafkaRawPublisher(ssc, settings, "raw_weather_data")), "raw-publisher")

  /** Creates an input stream that pulls messages from a Kafka Broker.
    * Accepts the streaming context, kafka connection properties, topic map and storage level.
    * Connects to the ZK cluster. */
  val stream = KafkaUtils.createStream[String, String, StringDecoder, StringDecoder](
    ssc, kafka.kafkaParams, Map(KafkaTopic -> 1), StorageLevel.MEMORY_ONLY)

  /* Defines the work to do in the stream. Retrieves data */
  stream.map { case (_, v) => v }
    .map(x => (x, 1))
    .reduceByKey(_ + _)
    .saveToCassandra(Keyspace, "raw_weather_data")

  ssc.start()

  override def postStop(): Unit = {

  }

  def receive: Actor.Receive = {
    case GetWeatherStation => raw(sender)
    case GetRawWeatherData =>
    case GetSkyConditionLookup =>
  }

  def raw(actor: ActorRef): Unit = {
    val rdd = ssc.cassandraTable[RawWeatherData](Keyspace, "raw_weather_data")
    rdd.toLocalIterator // TODO iterate and page
  }
}

/** Stores the raw data in Cassandra for multi-purpose data analysis.
  *
  * This just batches one data file for the demo. But you could do something like this
  * to set up a monitor on a directory, so that when new files arrive, Spark streams
  * them in. New files are read as text files with 'textFileStream' (using key as LongWritable,
  * value as Text and input format as TextInputFormat)
  * {{{
  *   ssc.textFileStream("dirname")
     .reduceByWindow(_ + _, Seconds(30), Seconds(1))
  * }}}
  *
  * Should make this a supervisor which partitions the workload to routees vs doing
  * all the work itself. But normally this would be done by other strategies anyway.
  */
class KafkaRawPublisher(ssc: StreamingContext, settings: WeatherSettings, tableName: String) extends Actor with ActorLogging {
  import settings._
  import com.datastax.spark.connector._

  // TODO finish this actor..
  ssc.sparkContext.textFile(RawDataFile)
    .flatMap(line => line.split(","))
    .saveToCassandra(Keyspace, tableName)

  def receive: Actor.Receive = {
    case _ =>
  }
}