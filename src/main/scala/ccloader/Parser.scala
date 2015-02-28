package ccloader

import Util.notNewline
import akka.actor.ActorRef
import scala.util.parsing.combinator.RegexParsers
import java.io.{InputStream, DataInputStream, BufferedInputStream}
import scala.collection.mutable

/**
 * Functions for parsing WARC and HTTP headers.
 */
trait HeaderParser extends RegexParsers {
  def keyword = regex( """(([a-zA-Z])|(\-))+""".r)
  def anything = regex(".+".r)
  def integer = regex( """\d+""".r)
  def separator = literal(": ")

  def keyVal: Parser[(String, String)] = keyword ~ separator ~ anything ^^ {
    case key ~ _ ~ value =>
      (key, value)
  }

  def parseKeyVal(line: String): Option[(String, String)] = {
    parse(keyVal, line).map(Some.apply).getOrElse(None)
  }
}

class CCParser(loader: ActorRef, is: InputStream) extends Runnable with HeaderParser {
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

  def run(): Unit = {
    implicit val dis = new DataInputStream(new BufferedInputStream(is))

    var parsingResponse = false
    var url: String = ""
    var date: String = ""
    var contentLength: Int = 0

    while (dis.available() > 0) {
      val line = new String(readWhile(dis, notNewline), "UTF-8")
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
