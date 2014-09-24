package com.helenaedelson.spark.cassandra.basic

import scala.concurrent.duration._
import org.apache.spark.SparkContext._
import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.embedded._
import com.helenaedelson.spark.cassandra.Blueprint
import com.helenaedelson.spark.cassandra.basic.StreamingEvent.WordCount

/** A simple spark word count, sorted alphabetically, written to console. */
trait AbstractWordCount extends Blueprint {

  val rootpath = settings.ResourceDataDirectory

  def clean(word: String): String =
    word.toLowerCase.replace(".", "").replace(",", "")

}

/** A simple spark word count, sorted alphabetically, written to console. */
object SparkWordCount extends AbstractWordCount {

  sc.textFile(s"$rootpath/words")
    .flatMap(_.split("\\s+"))
    .map(word => (clean(word), 1))
    .reduceByKey(_ + _)
    .sortByKey()
    .collect.foreach(row => log.info(s"$row"))
}

/** A simple spark cassandra word count, sorted alphabetically, written to cassandra. */
object SparkCassandraWordCount extends AbstractWordCount {

  /** Pre-setup local cassandra with the keyspace and table. */
  CassandraConnector(conf).withSessionDo { session =>
    session.execute("CREATE KEYSPACE IF NOT EXISTS blueprintsv1 WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 }")
    session.execute("DROP TABLE IF EXISTS blueprintsv1.wordcount")
    session.execute("CREATE TABLE IF NOT EXISTS blueprintsv1.wordcount (word TEXT PRIMARY KEY, count COUNTER)")
    session.execute("TRUNCATE blueprintsv1.wordcount")
  }

  import com.datastax.spark.connector._

  sc.textFile(s"$rootpath/words")
    .flatMap(_.split("\\s+"))
    .map(word => (clean(word), 1))
    .reduceByKey(_ + _)
    .sortByKey()
    //.collect().foreach(row => log.info(s"$row"))
    .saveToCassandra("blueprintsv1", "wordcount", SomeColumns("word", "count"))

  /** Read table and output its contents. */
  val rdd = sc.cassandraTable[WordCount]("blueprintsv1", "wordcount").select("word", "count")
  awaitCond(rdd.collect.length > 20, 60.seconds)
  rdd.collect.foreach(c => log.info(s"$c"))

}
