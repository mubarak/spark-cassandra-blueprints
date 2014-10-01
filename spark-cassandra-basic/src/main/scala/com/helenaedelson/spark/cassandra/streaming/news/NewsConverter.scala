package com.helenaedelson.spark.cassandra.streaming.news

import java.io.File
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.JavaConversions
import scala.collection.immutable.Stream.cons
import scala.io.Source
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import com.datastax.spark.connector.cql.CassandraConnector

// uses http://www.memetracker.org/data.html
object NewsConverter {

  val formatter = DateTimeFormat.forPattern("YYYY-MM-DD HH:mm:ss")

  case class Paragraph(p: String, t: DateTime, q: Seq[String], l: Seq[String])


  def parseParagraphs(it: Iterator[String]): Stream[(Iterator[String])] = {
    val (paragraph_1, rest_1) = it.span(_.startsWith("P"))
    val (paragraph_2, rest_2) = rest_1.span(!_.startsWith("P"))

    if (paragraph_1.nonEmpty) {
      val paragraph = paragraph_1 ++ paragraph_2
      cons(paragraph, parseParagraphs(rest_2))
    } else {
      Stream.empty
    }
  }


  def convertToParagraph(raw: (Iterator[String])) = {
    val (p, _tql) = raw.span(_.startsWith("P"))
    val (t, _ql) = _tql.span(_.startsWith("T"))
    val (q, _l) = _ql.span(_.startsWith("Q"))
    val (l, _) = _l.span(_.startsWith("L"))

    for (primary <- p.toList.headOption; timestamp <- t.toList.headOption) yield
      Paragraph(
        primary.substring(1).trim,
        DateTime.parse(timestamp.substring(1).trim, formatter),
        q.toList.filter(_.nonEmpty).map(_.substring(1).trim),
        l.toList.filter(_.nonEmpty).map(_.substring(1).trim))
  }


  def main(args: Array[String]) {
    val in = Source.fromFile(new File("/Users/jlewandowski/Downloads/quotes-2008-10-aa"), 1024*1024*16)
    val lines = in.getLines()
    val paragraphs = parseParagraphs(lines)

    val connector = CassandraConnector(host = InetAddress.getByName("localhost"))

    connector.withSessionDo { session =>
      session.execute("CREATE KEYSPACE IF NOT EXISTS meetup WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}")
      session.execute("CREATE TABLE IF NOT EXISTS meetup.news (" +
        "domain TEXT, ts TIMESTAMP, source TEXT, content TEXT, links SET<TEXT>, PRIMARY KEY (domain, ts, source))")

      val stmt = session.prepare("INSERT INTO meetup.news (domain, ts, source, content, links) VALUES (?, ?, ?, ?, ?)")
      val domainRegex = """^https?://([^/]+).*""".r
      val cnt = new AtomicInteger(0)
      paragraphs.grouped(200).foreach { group =>
        group.toParArray.flatMap(convertToParagraph).filter(_.q.nonEmpty).foreach { paragraph =>
          paragraph.p match {
            case domainRegex(domain) => {
              val bndStmt = stmt.bind(domain, paragraph.t.toDate, paragraph.p, paragraph.q.mkString(" "), JavaConversions.setAsJavaSet(paragraph.l.toSet))
              val future = session.executeAsync(bndStmt)

              println(s"inserted ${cnt.incrementAndGet()} / $domain")
            }
          }

        }

      }
    }

    in.close()
  }

}
