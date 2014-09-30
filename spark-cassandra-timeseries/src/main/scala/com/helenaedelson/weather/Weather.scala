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

import com.helenaedelson.blueprints.api.ApiData._

object Weather {
  /** Base marker trait. */
  sealed trait WeatherDataMarker extends Serializable

  trait DataRequest extends WeatherDataMarker

  /** Keeping as flat as possible for now, for simplicity. May modify later when time. */
  trait WeatherModel extends WeatherDataMarker

  /**
   * @param id Composite of Air Force Datsav3 station number and NCDC WBAN number
   * @param name Name of reporting station
   * @param countryCode 2 letter ISO Country ID // TODO restrict
   * @param callSign International station call sign
   * @param lat Latitude in decimal degrees
   * @param long Longitude in decimal degrees
   * @param elevation Elevation in meters
   */
  case class WeatherStation(
    id: String,
    name: String,
    countryCode: String,
    callSign: String,
    lat: Float,
    long: Float,
    elevation: Float) extends WeatherModel

  /**
   * @param weatherStation Composite of Air Force Datsav3 station number and NCDC WBAN number
   * @param year Year collected
   * @param month Month collected
   * @param day Day collected
   * @param hour Hour collected
   * @param temperature Air temperature (degrees Celsius)
   * @param dewpoint Dew point temperature (degrees Celsius)
   * @param pressure Sea level pressure (hectopascals)
   * @param windDirection Wind direction in degrees. 0-359
   * @param windSpeed Wind speed (meters per second)
   * @param skyCondition Total cloud cover (coded, see format documentation)
   * @param skyConditionText Non-coded sky conditions
   * @param oneHourPrecip One-hour accumulated liquid precipitation (millimeters)
   * @param sizeHourPrecip Six-hour accumulated liquid precipitation (millimeters)
   */
  case class RawWeatherData(
    weatherStation: String,
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    temperature: Int,
    dewpoint: Float,
    pressure: Float,
    windDirection: Int,
    windSpeed: Float,
    skyCondition: Int,
    skyConditionText: String,
    oneHourPrecip: Float,
    sizeHourPrecip: Float) extends WeatherModel

  case class HiLowForecast() extends WeatherModel
  /**
   * Quick access lookup table for sky_condition. Useful for potential analytics.
   * See http://en.wikipedia.org/wiki/Okta
   */
  case class SkyConditionLookup(code: Int, condition: String) extends WeatherModel

  sealed trait Forecast extends DataRequest
  sealed trait Aggregate extends Forecast

  case object GetWeatherStation extends DataRequest
  case object GetRawWeatherData extends DataRequest
  case object GetSkyConditionLookup extends Forecast
  /* Will be building out the date range handling this/next week. For now, keeping it simple. */
  case class GetHiLow(uid: UserId, days: Int = 3) extends Aggregate
}
