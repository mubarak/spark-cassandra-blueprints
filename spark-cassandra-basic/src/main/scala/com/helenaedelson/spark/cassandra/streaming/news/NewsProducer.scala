package com.helenaedelson.spark.cassandra.streaming.news

import java.net.InetAddress
import java.util.Properties

import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.embedded.EmbeddedKafka
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import kafka.serializer.StringEncoder

import scala.util.Random

object NewsProducer {

  import scala.collection.JavaConversions._

  lazy val kafka = new EmbeddedKafka

  def main(args: Array[String]) {

    val connector = CassandraConnector(host = InetAddress.getByName("localhost"))
    val query = "SELECT domain, content FROM meetup.news"

    kafka.createTopic("news")

    val p = new Properties()
    p.put("metadata.broker.list", kafka.kafkaConfig.hostName + ":" + kafka.kafkaConfig.port)
    p.put("serializer.class", classOf[StringEncoder].getName)

    val producer = new Producer[String, String](new ProducerConfig(p))

    connector.withSessionDo { session =>
      val rs = session.execute(query).iterator()
      rs.foreach { row =>
        val domain = row.getString("domain")
        val content = row.getString("content")
        producer.send(KeyedMessage("news", domain, content))
        println(s"Sent $domain / $content")
        Thread.sleep(Random.nextInt(100))
      }
    }

    producer.close()

    kafka.shutdown()
  }


}
