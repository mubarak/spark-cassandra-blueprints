/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import sbt._
import sbt.Keys._

object SparkCassandraBlueprintBuild extends Build {
  import Settings._

  lazy val blueprints = Project(
    id = "spark-cassandra-blueprints",
    base = file("."),
    settings = defaultSettings ++ Seq(libraryDependencies ++= Dependencies.blueprints)
  )

}

object Dependencies {

  object Compile {
    import Versions._

    val akkaActor         = "com.typesafe.akka"   %% "akka-actor"                         % Akka            // ApacheV2
    val akkaSlf4j         = "com.typesafe.akka"   %% "akka-slf4j"                         % Akka            // ApacheV2
    val kafka             = "org.apache.kafka"    %% "kafka"                              % Kafka           // ApacheV2
    val slf4jApi          = "org.slf4j"           % "slf4j-api"                           % Slf4j           // MIT
    val sparkCassandra    = "com.datastax.spark"  %% "spark-cassandra-connector"          % SparkCassandra  // ApacheV2
    val sparkCassandraEmb = "com.datastax.spark"  %% "spark-cassandra-connector-embedded" % SparkCassandra  // ApacheV2

  }

  import Compile._

  val akka = Seq(akkaActor)

  val connector = Seq(sparkCassandra, sparkCassandraEmb)

  val logging = Seq(akkaSlf4j, slf4jApi)

  val blueprints = logging ++ akka ++ connector ++ Seq(kafka)

}
