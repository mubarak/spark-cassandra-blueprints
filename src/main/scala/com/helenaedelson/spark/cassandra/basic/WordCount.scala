package com.helenaedelson.spark.cassandra.basic

import scala.concurrent.duration._
import org.apache.spark.SparkContext._
import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector._
import com.helenaedelson.spark.cassandra.Blueprint

/** Two simple spark word count apps, sorted alphabetically.
  *
  * [[SparkWordCount]] writes to console.
  *
  * [[SparkCassandraWordCount]] writes to cassandra.
  *
  * Same amount of code, `setup` added for keyspace/table setup, `verify` to prove stored.
  * In the wild, neither are relevant or needed. */
object SparkWordCount extends AbstractWordCount {

  sc.textFile("./src/main/resources/data/words")
    .flatMap(_.split("\\s+"))
    .map(word => (clean(word), 1))
    .reduceByKey(_ + _)
    .sortByKey()
    .collect.foreach(row => logInfo(s"$row"))
}

object SparkCassandraWordCount extends AbstractWordCount {
  setup()

  sc.textFile("./src/main/resources/data/words")
    .flatMap(_.split("\\s+"))
    .map(word => (clean(word), 1))
    .reduceByKey(_ + _)
    .sortByKey()
    .saveToCassandra(keyspaceName, tableName)

  verify()
}

trait AbstractWordCount extends Blueprint {
  import BlueprintEvents._

  val keyspaceName = "basic_blueprints"
  val tableName = "wordcount"

  /** Pre-setup local cassandra with the keyspace and table. */
  def setup(): Unit =
    CassandraConnector(conf).withSessionDo { session =>
      session.execute(s"CREATE KEYSPACE IF NOT EXISTS $keyspaceName WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 }")
      session.execute(s"CREATE TABLE IF NOT EXISTS $keyspaceName.$tableName (word TEXT PRIMARY KEY, count COUNTER)")
      session.execute(s"TRUNCATE $keyspaceName.$tableName")
    }

  def clean(word: String): String =
    word.toLowerCase.replace(".", "").replace(",", "")

  /** Read table and output its contents. */
  def verify(): Unit = {
    val rdd = sc.cassandraTable[WordCount](keyspaceName, tableName).select("word", "count")
    awaitCond(rdd.collect.length > 20, 60.seconds)
    rdd.collect.foreach(c => logInfo(s"$c"))

  }
}

object BlueprintEvents {
  sealed trait BlueprintEvent extends Serializable
  case class Publish(to: String, data: Map[String,Int])
  case class WordCount(word: String, count: Int)

}
