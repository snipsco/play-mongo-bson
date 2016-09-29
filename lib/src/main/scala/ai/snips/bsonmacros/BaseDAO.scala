package ai.snips.bsonmacros

import ai.snips.bsonmacros
import ai.snips.bsonmacros.BsonMagnets.CanBeBsonValue
import org.mongodb.scala._
import org.mongodb.scala.bson.{Document => _, _}
import org.mongodb.scala.model._

import scala.concurrent._

object BsonMagnets {

  sealed trait CanBeBsonValue {
    val value: BsonValue
  }

  implicit def singleToCanBeBsonValue[T]
  (v: T)
  (implicit transformer: BsonTransformer[T]): CanBeBsonValue = {
    new CanBeBsonValue {
      override val value = transformer(v)
    }
  }
}

abstract class BaseDAO[DO](implicit ct: scala.reflect.ClassTag[DO],
                           ec: ExecutionContext) {
  def collection: MongoCollection[DO]

  def byIdSelector(id: CanBeBsonValue): Document = {
    Document("_id" -> id.value)
  }

  def findOneById(id: CanBeBsonValue): Future[Option[DO]] =
    findOne(byIdSelector(id))

  def findOne(document: Document): Future[Option[DO]] =
    collection.find[DO](document).first.toFuture.map(_.headOption)

  def updateOneById(id: CanBeBsonValue, update: Document): Future[_] =
    collection.updateOne(byIdSelector(id), update).toFuture

  def replaceOne(it: DO): Future[_] = {
    implicit val codecs = collection.codecRegistry
    val bson = bsonmacros.toDBObject(it)
    collection.replaceOne(byIdSelector(bson.get("_id")), it).toFuture
  }

  def insertOne(it: DO): Future[_] = {
    collection.insertOne(it).toFuture
  }

  def upsertOne(it: DO): Future[_] = {
    implicit val codecs = collection.codecRegistry
    val bson = bsonmacros.toDBObject(it)
    collection.replaceOne(byIdSelector(bson.get("_id")), it,
      UpdateOptions().upsert(true)).toFuture
  }

  def all: Observable[DO] = collection.find()

  def find(bson: Document): Observable[DO] = collection.find(bson)

  def toDBObject(o: DO) = {
    implicit val codecs = collection.codecRegistry
    bsonmacros.toDBObject(o)
  }
}
