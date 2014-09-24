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

import java.util.Properties

import kafka.producer.{KeyedMessage, ProducerConfig, Producer}

import scala.concurrent.duration._
import kafka.serializer.{StringEncoder, StringDecoder}
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
  /** Implicits for the DStream's 'saveToCassandra' functions. */
  import com.datastax.spark.connector.streaming._

  val keyspaceName = "kafka_blueprints"
  val tableName = "wordcount"

  /** Creates the keyspace and table in Cassandra. */
  CassandraConnector(conf).withSessionDo { session =>
    session.execute(s"CREATE KEYSPACE IF NOT EXISTS $keyspaceName WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 }")
    session.execute(s"CREATE TABLE IF NOT EXISTS $keyspaceName.$tableName (key VARCHAR PRIMARY KEY, value INT)")
    session.execute(s"TRUNCATE $keyspaceName.$tableName")
  }

  private val topic = "topic1"

  /** Starts the Kafka broker. */
  lazy val kafka = new EmbeddedKafka

  /** Creates the Spark Streaming context. */
  val ssc =  new StreamingContext(sc, Seconds(2))

  SparkEnv.get.actorSystem.registerOnTermination(kafka.shutdown())

  /** Pushes messages to Kafka. */
  val sent =  Map("a" -> 5, "b" -> 3, "c" -> 10)
  kafka.createTopic(topic)
  kafka.produceAndSendMessage(topic, sent)

  /** Creates an input stream that pulls messages from a Kafka Broker.
    * Accepts the streaming context, kafka connection properties, topic map and storage level.
    * Connects to the ZK cluster. */
  val stream = KafkaUtils.createStream[String, String, StringDecoder, StringDecoder](
    ssc, kafka.kafkaParams, Map(topic -> 1), StorageLevel.MEMORY_ONLY)

  /* Defines the work to do in the stream. Retrieves data */
  stream.map { case (_, v) => v }
    .map(x => (x, 1))
    .reduceByKey(_ + _)
    .saveToCassandra(keyspaceName, tableName, SomeColumns("key", "value"), 1)

  ssc.start()

  val rdd = ssc.cassandraTable(keyspaceName, tableName).select("key", "value")
  awaitCond(rdd.collect.size == sent.size, 5.seconds)
  rdd.collect foreach (row => logInfo(s"$row"))

  logInfo(s"Assertions successful, shutting down.")
  ssc.stop(stopSparkContext = true, stopGracefully = false)
}


import java.io.File
import java.util.Properties

import scala.concurrent.duration.{Duration, _}
import org.apache.spark.Logging
import kafka.producer._
import kafka.admin.CreateTopicCommand
import kafka.common.TopicAndPartition
import kafka.producer.{KeyedMessage, ProducerConfig, Producer}
import kafka.serializer.StringEncoder
import kafka.server.{KafkaConfig, KafkaServer}
import kafka.utils.ZKStringSerializer
import org.I0Itec.zkclient.ZkClient

final class EmbeddedKafka extends Embedded with Logging {

  val kafkaParams = Map(
    "zookeeper.connect" -> ZookeeperConnectionString,
    "group.id" -> s"test-consumer-${scala.util.Random.nextInt(10000)}",
    "auto.offset.reset" -> "smallest")

  private val brokerConf = new Properties()
  brokerConf.put("broker.id", "0")
  brokerConf.put("host.name", "localhost")
  brokerConf.put("port", "9092")
  brokerConf.put("log.dir", createTempDir.getAbsolutePath)
  brokerConf.put("zookeeper.connect", ZookeeperConnectionString)
  brokerConf.put("log.flush.interval.messages", "1")
  brokerConf.put("replica.socket.timeout.ms", "1500")

  /** Starts the ZK server. */
  private val zookeeper = new EmbeddedZookeeper()
  awaitCond(zookeeper.isRunning, 2000.millis)

  log.info(s"Attempting to connect with $ZookeeperConnectionString")
  val client = new ZkClient(ZookeeperConnectionString, 6000, 6000, ZKStringSerializer)
  log.info(s"ZooKeeper Client connected.")

  log.info(s"Attempting to connect KafkaServer with $ZookeeperConnectionString")
  val kafkaConfig = new KafkaConfig(brokerConf)
  val server = new KafkaServer(kafkaConfig)
  Thread.sleep(2000)

  val p = new Properties()
  p.put("metadata.broker.list", kafkaConfig.hostName + ":" + kafkaConfig.port)
  p.put("serializer.class", classOf[StringEncoder].getName)
  val producer = new Producer[String, String](new ProducerConfig(p))

  log.info(s"Starting the Kafka server at $ZookeeperConnectionString")
  server.startup()
  Thread.sleep(2000)

  def createTopic(topic: String) {
    CreateTopicCommand.createTopic(client, topic, 1, 1, "0")
    awaitPropagation(Seq(server), topic, 0, 1000.millis)
  }

  def produceAndSendMessage(topic: String, sent: Map[String, Int]) {
    producer.send(createTestMessage(topic, sent): _*)
  }

  private def createTestMessage(topic: String, send: Map[String, Int]): Seq[KeyedMessage[String, String]] =
    (for ((s, freq) <- send; i <- 0 until freq) yield new KeyedMessage[String, String](topic, s)).toSeq

  def awaitPropagation(servers: Seq[KafkaServer], topic: String, partition: Int, timeout: Duration): Unit =
    awaitCond(
      p = servers.forall(_.apis.leaderCache.keySet.contains(TopicAndPartition(topic, partition))),
      max = timeout,
      message = s"Partition [$topic, $partition] metadata not propagated after timeout")

  def shutdown(): Unit = {
    log.info(s"Shutting down Kafka server.")
    producer.close()
    server.shutdown()
    server.config.logDirs.foreach(f => deleteRecursively(new File(f)))
    log.info(s"Shutting down ZK client.")
    client.close()
    zookeeper.shutdown()
    awaitCond(!zookeeper.isRunning, 2000.millis)
    log.info(s"ZooKeeper server shut down.")
  }
}

trait Embedded extends EmbeddedIO with Serializable with Assertions


trait EmbeddedIO {

  import java.io._
  import java.util.UUID

  import scala.language.reflectiveCalls
  import com.google.common.io.Files
  import org.apache.commons.lang3.SystemUtils

  val shutdownDeletePaths = new scala.collection.mutable.HashSet[String]()

  def createTempDir: File = {
    val dir = mkdir(new File(Files.createTempDir(), "spark-tmp-" + UUID.randomUUID.toString))
    registerShutdownDeleteDir(dir)

    Runtime.getRuntime.addShutdownHook(new Thread("delete Spark temp dir " + dir) {
      override def run() {
        if (! hasRootAsShutdownDeleteDir(dir)) deleteRecursively(dir)
      }
    })
    dir
  }

  /** Makes a new directory or throws an `IOException` if it cannot be made */
  def mkdir(dir: File): File = {
    if (!dir.mkdir()) throw new IOException(s"Could not create dir $dir")
    dir
  }

  def registerShutdownDeleteDir(file: File) {
    shutdownDeletePaths.synchronized {
      shutdownDeletePaths += file.getAbsolutePath
    }
  }

  def hasRootAsShutdownDeleteDir(file: File): Boolean = {
    val absolutePath = file.getAbsolutePath
    shutdownDeletePaths.synchronized {
      shutdownDeletePaths.exists { path =>
        !absolutePath.equals(path) && absolutePath.startsWith(path)
      }
    }
  }

  def deleteRecursively(file: File) {
    if (file != null) {
      if (file.isDirectory && !isSymlink(file)) {
        for (child <- listFilesSafely(file))
          deleteRecursively(child)
      }
      if (!file.delete()) {
        if (file.exists())
          throw new IOException("Failed to delete: " + file.getAbsolutePath)
      }
    }
  }

  def isSymlink(file: File): Boolean = {
    if (file == null) throw new NullPointerException("File must not be null")
    if (SystemUtils.IS_OS_WINDOWS) return false
    val fcd = if (file.getParent == null) file else new File(file.getParentFile.getCanonicalFile, file.getName)
    if (fcd.getCanonicalFile.equals(fcd.getAbsoluteFile)) false else true
  }

  def listFilesSafely(file: File): Seq[File] = {
    val files = file.listFiles()
    if (files == null) throw new IOException("Failed to list files for dir: " + file)
    files
  }
}

