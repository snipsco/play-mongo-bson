package ai.snips

import org.bson._
import org.bson.codecs._
import org.bson.codecs.configuration._

import scala.reflect.runtime.universe._
import scala.reflect._

package object bsonmacros {

	private val mirror: Mirror = runtimeMirror(getClass.getClassLoader)

	def reflectEnum[T: TypeTag](name: String): T = {
		typeOf[T] match {
			case TypeRef(enumType, _, _) =>
				val methodSymbol = enumType.member(TermName("withName")).asMethod
				val moduleSymbol = enumType.termSymbol.asModule
				reflect(moduleSymbol, methodSymbol)(name).asInstanceOf[T]
		}
	}

	private def reflect(module: ModuleSymbol, method: MethodSymbol)(args: Any*): Any = {
		val moduleMirror = mirror.reflectModule(module)
		val instanceMirror = mirror.reflect(moduleMirror.instance)
		instanceMirror.reflectMethod(method)(args:_*)
	}

	def toDBObject(a: Any)(implicit repo: CodecRegistry): BsonDocument = {
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
