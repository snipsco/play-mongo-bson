package ai.snips.bsonmacros

import org.mongodb.scala.bson.{BsonDocument, BsonObjectId}
import org.scalatest._

class BsonMacrosTest extends FlatSpec with Matchers {

  implicit val registry = new DynamicCodecRegistry

  case class Alpha(a: Integer, b: String, c: Double, d: Int)

  CodecGen[Alpha](registry)
  "Alpha" should "be alpha" in {
    val a = Alpha(12, "foo", 42.12, 42)
    toDBObject(a) should be(BsonDocument("a" -> 12, "b" -> "foo", "c" -> 42.12, "d" -> 42))
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

  case class Iota(_id: BsonObjectId, a: List[Int])

  CodecGen[Iota](registry)
  "Iota" should "have list[prim] support" in {
    val iota1 = Iota(org.mongodb.scala.bson.BsonObjectId(), List(1, 2, 3))
    toDBObject(iota1) should be(BsonDocument("_id" -> iota1._id, "a" -> List(1, 2, 3)))
    fromDBObject[Iota](toDBObject(iota1)) should be(iota1)
  }

  case class Kappa(_id: BsonObjectId, a: List[Beta])

  CodecGen[Kappa](registry)
  "Kappa" should "have list[Object] support" in {
    val kappa1 = Kappa(org.mongodb.scala.bson.BsonObjectId(), List(Beta("foo")))
    toDBObject(kappa1) should be(BsonDocument("_id" -> kappa1._id, "a" -> List(BsonDocument("_id" -> "foo"))))
    fromDBObject[Kappa](toDBObject(kappa1)) should be(kappa1)
  }

  case class Lambda(_id: BsonObjectId, map: Map[String, Int])

  CodecGen[Lambda](registry)
  "Lambda" should "have Map[String,Int] support" in {
    val lambda1 = Lambda(org.mongodb.scala.bson.BsonObjectId(), Map("foo" -> 12, "bar" -> 42))
    toDBObject(lambda1) should be(BsonDocument("_id" -> lambda1._id, "map" -> BsonDocument("foo" -> 12, "bar" -> 42)))
    fromDBObject[Lambda](toDBObject(lambda1)) should be(lambda1)
  }
}
