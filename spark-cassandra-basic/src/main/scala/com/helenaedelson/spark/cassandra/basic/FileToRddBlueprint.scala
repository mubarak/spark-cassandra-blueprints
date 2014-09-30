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

import org.apache.spark.SparkContext._
import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector._
import com.helenaedelson.blueprints.BlueprintApp

object FileToRddBlueprint extends BlueprintApp {
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
