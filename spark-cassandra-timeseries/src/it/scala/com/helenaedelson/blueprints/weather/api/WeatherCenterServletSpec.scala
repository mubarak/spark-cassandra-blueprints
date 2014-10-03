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

package com.helenaedelson.blueprints.weather.api

import org.json4s.Extraction._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.JsonParser
import org.scalatest.WordSpecLike
import org.scalatra.test.scalatest._
import com.helenaedelson.blueprints.weather.TimeseriesBlueprint

class WeatherCenterServletSpec extends ScalatraSuite with WordSpecLike
  with TimeseriesBlueprint with TimeseriesFixture {
  import com.helenaedelson.blueprints.weather.Weather._
  import com.helenaedelson.blueprints.weather._

  val api = new WeatherDataActorApi(system, guardian)
  
  addServlet(new WeatherCenterServlet(api), "/*")

  "WeatherCenterServlet" should {
    "GET v1/high-low with a valid uid" in {
      get("/v1/high-low", headers = testHeaders) {
        response.status should be(200)
        val alerts = JsonParser.parse(response.body).extract[HiLowForecast]
        println(pretty(render(decompose(alerts))))
        // TODO validate
      }
    }
    "response with 400 if no uid is passed in the header" in {
      get("/v1/high-low") {
        response.status should be(400)
      }
    }
  }
}

// ?perPage=20&size=400
trait TimeseriesFixture {

  val uid = "9784dkfu387669eb2936d1b1fdd858a"

  val testHeaders = Map("X-CS-CUSTID" -> uid, "content-type" -> "application/json")
}