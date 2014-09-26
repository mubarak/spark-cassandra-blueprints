package com.helenaedelson.spark.cassandra.basic

import org.apache.spark.SparkContext._
import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector._
import com.helenaedelson.spark.cassandra.Blueprint

object FileToRddBlueprint extends Blueprint {
  protected val keyspaceName = "basic_blueprints"
  protected val tableName = "wordcount"

  CassandraConnector(conf).withSessionDo { session =>
    session.execute(s"CREATE KEYSPACE IF NOT EXISTS basic_blueprints WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 }")
    session.execute(s"CREATE TABLE IF NOT EXISTS basic_blueprints.wordcount (word TEXT PRIMARY KEY, count COUNTER)")
    session.execute(s"TRUNCATE basic_blueprints.wordcount")
  }

  sc.textFile("./src/main/resources/data/words")
    .flatMap(_.split("\\s+"))
    .map(word => (word, 1))
    .reduceByKey(_ + _)
    .saveToCassandra("basic_blueprints", "wordcount")

}
