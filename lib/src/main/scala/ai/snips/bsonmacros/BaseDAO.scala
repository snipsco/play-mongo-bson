package ai.snips.bsonmacros

import ai.snips.bsonmacros
import ai.snips.bsonmacros.BsonMagnets.CanBeBsonValue
import org.mongodb.scala._
import org.mongodb.scala.bson.{BsonTransformer, BsonValue, Document}
import org.mongodb.scala.model._

import scala.concurrent._
import scala.reflect.ClassTag

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

abstract class BaseDAO[DO](implicit ct: ClassTag[DO],
                           ec: ExecutionContext) {

  def collection: MongoCollection[DO]

  private val ID = "_id"

  implicit lazy val codecs = collection.codecRegistry

  def byIdSelector(id: CanBeBsonValue): Document = Document(ID -> id.value)

  def findOneById(id: CanBeBsonValue): Future[Option[DO]] =
    findOne(byIdSelector(id))

  def findOne(document: Document): Future[Option[DO]] =
    collection.find[DO](document).limit(1).toFuture.map(_.headOption)

  def updateOneById(id: CanBeBsonValue, update: Document): Future[_] =
    collection.updateOne(byIdSelector(id), update).toFuture

  def replaceOne(it: DO): Future[_] =
    collection.replaceOne(byIdSelector(getId(it)), it).toFuture

  def insertOne(it: DO): Future[_] =
    collection.insertOne(it).toFuture

  def upsertOne(it: DO): Future[_] =
    collection.replaceOne(byIdSelector(getId(it)), it,
      UpdateOptions().upsert(true)).toFuture

  def all: FindObservable[DO] = collection.find()

  def find(bson: Document): FindObservable[DO] = collection.find(bson)

  private def getId(it: DO) = bsonmacros.toDBObject(it).get(ID)
}
