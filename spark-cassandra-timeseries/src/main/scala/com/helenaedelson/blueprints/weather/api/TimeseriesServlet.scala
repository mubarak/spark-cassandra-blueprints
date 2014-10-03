package com.helenaedelson.blueprints.weather.api

import javax.servlet.http.HttpServletRequest

import org.scalatra._
import org.joda.time.{DateTimeZone, DateTime}
import org.json4s.{Formats, DefaultFormats}
import com.helenaedelson.blueprints.api.BlueprintsServlet

class TimeseriesServlet extends BlueprintsServlet {
  import WeatherApi._

  override def jsonFormats: Formats = apiFormats

  def dayOfYearParam(params: Params): Int = params.get("dayofyear").map(_.toInt) getOrElse currentDayOfYear

  def zipcodeParam(params: Params): Option[Int] = params.get("zipcode").map(_.toInt)

  protected def stationIdOrHalt(request: HttpServletRequest, errorBody: => String => Any = identity): WeatherStationId = {
    WeatherStationId(request) getOrElse halt(status = 400, body = errorBody("No Station ID")) valueOr (fail => halt(status = 400, body = errorBody(fail)))
  }

  private def currentDayOfYear: Int = new DateTime(DateTimeZone.UTC).dayOfYear().get()
}