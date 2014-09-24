/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helenaedelson.spark.cassandra.basic

import scala.concurrent.duration._
import kafka.serializer.StringDecoder
import org.apache.spark.SparkEnv
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.StreamingContext.toPairDStreamFunctions
import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.SomeColumns
import com.datastax.spark.connector.embedded._
import com.helenaedelson.spark.cassandra._

object KafkaStreamingBlueprint extends StreamingBlueprint {

  /** Creates the keyspace and table in Cassandra. */
  CassandraConnector(conf).withSessionDo { session =>
    session.execute(s"CREATE KEYSPACE IF NOT EXISTS streaming_test WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 }")
    session.execute(s"CREATE TABLE IF NOT EXISTS streaming_test.key_value (key VARCHAR PRIMARY KEY, value INT)")
    session.execute(s"TRUNCATE streaming_test.key_value")
  }

  private val topic = "topic1"

  /** Starts the Kafka broker. */
  lazy val kafka = new EmbeddedKafka

  /** Creates the Spark Streaming context. */
  val ssc =  new StreamingContext(sc, Seconds(2))

  SparkEnv.get.actorSystem.registerOnTermination(kafka.shutdown())

  val sent =  Map("a" -> 5, "b" -> 3, "c" -> 10)
  kafka.createTopic(topic)
  kafka.produceAndSendMessage(topic, sent)

  /** Creates an input stream that pulls messages from a Kafka Broker. */
  val stream = KafkaUtils.createStream[String, String, StringDecoder, StringDecoder](
    ssc, kafka.kafkaParams, Map(topic -> 1), StorageLevel.MEMORY_ONLY)

  /* Defines the work to do in the stream. Placing the import here to explicitly show
   that this is where the implicits are used for the DStream's 'saveToCassandra' functions: */
  import com.datastax.spark.connector.streaming._

  stream.map { case (_, v) => v }
    .map(x => (x, 1))
    .reduceByKey(_ + _)
    .saveToCassandra("streaming_test", "key_value", SomeColumns("key", "value"), 1)

  ssc.start()

  val rdd = ssc.cassandraTable("streaming_test", "key_value").select("key", "value")


  awaitCond(rdd.collect.size == sent.size, 5.seconds)

  val rows = rdd.collect
  sent.forall { rows.contains(_)}

  log.info(s"Assertions successful, shutting down.")
  ssc.stop(stopSparkContext = true, stopGracefully = false)

}
