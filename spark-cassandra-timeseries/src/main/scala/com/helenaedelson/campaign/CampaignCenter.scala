package com.helenaedelson.campaign

import scala.concurrent.duration._
import scala.util.Try
import akka.actor.{Props, ActorSystem}
import akka.event.Logging
import org.apache.spark.streaming.{Seconds, StreamingContext}
import twitter4j.auth.BasicAuthorization
import com.datastax.spark.connector.embedded.{Assertions, EmbeddedKafka}
import com.helenaedelson.blueprints.{Settings, StreamingBlueprint}

/** For running CampaignCenter from command line or IDE.
  * This is a WIP - just started to build out. Check back mid-October. */
object CampaignCenter extends StreamingBlueprint with Assertions {

  override val settings = new CampaignSettings()
  import settings._

  /** Starts the Kafka broker and Zookeeper. */
  lazy val kafka = new EmbeddedKafka

  /** Creates the Spark Streaming context. */
  val ssc =  new StreamingContext(sc, Seconds(2))
  ssc.checkpoint("./tmp")

  /** Creates the ActorSystem. */
  val system = ActorSystem(settings.AppName)

  val log = Logging(system, system.name)

  system.registerOnTermination(kafka.shutdown())

  val auth = new BasicAuthorization(TwitterKey, TwitterSecret)

  system.actorOf(Props(new TweetsStreamActor(ssc, auth)), "tweet-stream")

  log.info("System initialized.")

  ssc.awaitTermination()
  system.shutdown()
  awaitCond(system.isTerminated, 3.seconds)

}

class CampaignSettings extends Settings() {

  protected lazy val campaign = blueprints.getConfig("timeseries.campaign")

  lazy val Keyspace = campaign.getString("cassandra.keyspace")

  lazy val KafkaTopic = campaign.getString("kafka.topic")

  lazy val TwitterKey = withFallback[String](Try(campaign.getString("twitter.key")),
    "twitter.key").getOrElse { throw new IllegalArgumentException("""
        A Twitter api key is required to be set in your environment: export TWITTER_KEY="yourkey"""")
  }

  lazy val TwitterSecret = withFallback[String](Try(campaign.getString("twitter.secret")),
    "twitter.secret").getOrElse { throw new IllegalArgumentException("""
        A Twitter api secret is required to be set in your environment: export TWITTER_SECRET="yourkey"""")
  }

}