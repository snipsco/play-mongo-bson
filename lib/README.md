Play Mongo BSON
=================================

A mongo client for Play Framework using scala macros to serialize/deserialize 
case class as BSON documents.

Configuration
===========

Provide "mongodb.uri" in application.conf
If not provided "mongodb://localhost" will be used

Sample
==========

cf [sample] folder (HomeController, SampleDataDAO) on how to use it