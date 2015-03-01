package ccloader

import akka.actor.Actor
import ccloader.Util.notNewline
import org.joda.time.format.DateTimeFormat

class Loader extends Actor with HeaderParser {
  val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").withOffsetParsed()

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
            case _ =>
          }
        } else {
          val html = new String(b, "UTF-8")
          val parsedDate = dateFormatter.parseDateTime(date)
          Pages.insertPage(Page(url, html, parsedDate))
        }
      }
  }
}
