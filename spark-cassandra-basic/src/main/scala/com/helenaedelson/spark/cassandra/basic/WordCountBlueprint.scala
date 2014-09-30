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
import org.apache.spark.SparkContext._
import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector._
import com.helenaedelson.blueprints._

/** Two simple spark word count apps, sorted alphabetically.
  * [[SparkWordCount]] writes to console.
  * [[SparkCassandraWordCount]] writes to cassandra.
  *
  * Same amount of code, `setup` added for keyspace/table setup, `verify` to prove stored.
  * In the wild, neither are relevant or needed.
 */
object SparkWordCount extends WordCountBlueprint {

  sc.textFile("./src/main/resources/data/words")
    .flatMap(_.split("\\s+"))
    .map(word => (clean(word), 1))
    .reduceByKey(_ + _)
    .collect foreach println 
}

object SparkCassandraWordCount extends WordCountBlueprint {
  setup()

  sc.textFile("./src/main/resources/data/words")
    .flatMap(_.split("\\s+"))
    .map(word => (clean(word), 1))
    .reduceByKey(_ + _)
    .sortByKey()
    .saveToCassandra(keyspaceName, tableName)

  verify()
}

trait WordCountBlueprint extends BlueprintApp {
  import BlueprintEvents._

  protected val keyspaceName = "basic_blueprints"
  protected val tableName = "wordcount"

  /** Pre-setup local cassandra with the keyspace and table. */
  def setup(): Unit =
    CassandraConnector(conf).withSessionDo { session =>
      session.execute(s"CREATE KEYSPACE IF NOT EXISTS $keyspaceName WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 }")
      session.execute(s"CREATE TABLE IF NOT EXISTS $keyspaceName.$tableName (word TEXT PRIMARY KEY, count COUNTER)")
      session.execute(s"TRUNCATE $keyspaceName.$tableName")
    }

  def clean(word: String): String =
    word.toLowerCase.replace(".", "").replace(",", "")

  def timestamp: Long = now.toNanos

  /** Read table and output its contents. */
  def verify(): Unit = {
    val rdd = sc.cassandraTable[WordCount](keyspaceName, tableName).select("word", "count")
    awaitCond(rdd.collect.length > 20, 60.seconds)
    rdd.collect.foreach(c => logInfo(s"$c"))

  }
}
