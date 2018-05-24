package ai.snips.bsonmacros

import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

import org.bson._
import org.bson.codecs._
import org.bson.codecs.configuration._
import org.bson.codecs.{LongCodec => BsonLongCodec, ObjectIdCodec => BsonObjectIdCodec }
import org.mongodb.scala.bson.ObjectId

import scala.collection.concurrent
import scala.language.experimental.macros
import scala.reflect.macros.whitebox
import scala.util.Try

class DoubleCodec extends Codec[Double] {
  def getEncoderClass: Class[Double] = classOf[Double]

  val inner = new BsonDoubleCodec

  def encode(writer: BsonWriter, it: Double, encoderContext: EncoderContext) {
    inner.encode(writer, new BsonDouble(it), encoderContext)
  }

  def decode(reader: BsonReader, decoderContext: DecoderContext): Double = {
    inner.decode(reader, decoderContext).getValue
  }
}

class IntCodec extends Codec[Int] {
  def getEncoderClass: Class[Int] = classOf[Int]

  val inner = new IntegerCodec

  def encode(writer: BsonWriter, it: Int, encoderContext: EncoderContext) {
    inner.encode(writer, it, encoderContext)
  }

  def decode(reader: BsonReader, decoderContext: DecoderContext): Int = {
    inner.decode(reader, decoderContext)
  }
}

class LongCodec extends Codec[Long] {
  def getEncoderClass: Class[Long] = classOf[Long]

  val inner = new BsonLongCodec

  def encode(writer: BsonWriter, it: Long, encoderContext: EncoderContext) {
    inner.encode(writer, it, encoderContext)
  }

  def decode(reader: BsonReader, decoderContext: DecoderContext): Long = {
    inner.decode(reader, decoderContext)
  }
}

class BooleanCodec extends Codec[Boolean] {
  def getEncoderClass: Class[Boolean] = classOf[Boolean]

  val inner = new org.bson.codecs.BooleanCodec

  def encode(writer: BsonWriter, it: Boolean, encoderContext: EncoderContext) {
    inner.encode(writer, it, encoderContext)
  }

  def decode(reader: BsonReader, decoderContext: DecoderContext): Boolean = {
    inner.decode(reader, decoderContext)
  }
}

class InstantCodec extends Codec[Instant] {
  def getEncoderClass: Class[Instant] = classOf[Instant]

  val inner = new BsonDateTimeCodec

  def encode(writer: BsonWriter, it: Instant, encoderContext: EncoderContext) {
    inner.encode(writer, new BsonDateTime(it.toEpochMilli), encoderContext)
  }

  def decode(reader: BsonReader, decoderContext: DecoderContext): Instant = {
    Instant.ofEpochMilli(inner.decode(reader, decoderContext).getValue)
  }
}

class ObjectIdCodec extends Codec[ObjectId] {
	def getEncoderClass: Class[ObjectId] = classOf[ObjectId]

	val inner = new BsonObjectIdCodec

	def encode(writer: BsonWriter, it: ObjectId, encoderContext: EncoderContext) {
		inner.encode(writer, it, encoderContext)
	}

	def decode(reader: BsonReader, decoderContext: DecoderContext): ObjectId = {
		inner.decode(reader, decoderContext)
	}
}

class UUIDCodec extends Codec[UUID] {
	def getEncoderClass: Class[UUID] = classOf[UUID]

	val inner = new UuidCodec

	def encode(writer: BsonWriter, it: UUID, encoderContext: EncoderContext) {
		inner.encode(writer, it, encoderContext)
	}

	def decode(reader: BsonReader, decoderContext: DecoderContext): UUID = {
		inner.decode(reader, decoderContext)
	}
}

class SeqCodec[T](inner: Codec[T]) extends Codec[Seq[T]] {
  def getEncoderClass: Class[Seq[T]] = classOf[Seq[T]]

  def encode(writer: BsonWriter, it: Seq[T], encoderContext: EncoderContext) {
    writer.writeStartArray()
    it.foreach(inner.encode(writer, _, encoderContext))
    writer.writeEndArray()
  }

  def decode(reader: BsonReader, decoderContext: DecoderContext): Seq[T] = {
    reader.readStartArray()
    val buffer = scala.collection.mutable.Buffer[T]()
    while (reader.readBsonType != BsonType.END_OF_DOCUMENT) {
      buffer.append(inner.decode(reader, decoderContext))
    }
    reader.readEndArray()
    buffer
  }
}

class MapCodec[V](inner: Codec[V]) extends Codec[Map[String, V]] {
  def getEncoderClass: Class[Map[String, V]] = classOf[Map[String, V]]

  def encode(writer: BsonWriter, it: Map[String, V], encoderContext: EncoderContext) {
    writer.writeStartDocument()
    it.foreach { case (k, v) =>
      writer.writeName(k)
      inner.encode(writer, v, encoderContext)
    }
    writer.writeEndDocument()
  }

  def decode(reader: BsonReader, decoderContext: DecoderContext): Map[String, V] = {
    reader.readStartDocument()
    val buffer = scala.collection.mutable.Buffer[(String, V)]()
    while (reader.readBsonType != BsonType.END_OF_DOCUMENT) {
      buffer.append((reader.readName, inner.decode(reader, decoderContext)))
    }
    reader.readEndDocument()
    buffer.toMap
  }
}

class ExistentialCodec[T](v: T) extends Codec[T] {
  override def encode(writer: BsonWriter, value: T, encoderContext: EncoderContext) {}

  override def getEncoderClass: Class[T] = v.getClass.asInstanceOf[Class[T]]

  override def decode(reader: BsonReader, decoderContext: DecoderContext): T = v
}


class DynamicCodecRegistry extends CodecRegistry {

  import collection.JavaConverters._

  def get[T](it: Class[T]): Codec[T] = Try {
    providedCodecs.get(it)
  }.toOption.orElse {
    Some(registered(it).asInstanceOf[Codec[T]])
  }.get

  def register[T](codec: Codec[T]) {
    registered.put(codec.getEncoderClass, codec)
  }

  def registerFor[T, V <: T](codec: Codec[T], v: Class[V]) {
    registered.put(v, codec)
  }

  val providedCodecs: CodecRegistry =
    CodecRegistries.fromRegistries(
      org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY,
      CodecRegistries.fromCodecs(new DoubleCodec, new IntCodec, new LongCodec, new InstantCodec, new BooleanCodec,
	      new UUIDCodec, new ObjectIdCodec)
    )

  val registered: concurrent.Map[Class[_], Codec[_]] =
    new ConcurrentHashMap[Class[_], Codec[_]]().asScala
}

object CodecGen {

  def apply[T](registry: DynamicCodecRegistry): Codec[T] = macro registerCodec[T]

  def gen[T](registry: DynamicCodecRegistry): Codec[T] = macro materializeCodec[T]

  def forSealedImpl[T: c.WeakTypeTag](c: whitebox.Context)(registry: c.Expr[DynamicCodecRegistry]): c.Expr[Codec[T]] = {
    import c.universe._
    val tpe = c.weakTypeOf[T]
    val ctpe = tpe.typeSymbol.asClass
    require(ctpe.isSealed)
    require(ctpe.knownDirectSubclasses.nonEmpty)
    val (objects, caseClasses) = {
      val (os, cs) = ctpe.knownDirectSubclasses.partition(_.isModuleClass)
      (os.map(_.asClass.module.asModule), cs.map(_.asClass))
    }
    def nameOf(s: Symbol) = s.fullName.split("\\.").last

    val objCodecs = objects.map(o =>
      q"""${nameOf(o)} -> {
         new ai.snips.bsonmacros.ExistentialCodec[$tpe]($o).asInstanceOf[Codec[$tpe]]
        }"""
    )

    val ccCodecs = caseClasses.map(cc =>
      q"${nameOf(cc)} -> ai.snips.bsonmacros.CodecGen.gen[$cc]($registry).asInstanceOf[Codec[$tpe]]"
    )

    val e = c.Expr[Codec[T]] {
      q"""{
        val codec = new org.bson.codecs.Codec[$tpe] {
          import org.bson._
          import org.bson.codecs._
          val codecs: Map[String, Codec[$tpe]] = Seq(..$objCodecs,..$ccCodecs).toMap

          private def rtNameOf(v: $tpe): String = {
            ${
            val cases = ctpe.knownDirectSubclasses.map {
              case c: ClassSymbol if c.isModuleClass => cq"x if x == ${c.module} => ${nameOf(c)}"
              case c: ClassSymbol                    => cq" _ : ${c.name}        => ${nameOf(c)}"
            }
            q"""
              v match {
                case ..$cases
              }
            """
            }

          }
          private val sentinelType = "__type"
          private val payloadName = "payload"
          override def getEncoderClass: Class[$tpe] = classOf[$tpe]

          override def encode(writer: BsonWriter, value: $tpe, encoderContext: EncoderContext) {
            val typeName = rtNameOf(value)
            writer.writeStartDocument
            writer.writeString(sentinelType, typeName)
            val codec = codecs(typeName)
            if (!codec.isInstanceOf[ai.snips.bsonmacros.ExistentialCodec[$tpe]]) {
              writer.writeName(payloadName)
            }
            codec.encode(writer, value, encoderContext)
            writer.writeEndDocument
          }

          override def decode(reader: BsonReader, decoderContext: DecoderContext): $tpe = {
            reader.readStartDocument
            val typeName = reader.readString(sentinelType)
            val codec = codecs(typeName)
            if (!codec.isInstanceOf[ai.snips.bsonmacros.ExistentialCodec[$tpe]]) {
              reader.readName(payloadName)
            }
            val value = codec.decode(reader, decoderContext)
            reader.readEndDocument
            value
          }

        }
        $registry.register(codec)
        ${
        val regObjs = objects.map(o => q"$registry.registerFor(codec, ${o.name}.getClass)")
        val regCCs = caseClasses.map(cc => q"$registry.registerFor(codec, classOf[$cc])")

        q"""{..$regCCs;..$regObjs}"""
        }
        codec
      }"""

    }
    e
  }

  def registerCodec[T: c.WeakTypeTag](c: whitebox.Context)(registry: c.Expr[DynamicCodecRegistry]): c.Expr[Codec[T]] = {
    import c.universe._
    c.Expr[Codec[T]] {
      q"""
        val e = ${materializeCodec[T](c)(registry)}
        $registry.register(e)
        e
      """
    }
  }

  def materializeCodec[T: c.WeakTypeTag](c: whitebox.Context)(registry: c.Expr[DynamicCodecRegistry]): c.Expr[Codec[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    if (tpe.typeSymbol.isAbstract) {
      // try generation for sealed classes
      return forSealedImpl[T](c)(registry)
    }

    val constructor = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }.get

    abstract sealed class FieldType {
      def tpe: Type

      def codecExpr: Tree
    }
    case class SimpleField(tpe: Type) extends FieldType {
      def codecExpr: Tree = {
        q"""$registry.get(classOf[${tpe.typeSymbol}]).asInstanceOf[Codec[Any]]"""
      }
    }
    case class SeqField(inner: FieldType) extends FieldType {
      def tpe: c.universe.Type = appliedType(typeOf[Seq[Any]].typeConstructor, List(inner.tpe))

      def codecExpr: Tree = q"""new ai.snips.bsonmacros.SeqCodec(${inner.codecExpr}).asInstanceOf[Codec[Any]]"""
    }
    case class MapField(inner: FieldType) extends FieldType {
      def tpe: c.universe.Type = appliedType(typeOf[Map[Any, Any]].typeConstructor, List(typeOf[String], inner.tpe))

      def codecExpr: Tree = q"""new ai.snips.bsonmacros.MapCodec(${inner.codecExpr}).asInstanceOf[Codec[Any]]"""
    }

    object FieldType {
      def apply(outer: Type): FieldType = {
        val TypeRef(_, _, inner) = outer
        if (inner.isEmpty) {
          SimpleField(outer)
        } else {
          if (outer.typeConstructor == typeOf[Seq[Any]].typeConstructor) {
            SeqField(FieldType(inner.head))
          } else if (outer.typeConstructor == typeOf[Map[String, Any]].typeConstructor) {
            MapField(FieldType(inner(1)))
          } else {
            throw new RuntimeException("Unsupported generic type mapping " + outer)
          }
        }
      }
    }

    case class Field(name: Name, option: Boolean, raw: FieldType)
    val fields = constructor.paramLists.head.map { field =>
      val name = field.name.decodedName
      val ftpe = tpe.decl(field.name).typeSignature.resultType
      val isOption = ftpe.typeConstructor == typeOf[Option[_]].typeConstructor
      val deoptioned: Type = if (isOption) {
        val TypeRef(_, _, inner) = ftpe;
        inner.head
      } else ftpe
      Field(name, isOption, FieldType(deoptioned))
    }

    val e = c.Expr[Codec[T]] {
      q"""{

      val codec = new org.bson.codecs.Codec[$tpe] {
        import org.bson._
        import org.bson.codecs._

        val log = play.api.Logger(getClass)

        val _codecs = ${
        fields.map { field =>
          q"""scala.util.Try {
            ${field.raw.codecExpr}
            }.getOrElse {
              throw new RuntimeException("Can not find Codec for " + ${field.raw.toString}
                + " while mapping case class " + ${tpe.typeSymbol.fullName.toString}
                )
            }
            """
        }
      }

        def getEncoderClass():Class[$tpe] = classOf[$tpe]

        def encode(writer: BsonWriter, it:$tpe, encoderContext: EncoderContext) {
          ${
        Block(
          Seq(q"""writer.writeStartDocument""") ++
            fields.zipWithIndex.flatMap { case (field, ix) =>
              if (field.option) {
                Seq(
                  q"""if(it.${field.name.toTermName}.isDefined) ${
                    Block(
                      q"""writer.writeName(${Literal(Constant(field.name.toString))})""",
                      q"""_codecs($ix).encode(writer, it.${field.name.toTermName}.get, encoderContext)""")
                  }""")
              } else {
                Seq(
                  q"""writer.writeName(${Literal(Constant(field.name.toString))})""",
                  q"""_codecs($ix).encode(writer, it.${field.name.toTermName}, encoderContext)""")
              }
            } ++
            Seq(q"""writer.writeEndDocument"""): _*
        )
      }}

        def decode(reader: BsonReader, decoderContext: DecoderContext):$tpe =
          ${
        Block(
          Seq( q"""reader.readStartDocument""") ++
            fields.map(f =>
              q"""var ${f.name.toTermName}:Option[${f.raw.tpe}] = None"""
            ) ++
            Seq(
              q"""while(reader.readBsonType != BsonType.END_OF_DOCUMENT) ${
                Match(
                  q"""reader.readName()""",
                  fields.zipWithIndex.map { case (field, ix) =>
                    CaseDef(Literal(Constant(field.name.decodedName.toString)),
                      q""" ${field.name.toTermName} = Some(_codecs($ix).decode(reader, decoderContext).asInstanceOf[${field.raw.tpe}]) """)
                  } ++ Seq(CaseDef(pq""" foo """, q"""log.debug("Ignore unmapped field `" + foo + "'"); reader.skipValue()"""))
                )
              }"""
            ) ++
            Seq( q"""reader.readEndDocument""") ++
            fields.filter(!_.option).map { field =>
              q"""if(${field.name.toTermName}.isEmpty) {
                throw new RuntimeException("No value found for required field `" + ${field.name.toTermName.toString} + "'")
              }"""
            } ++
            Seq(
              New(tpe, fields.map(f => if (f.option) q"""${f.name.toTermName}""" else q"""${f.name.toTermName}.get"""): _*)
            ): _*)
      }
      }
      codec
    }"""
    }
    e
  }
}
