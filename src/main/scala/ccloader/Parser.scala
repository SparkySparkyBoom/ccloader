package ccloader

import Main.{readWhile, notNewline}

/**
 * Functions for parsing WARC and HTTP headers.
 */
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
