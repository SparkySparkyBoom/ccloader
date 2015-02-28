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
