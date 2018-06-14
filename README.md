Play Mongo BSON
===============

[![Build Status](https://travis-ci.org/snipsco/play-mongo-bson.svg?branch=master)](https://travis-ci.org/snipsco/play-mongo-bson)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](https://img.shields.io/badge/license-MIT-blue.svg)

A Scala MongoDB Object-Document-Mapper for Play Framework using macros to
serialize/deserialize case class as BSON documents. The idea is to get
something similar to [salat](https://github.com/salat/salat).

Salat was good in that it was a "small" library and not a full blown driver,
but is ageing bad: it was based on bytecode analysis and bytecode generation
whereas Scala compiler macros can be used now (simpler build, no more issues
with type elision). Also it was still bound (by the BSON types) to the "old"
synchronous MongoDB API.

Alternative projects are offering full driver and object mapping solution but
now that the official driver is asynchronous, they feel less relevant. Also,
they are re-inventing the whole API instead of sticking to the official MongoDB
line: same API for all drivers. This is getting frustrating when switching
between languages constantly.

build.sbt
---------

Import play-mongo-bson in your project. Only Scala 2.12 is supported.

You will need to provide also Scala mongo driver.

```
resolvers ++= Seq(
  ...
  "Sonatype OSS" at "https://oss.sonatype.org/content/groups/public"
)

libraryDependencies ++= Seq(
  ...
  "ai.snips" %% "play-mongo-bson" % "0.5",
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.1.0"
)
```

DAO
---

 - extend `BaseDAO[_]` by specifying you data class.
 - register you case case in the codec registry using `CodecGen`.
 - declare your mongo collection.

```
class SampleDataDAO @Inject()(val dbContext: DatabaseContext)
                             (implicit ec: ExecutionContext) extends BaseDAO[SampleData] {

  CodecGen[SampleData](dbContext.codecRegistry)

  val db = dbContext.database("sample_db")

  override val collection = db.getCollection[SampleData]("sample_data")
}
```


Configuration
-------------

Provide `"mongodb.uri"` in application.conf, if not provided `"mongodb://localhost"` will be used.

Sample
------

Go to [sample project](sample) ([HomeController](sample/app/controllers/HomeController.scala), [SampleDataDAO](sample/app/models/SampleDataDAO.scala)) on how to use it

Supported types
---------------

 - `String`
 - `Boolean`
 - `Int`
 - `Long`
 - `Double`
 - `ObjectId`
 - `UUID`
 - `Enumeration`
 - `java.time.Instant`
 - `Option[_]`
 - `Either[_,_]`
 - `Seq[_]`
 - `Set[_]`
 - `List[_]`
 - `Map[_,_]`
 - Simple monomorphic and non recursive ADTs

```
case class Point(x: Int, y: Int)
CodecGen[Point](registry)

sealed trait Shape
final case class Rectangle(bottomLeft: Point, topRight: Point) extends Shape
final case class Circle(center: Point, radius: Int) extends Shape
final case object Empty extends Shape
CodecGen[Shape](registry)
```

Default values are not yet supported (eg. `case class Foo(bar: Int = 12)`)

Publishing release
------------------

```
export SONATYPE_USERNAME=...
export SONATYPE_PASSWORD=...
cd lib
bin/activator release
```

This will publish artifacts in release and create a new snapshot.

License
-------

Licensed under MIT license ([LICENSE](LICENSE) or http://opensource.org/licenses/MIT)
