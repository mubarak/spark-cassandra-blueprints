package com.helenaedelson.weather.api

import javax.servlet.{ServletContextEvent, ServletContextListener}

import scala.util.control.NonFatal
import org.scalatra.servlet.ServletApiImplicits
import com.helenaedelson.weather.TimeseriesBlueprint

class WeatherServletContextListener extends ServletContextListener with ServletApiImplicits with TimeseriesBlueprint {

  override def contextInitialized(event: ServletContextEvent): Unit = try {
    log.info(s"Creating context for ${system.name}")
    val context = event.getServletContext
    context("settings") = settings
    context("system") = system
    context("streaming-context") = ssc
    context("kafka") = kafka
  } catch { case NonFatal(e) =>
    log.error(e, "Error while initializing servlet context.")
    throw e
  }

  override def contextDestroyed(event: ServletContextEvent): Unit = {
    log.info("Shutting down on {}", system)
    kafka.shutdown()
    system.shutdown()
  }
}
