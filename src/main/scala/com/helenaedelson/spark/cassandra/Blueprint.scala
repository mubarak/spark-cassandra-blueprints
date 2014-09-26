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

package com.helenaedelson.spark.cassandra

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.streaming.StreamingContext
import com.datastax.spark.connector.embedded.Assertions
import com.datastax.spark.connector.util.Logging

/**
 * Currently requires a node to be running.
 *
 * TODO fix issue with EmbeddedCassandra and add
 * extends.. then useCassandraConfig("cassandra-default.yaml.template")
 * and def clearCache(): Unit = CassandraConnector.evictCache()
 */
trait Blueprint extends App with Assertions with Logging {

  val settings: Settings = new Settings
  import settings._

  /** Configures Spark. */
  lazy val conf = new SparkConf()
    .set("spark.cassandra.connection.host", CassandraHosts)
    .set("spark.cleaner.ttl", SparkCleanerTtl.toString)
    .setMaster(SparkMaster)
    .setAppName(getClass.getSimpleName)

  lazy val sc = new SparkContext(conf)

}

trait StreamingBlueprint extends Blueprint {

  def ssc: StreamingContext

}
