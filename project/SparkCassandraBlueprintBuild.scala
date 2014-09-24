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
  ) configs (IntegrationTest)

}

object Dependencies {

  object Compile {
    import Versions._

    val akkaActor         = "com.typesafe.akka"       %% "akka-actor"                % Akka           % "provided"                 // ApacheV2
    val akkaSlf4j         = "com.typesafe.akka"       %% "akka-slf4j"                % Akka           % "provided"                 // ApacheV2
    val cassandraThrift   = "org.apache.cassandra"    % "cassandra-thrift"           % Cassandra
    val cassandraClient   = "org.apache.cassandra"    % "cassandra-clientutil"       % Cassandra
    val cassandraDriver   = "com.datastax.cassandra"  % "cassandra-driver-core"      % CassandraDriver              withSources()  // ApacheV2
    val config            = "com.typesafe"            % "config"                     % Config         % "provided"                 // ApacheV2
    val logback           = "ch.qos.logback"          % "logback-classic"            % Logback                                     // EPL 1.0 / LGPL 2.1
    val slf4jApi          = "org.slf4j"               % "slf4j-api"                  % Slf4j          % "provided"                 // MIT
    val sparkCassandra    = "com.datastax.spark"      %% "spark-cassandra-connector" % SparkCassandra
    val sparkCassandraEmb = "com.datastax.spark"      %% "spark-cassandra-connector-embedded" % SparkCassandra
    val sparkCore         = "org.apache.spark"        %% "spark-core"                % Spark      exclude("com.google.guava", "guava") // ApacheV2
    val sparkStreaming    = "org.apache.spark"        %% "spark-streaming"           % Spark      exclude("com.google.guava", "guava") // ApacheV2
    val sparkStreamingKafka   = "org.apache.spark"     %% "spark-streaming-kafka"    % Spark      exclude("com.google.guava", "guava") // ApacheV2
    val sparkStreamingTwitter = "org.apache.spark"     %% "spark-streaming-twitter"  % Spark      exclude("com.google.guava", "guava") // ApacheV2
    val sparkStreamingZmq     = "org.apache.spark"     %% "spark-streaming-zeromq"   % Spark      exclude("com.google.guava", "guava") // ApacheV2
    val cassandraServer       = "org.apache.cassandra" % "cassandra-all"             % Cassandra  exclude("com.google.guava", "guava") // ApacheV2
    val jopt                  =  "net.sf.jopt-simple"  % "jopt-simple"               % JOpt       // For kafka command line
    val sparkRepl             = "org.apache.spark"     %% "spark-repl"               % Spark exclude("com.google.guava", "guava") exclude("org.apache.spark", "spark-core_2.10") exclude("org.apache.spark", "spark-bagel_2.10") exclude("org.apache.spark", "spark-mllib_2.10") exclude("org.scala-lang", "scala-compiler")          // ApacheV2

    object Test {
      val akkaTestKit     = "com.typesafe.akka"       %% "akka-testkit"         % Akka           % "test,it"                 // ApacheV2
      val commonsIO       = "commons-io"              % "commons-io"            % CommonsIO      % "test,it"                 // ApacheV2
      val scalatest       = "org.scalatest"           %% "scalatest"            % ScalaTest      % "test,it"                 // ApacheV2
    }
  }

  import Compile._

  val logging = Seq(slf4jApi)

  val test = Seq(Test.akkaTestKit, Test.commonsIO, Test.scalatest)

  val akka = Seq(akkaActor, akkaSlf4j)

  val connector = Seq(sparkCassandra, sparkCassandraEmb)

  val cassandra = Seq(cassandraThrift, cassandraClient, cassandraDriver)

  val spark = Seq(sparkCore, sparkStreaming)

  val sparkExtStreaming = Seq(sparkStreamingKafka, sparkStreamingTwitter, sparkStreamingZmq)

  val blueprints = test ++ logging ++ akka ++ cassandra ++ connector ++ spark ++ sparkExtStreaming ++ Seq(config)

}
