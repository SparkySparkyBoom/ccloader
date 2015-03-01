package ccloader

import org.specs2.mutable._

class HeaderParserSpec extends Specification {
  override def is = s2"""
    keyVal $e1
  """

  def e1 = {
    class p
    val parser = new p with HeaderParser
    parser.parseKeyVal("Key: value") === Some("Key", "value")
  }
}
