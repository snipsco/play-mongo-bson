package models

import javax.inject.{Inject, Singleton}

import ai.snips.bsonmacros.{BaseDAO, CodecGen, DatabaseContext}
import org.mongodb.scala.bson.BsonObjectId

import scala.concurrent.ExecutionContext

case class SampleData(_id: BsonObjectId,
                      string: String,
                      boolean: Boolean,
                      int: Int)

@Singleton
class SampleDataDAO @Inject()(val dbContext: DatabaseContext)
                             (implicit ec: ExecutionContext) extends BaseDAO[SampleData] {

  CodecGen[SampleData](dbContext.codecRegistry)

  val db = dbContext.database("sample_db")

  override val collection = db.getCollection[SampleData]("sample_data")
}
