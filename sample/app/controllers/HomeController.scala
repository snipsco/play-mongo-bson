package controllers

import javax.inject._

import ai.snips.bsonmacros.DatabaseContext
import models.{SampleData, SampleDataDAO}
import org.mongodb.scala.bson.BsonObjectId
import play.api.Logger
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

@Singleton
class HomeController @Inject()(sampleDataDAO: SampleDataDAO,
                               dbContext: DatabaseContext)
                              (implicit ec: ExecutionContext) extends Controller {

  def index = Action.async {
    val data = SampleData(BsonObjectId(), "sample", (Math.random() * 10).toInt)

    val future = for {
      _ <- dbContext.ping()
      _ = Logger.info("ping successful")

      _ <- sampleDataDAO.insertOne(data)

      all <- sampleDataDAO.all.toFuture()
      _ = Logger.info(s"collection data: $all")
    } yield {
      Ok(s"index: ${all.map(_.int).mkString(", ")}")
    }

    future.recover {
      case NonFatal(e) =>
        Logger.error("something went wrong", e)
        InternalServerError(e.getMessage)
    }
  }

}
