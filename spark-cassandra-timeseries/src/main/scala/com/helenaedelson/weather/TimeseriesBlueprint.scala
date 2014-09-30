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
package com.helenaedelson.weather

import akka.actor.{ActorSystem, Props}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import com.datastax.spark.connector.embedded.EmbeddedKafka
import com.helenaedelson.blueprints.StreamingBlueprint

/** For running WeatherCenter from command line or IDE. */
object WeatherCenter extends TimeseriesBlueprint

/** Used to run [[WeatherCenter]] and [[com.helenaedelson.weather.api.WeatherServletContextListener]] */
trait TimeseriesBlueprint extends StreamingBlueprint {

  override val settings = new WeatherSettings()

  /** Creates the Spark Streaming context. */
  val ssc =  new StreamingContext(sc, Seconds(2))

  /** Creates the ActorSystem. */
  val system = ActorSystem(settings.AppName) //settings.rootConfig

  val log = akka.event.Logging(system, system.name)

  /** Starts the Kafka broker. */
  val kafka = new EmbeddedKafka

  system.registerOnTermination(kafka.shutdown())

  val guardian = system.actorOf(Props(new NodeGuardian(ssc, kafka, settings)), "node-guardian")

}
