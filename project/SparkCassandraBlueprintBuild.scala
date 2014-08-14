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

    val akkaActor         = "com.typesafe.akka"       %% "akka-actor"           % Akka           % "provided"                 // ApacheV2
    val akkaSlf4j         = "com.typesafe.akka"       %% "akka-slf4j"           % Akka           % "provided"                 // ApacheV2
    val cassandraThrift   = "org.apache.cassandra"    % "cassandra-thrift"      % Cassandra
    val cassandraClient   = "org.apache.cassandra"    % "cassandra-clientutil"  % Cassandra
    val cassandraDriver   = "com.datastax.cassandra"  % "cassandra-driver-core" % CassandraDriver              withSources()  // ApacheV2
    val config            = "com.typesafe"            % "config"                % Config         % "provided"                 // ApacheV2
    val logback           = "ch.qos.logback"          % "logback-classic"       % Logback                                     // EPL 1.0 / LGPL 2.1
    val slf4jApi          = "org.slf4j"               % "slf4j-api"             % Slf4j          % "provided"                 // MIT
    val sparkCore         = "org.apache.spark"        %% "spark-core"           % Spark          % "provided"   exclude("com.google.guava", "guava") // ApacheV2
    val sparkStreaming    = "org.apache.spark"        %% "spark-streaming"      % Spark          % "provided"   exclude("com.google.guava", "guava") // ApacheV2

  }

  import Compile._

  val logging = Seq(slf4jApi)

  val akka = Seq(akkaActor, akkaSlf4j)

  val cassandra = Seq(cassandraThrift, cassandraClient, cassandraDriver)

  val blueprints = logging ++ akka ++ cassandra ++ Seq(config, sparkCore, sparkStreaming)

}
