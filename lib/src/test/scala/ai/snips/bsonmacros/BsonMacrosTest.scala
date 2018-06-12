package ai.snips.bsonmacros

import java.time.Instant
import java.util.UUID

import org.bson.types.ObjectId
import org.mongodb.scala.bson.{BsonDocument, BsonInt64, BsonObjectId, ObjectId}
import org.scalatest._
import TauEnum.TauEnum

class BsonMacrosTest extends FlatSpec with Matchers {

  implicit val registry: DynamicCodecRegistry = new DynamicCodecRegistry

  case class Alpha(a: Integer, b: String, c: Double, d: Int, e: Boolean)

  CodecGen[Alpha](registry)
  "Alpha" should "be alpha" in {
    val a = Alpha(12, "foo", 42.12, 42, e = false)
    toDBObject(a) should be(BsonDocument("a" -> 12, "b" -> "foo", "c" -> 42.12, "d" -> 42, "e" -> false))
    fromDBObject[Alpha](toDBObject(a)) should be(a)
  }

  case class Beta(_id: String)

  CodecGen[Beta](registry)
  "Beta" should "be Beta" in {
    val beta = Beta("foobar")
    toDBObject(beta) should be(BsonDocument("_id" -> "foobar"))
    fromDBObject[Beta](toDBObject(beta)) should be(beta)
  }

  case class Gamma(_id: BsonObjectId)

  CodecGen[Gamma](registry)
  "Gamma" should "be Gamma" in {
    val gamma = Gamma(org.mongodb.scala.bson.BsonObjectId())
    toDBObject(gamma) should be(BsonDocument("_id" -> gamma._id))
    fromDBObject[Gamma](toDBObject(gamma)) should be(gamma)
  }

  case class Delta(_id: BsonObjectId, value: Option[String])

  CodecGen[Delta](registry)
  "Delta" should "be Delta" in {
    val delta = Delta(org.mongodb.scala.bson.BsonObjectId(), value = Some("foobar"))
    toDBObject(delta) should be(BsonDocument("_id" -> delta._id, "value" -> "foobar"))
    fromDBObject[Delta](toDBObject(delta)) should be(delta)

    val delta2 = Delta(org.mongodb.scala.bson.BsonObjectId(), value = None)
    toDBObject(delta2) should be(BsonDocument("_id" -> delta2._id))
    fromDBObject[Delta](toDBObject(delta2)) should be(delta2)
  }

  case class Epsilon(_id: BsonObjectId, delta: Delta)

  CodecGen[Epsilon](registry)
  "Epsilon" should "be Epsilon" in {
    val epsilon = Epsilon(org.mongodb.scala.bson.BsonObjectId(),
      Delta(org.mongodb.scala.bson.BsonObjectId(), value = None))
    toDBObject(epsilon) should be(BsonDocument("_id" -> epsilon._id,
      "delta" -> BsonDocument("_id" -> epsilon.delta._id)))
    fromDBObject[Epsilon](toDBObject(epsilon)) should be(epsilon)
  }

  case class Zeta(_id: BsonObjectId, delta: Option[Delta])

  CodecGen[Zeta](registry)
  "Zeta" should "be Zeta" in {
    val zeta1 = Zeta(org.mongodb.scala.bson.BsonObjectId(),
      Some(Delta(org.mongodb.scala.bson.BsonObjectId(), value = None)))
    toDBObject(zeta1) should be(BsonDocument("_id" -> zeta1._id,
      "delta" -> BsonDocument("_id" -> zeta1.delta.get._id)))
    fromDBObject[Zeta](toDBObject(zeta1)) should be(zeta1)

    val zeta2 = Zeta(org.mongodb.scala.bson.BsonObjectId(), None)
    toDBObject(zeta2) should be(BsonDocument("_id" -> zeta2._id))
    fromDBObject[Zeta](toDBObject(zeta2)) should be(zeta2)
  }

  case class Eta(_id: BsonObjectId, defaulting: Int = 12)

  CodecGen[Eta](registry)
  "Eta" should "support default values" in {
    pending
    fromDBObject[Eta](BsonDocument("_id" -> BsonObjectId()))
  }

  case class Theta(_id: BsonObjectId, a: Int)

  CodecGen[Theta](registry)
  "Theta" should "fail with helpful error message" in {
    try {
      fromDBObject[Theta](BsonDocument("_id" -> BsonObjectId()))
      fail()
    } catch {
      case e: RuntimeException if e.getMessage.contains("No value found for required field `a'") => ()
    }
  }

  case class Iota(_id: BsonObjectId, a: Seq[Int])

  CodecGen[Iota](registry)
  "Iota" should "have Seq[prim] support" in {
    val iota1 = Iota(org.mongodb.scala.bson.BsonObjectId(), Seq(1, 2, 3))
    toDBObject(iota1) should be(BsonDocument("_id" -> iota1._id, "a" -> Seq(1, 2, 3)))
    fromDBObject[Iota](toDBObject(iota1)) should be(iota1)
  }

  case class Kappa(_id: BsonObjectId, a: Seq[Beta])

  CodecGen[Kappa](registry)
  "Kappa" should "have Seq[Object] support" in {
    val kappa1 = Kappa(org.mongodb.scala.bson.BsonObjectId(), Seq(Beta("foo")))
    toDBObject(kappa1) should be(BsonDocument("_id" -> kappa1._id, "a" -> Seq(BsonDocument("_id" -> "foo"))))
    fromDBObject[Kappa](toDBObject(kappa1)) should be(kappa1)
  }

  case class Lambda(_id: BsonObjectId,
                    map1: Map[String, String],
                    map2: Map[Int, Int],
                    map3: Map[Long, Long],
                    map4: Map[Double, Double],
                    map5: Map[Boolean, Boolean],
                    map6: Map[Instant, Instant],
                    map7: Map[ObjectId, ObjectId],
                    map8: Map[UUID, UUID])

  CodecGen[Lambda](registry)
  "Lambda" should "have Map support" in {
    val lambda1 = Lambda(org.mongodb.scala.bson.BsonObjectId(),
	    Map("foo1" -> "bar1", "foo2" -> "bar2"),
	    Map(1 -> 12, 2 -> 42),
	    Map(1L -> 12L, 2L -> 42L),
	    Map(1.0 -> 1.0, 2.0 -> 2.0),
	    Map(true -> true, false -> false),
	    Map(Instant.now -> Instant.now, Instant.EPOCH -> Instant.EPOCH),
	    Map(ObjectId.get() -> ObjectId.get(), ObjectId.get() -> ObjectId.get()),
	    Map(UUID.randomUUID() -> UUID.randomUUID(), UUID.randomUUID() -> UUID.randomUUID()))
    fromDBObject[Lambda](toDBObject(lambda1)) should be(lambda1)
  }

  case class Mu(_id: BsonObjectId, long: Long)

  CodecGen[Mu](registry)
  "Mu" should "have Long support" in {
    val mu1 = Mu(org.mongodb.scala.bson.BsonObjectId(), 42)
    toDBObject(mu1) should be(BsonDocument("_id" -> mu1._id, "long" -> BsonInt64(42)))
    fromDBObject[Mu](toDBObject(mu1)) should be(mu1)
  }

  sealed trait Nu
  final case object NuObj extends Nu
  final case class NuCC(x: String, y: Int) extends Nu


  CodecGen[Nu](registry)
  "Nu" should "support sealed types" in {
    toDBObject(NuObj) shouldBe BsonDocument("__type" -> "NuObj")
    fromDBObject[Nu](toDBObject(NuObj)) shouldBe NuObj
    val cc1 = NuCC("hello", 1)
    toDBObject(cc1) shouldBe BsonDocument("__type" -> "NuCC", "payload" -> BsonDocument("x" -> cc1.x, "y" -> cc1.y))
    fromDBObject[Nu](toDBObject(cc1)) shouldBe cc1
  }

  sealed abstract class Xi(v: String)
  final case object XiObj extends Xi("test")
  final case class XiCC(a: String, b: Double) extends Xi(a)

  CodecGen[Xi](registry)
  "Xi" should "support sealed types" in {

    toDBObject(XiObj) shouldBe BsonDocument("__type" -> "XiObj")
    fromDBObject[Xi](toDBObject(XiObj)) shouldBe XiObj

    val cc2 = XiCC(a = "what", b = 2.4d)
    toDBObject(cc2) shouldBe BsonDocument("__type" -> "XiCC", "payload" -> BsonDocument("a" -> cc2.a, "b" -> cc2.b))
    fromDBObject[Xi](toDBObject(cc2)) shouldBe cc2
  }


  final case class Omnicron(first: Nu, other: Option[Xi])

  CodecGen[Omnicron](registry)
  "Xi" should "support composite ADTs" in {
    val omnicron1 = Omnicron(NuObj, Some(XiCC("a", 2.4)))
    fromDBObject[Omnicron](toDBObject(omnicron1)) shouldBe omnicron1
  }

  case class Point(x: Int, y: Int)
  CodecGen[Point](registry)
  sealed trait Shape
  final case class Rectangle(bottomLeft: Point, topRight: Point) extends Shape
  final case class Circle(center: Point, radius: Int) extends Shape
  final case object Empty extends Shape
  CodecGen[Shape](registry)

  "Readme sample" should "work" in {
    val bl = Point(0, 0)
    val tr = Point(5, 10)
    val rect = Rectangle(bl, tr)
    val c = Point(1,1)
    val circle = Circle(c, 2)
    Seq(Empty, circle, rect).foreach { s =>
      fromDBObject[Shape](toDBObject(s)) shouldBe s
    }
  }

	case class Quoppa(_id: BsonObjectId, a: Set[Int])
	CodecGen[Quoppa](registry)

	"Quoppa" should "have Set[Int] support" in {
		val q = Quoppa(org.mongodb.scala.bson.BsonObjectId(), Set(1, 2, 3))
		toDBObject(q) should be(BsonDocument("_id" -> q._id, "a" -> Seq(1, 2, 3)))
		fromDBObject[Quoppa](toDBObject(q)) should be(q)
	}

	case class Rho(_id: BsonObjectId, value: Either[String, Int])

	CodecGen[Rho](registry)
	"Rho" should "support Either[String, Int]" in {
		val left = Rho(org.mongodb.scala.bson.BsonObjectId(), Left("string"))
		toDBObject(left) shouldBe BsonDocument("_id" -> left._id, "value" -> BsonDocument("left" -> "string"))
		fromDBObject[Rho](toDBObject(left)) should be(left)

		val right = Rho(org.mongodb.scala.bson.BsonObjectId(), Right(42))
		toDBObject(right) shouldBe BsonDocument("_id" -> right._id, "value" -> BsonDocument("right" -> 42))
		fromDBObject[Rho](toDBObject(right)) should be(right)
	}

	case class San(x: ObjectId, y: UUID)
	CodecGen[San](registry)

	"ObjectId and UUID" should "work" in {
		val a = San(ObjectId.get(), UUID.randomUUID())
		fromDBObject[San](toDBObject(a)) should be(a)
	}

	case class Tau(_id: BsonObjectId, a: Map[TauEnum, String], b: TauEnum, c: Seq[TauEnum])

	CodecGen[Tau](registry)
	"Tau" should "support Enumeration" in {
		val enum = Tau(org.mongodb.scala.bson.BsonObjectId(), Map(TauEnum.One -> "One"), TauEnum.Two, Seq(TauEnum.Two))
		fromDBObject[Tau](toDBObject(enum)) should be(enum)
	}

	case class Upsilon(_id: BsonObjectId, a: List[String], b: List[Int])

	CodecGen[Upsilon](registry)
	"Upsilon" should "support List" in {
		val a = Upsilon(org.mongodb.scala.bson.BsonObjectId(), List("One", "Two", "Three"), List(1, 2, 3))
		fromDBObject[Upsilon](toDBObject(a)) should be(a)
	}
}