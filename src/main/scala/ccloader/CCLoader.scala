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

    val fis = new FileInputStream(new File(warcFile))
    val loader = system.actorOf(Props[Loader])
    val ccp = new CCParser(loader, fis)
    new Thread(ccp).run()
  }
}
