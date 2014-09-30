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
package com.helenaedelson.weather.api

/* not using yet
import scalaz.contrib.std.scalaFuture._
import scalaz.std.list._
import scalaz.syntax.traverse._
import scalaz.syntax.monad._
import scalaz._
import akka.pattern.ask
import org.json4s._
import org.json4s.native.JsonParser
*/

import akka.actor.{ActorRef, ActorSystem}
import com.helenaedelson.blueprints.api._

class WeatherCenterServlet(api: WeatherDataActorApi) extends BlueprintsServlet {
  import com.helenaedelson.blueprints.api._

  get("/v1/high-low") {
    val userId = userIdOrHalt(request)
    api.hilow(userId).run.valueOrThrow
  }
}

class WeatherDataActorApi(system: ActorSystem, guardian: ActorRef) {

  import scala.concurrent.duration._
  import akka.pattern.ask
  import akka.util.Timeout
  import com.helenaedelson.blueprints.api._
  import com.helenaedelson.weather.Weather
  import ApiData._
  import Weather._
  import system.dispatcher

  implicit val timeout = Timeout(5.seconds)

  /** Returns a summary of the weather for the next 3 days.
    * This includes high and low temperatures, a string text forecast and the conditions.
    * @param userId the user id
    */
  def hilow(userId: UserId): FutureT[HiLowForecast] =
    (guardian ? userId).mapTo[HiLowForecast].eitherT

}