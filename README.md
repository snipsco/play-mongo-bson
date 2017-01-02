Play Mongo BSON
=================================

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
line: same API everywhere. This is getting frustrated when switching between
languages constantly.

Configuration
===========

Provide "mongodb.uri" in application.conf
If not provided "mongodb://localhost" will be used

Sample
==========

Go to [sample project](sample) (HomeController, SampleDataDAO) on how to use it
