package ccloader

import java.nio.ByteBuffer
import java.nio.charset.{CodingErrorAction, Charset}
import akka.actor.{Props, ActorSystem, ActorRef, Actor}
import akka.util.ByteString
import org.joda.time.format.DateTimeFormat
import java.io.{File, FileInputStream, DataInputStream, BufferedInputStream}
import scala.io.Source
import scala.util.parsing.combinator.RegexParsers
import scala.collection.mutable
import Main.{readWhile, notNewline}

case class Resource(url: String, date: String, content: Array[Byte])

object Main {
  def main(args: Array[String]): Unit = {
    val warcFile = args(0)
    implicit val system = ActorSystem("loader-system")
    val loader = system.actorOf(Props[Loader])
    val ccp = new CCParser(loader, warcFile)
    new Thread(ccp).run()
  }

  def readWhile(implicit dis: DataInputStream, predicate: Byte => Boolean): Array[Byte] = {
    val bytes = new mutable.ArrayBuffer[Byte]
    var done = false
    while (!done) {
      val b = dis.readByte()
      if (predicate(b)) {
        bytes += b
      } else {
        done = true
      }
    }
    bytes.toArray
  }

  def notNewline(b: Byte) = {
    b != '\n'.toByte
  }
}

class Loader extends Actor with HeaderParser {
  private val dateFormatter = DateTimeFormat.forPattern("yyyy-mm-dd'T'ZHH:mm:ss'Z'").withOffsetParsed()

  override def receive = {
    case Resource(url, date, b) =>
      var done = false
      var contentType = ""
      val bytesIterator = b.toIterator
      while (bytesIterator.hasNext) {
        val line = new String(bytesIterator.takeWhile(notNewline).toArray, "UTF-8")
        if (line != "") {
          parseKeyVal(line) match {
            case Some(("Content-Type", t)) =>
              contentType = t
              if (!contentType.contains("text/html"))
                done = true
          }
        } else {
          val html = new String(b, "UTF-8")
          val parsedDate = dateFormatter.parseDateTime(date)
          Pages.insertPage(Page(url, html, parsedDate))
        }
      }
  }
}

class CCParser(loader: ActorRef, filename: String) extends Runnable with HeaderParser {
  def run(): Unit = {
    val fis = new FileInputStream(new File(filename))
    implicit val dis = new DataInputStream(new BufferedInputStream(fis))

    var parsingResponse = false
    var url: String = ""
    var date: String = ""
    var contentLength: Int = 0

    while (fis.available() > 0) {
      val line = new String(readWhile(dis, notNewline), "US-ASCII")
      if (!parsingResponse) {
        parseKeyVal(line) match {
          case Some(("WARC-Type", "response")) =>
            parsingResponse = true
        }
      } else if (line != "") {
        parseKeyVal(line) match {
          case Some(("WARC-Date", _date)) => date = _date
          case Some(("Content-Length", len)) => contentLength = len.toInt
          case Some(("WARC-Target-URI", _url)) => url = _url
        }
      } else {
        val content = new Array[Byte](contentLength)
        dis.read(content)
        loader ! Resource(url, date, content)
      }
    }
  }
}

trait HeaderParser extends RegexParsers {
  def keyword = regex( """(([a-zA-Z])|(\-))+""".r)
  def anything = regex(".+".r)
  def integer = regex( """\d+""".r)
  def separator = literal(" :")

  def keyVal: Parser[(String, String)] = keyword ~ separator ~ anything ^^ {
    case key ~ _ ~ value =>
      (key, value)
  }

  def parseKeyVal(line: String): Option[(String, String)] = {
    parse(keyVal, line).map(Some.apply).getOrElse(None)
  }
}
