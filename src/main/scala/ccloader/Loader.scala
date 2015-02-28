package ccloader

import Util.notNewline
import org.joda.time.format.DateTimeFormat
import akka.actor.{Props, ActorSystem, ActorRef, Actor}

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
