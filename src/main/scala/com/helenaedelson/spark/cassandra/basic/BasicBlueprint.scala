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

import com.datastax.spark.connector.cql.CassandraConnector
import com.helenaedelson.spark.cassandra.{ Blueprint, Settings }

object BasicBlueprint extends Blueprint {

  CassandraConnector(conf).withSessionDo { session =>
    session.execute("CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 }")
    session.execute("CREATE TABLE IF NOT EXISTS test.key_value (key INT PRIMARY KEY, value VARCHAR)")
    session.execute("TRUNCATE test.key_value")
    session.execute("INSERT INTO test.key_value(key, value) VALUES (1, 'first row')")
    session.execute("INSERT INTO test.key_value(key, value) VALUES (2, 'second row')")
    session.execute("INSERT INTO test.key_value(key, value) VALUES (3, 'third row')")
  }

  /** Import the Spark Cassandra implicits. */
  import com.datastax.spark.connector._

  /** Read table test.kv and print its contents.
    * Retrieving data from Cassandra returns a (Cassandra) RDD. */
  val rdd = sc.cassandraTable("test", "key_value").select("key", "value")
  rdd.collect.foreach(row => log.info(s"$row"))

  /** Write two rows to the test.kv table. */
  val col = sc.parallelize(Seq((4, "fourth row"), (5, "fifth row")))
  col.saveToCassandra("test", "key_value", SomeColumns("key", "value"))
  col.collect().foreach(row => log.info(s"$row"))

  // Assert the two new rows were stored in test.kv table:
  awaitCond(col.collect().length == 2)

  sc.stop()
}
