package ai.snips.bsonmacros

import javax.inject._

import org.mongodb.scala.{MongoClient, MongoDatabase}
import play.api._
import play.api.inject.ApplicationLifecycle

import scala.concurrent._

@Singleton
class DatabaseContext @Inject()(val conf: Configuration,
                                applicationLifecycle: ApplicationLifecycle)
                               (implicit val ec: ExecutionContext) {

  lazy val mongoConf = conf.getString("mongodb.uri").getOrElse("mongodb://localhost")
  lazy val client = MongoClient(mongoConf)
  lazy val codecRegistry = new DynamicCodecRegistry

  applicationLifecycle.addStopHook { () =>
    Future.successful(client.close())
  }

  def database(name: String): MongoDatabase =
    client.getDatabase(name).withCodecRegistry(codecRegistry)

  def ping(): Future[Unit] =
    client.listDatabaseNames().toFuture.map(_ => ())
}
