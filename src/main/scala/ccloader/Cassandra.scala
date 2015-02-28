package ccloader

import java.util.UUID
import com.websudos.phantom.zookeeper.SimpleCassandraConnector
import com.websudos.phantom.Implicits._
import org.joda.time.DateTime

import scala.concurrent.Future

case class Page(url: String,
                html: String,
                timestamp: DateTime,
                id: UUID = UUID.randomUUID)

sealed class Pages extends CassandraTable[Pages, Page] {

  object id extends UUIDColumn(this) with PartitionKey[UUID] {
    override val name = "id"
  }

  object url extends StringColumn(this)

  object html extends StringColumn(this)

  object timestamp extends DateTimeColumn(this)

  override def fromRow(row: Row) = Page(
    url(row),
    html(row),
    timestamp(row),
    id = id(row)
  )
}

object Pages extends Pages with Connector {
  def insertPage(page: Page): Future[Any] = {
    insert
      .value(_.id, page.id)
      .value(_.url, page.url)
      .value(_.html, page.html)
      .value(_.timestamp, page.timestamp)
      .future()
  }
}

trait Connector extends SimpleCassandraConnector {
  override val keySpace = "hippo"
}
