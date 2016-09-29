package ai.snips

import org.bson._
import org.bson.codecs._
import org.bson.codecs.configuration._

import scala.reflect._

package object bsonmacros {

  def toDBObject(a: Any)(implicit repo: CodecRegistry) = {
    val codec: Encoder[Any] = repo.get(a.getClass).asInstanceOf[Encoder[Any]]
    val doc = new BsonDocument()
    val writer = new BsonDocumentWriter(doc)
    codec.encode(writer, a, EncoderContext.builder().build())
    doc
  }

  def fromDBObject[T](doc: BsonDocument)(implicit ct: ClassTag[T], repo: CodecRegistry): T = {
    val codec: Decoder[Any] = repo.get(ct.runtimeClass).asInstanceOf[Decoder[Any]]
    val reader = new BsonDocumentReader(doc)
    codec.decode(reader, DecoderContext.builder().build()).asInstanceOf[T]
  }
}
