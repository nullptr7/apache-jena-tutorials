package com.github.nullptr7.jenademo

import org.apache.jena.query.{ Dataset, ReadWrite }
import org.apache.jena.rdf.model.*
import org.apache.jena.tdb2.TDB2Factory

import scala.collection.JavaConverters.asScalaIteratorConverter

object AnotherMainApp extends App {

  // Optional: if you really need Jena's Seq somewhere, alias it like this:
  // import org.apache.jena.rdf.model.{Seq as JenaSeq}
  object TDB2DataAccess:
    private val storeDir = "D:\\softwares\\apache-jena-fuseki-5.6.0\\db"
    private val dataset: Dataset = TDB2Factory.connectDataset(storeDir)

    /** Given Subject + Predicate, return all Objects as a Scala collection. */
    def ObjectDataAccess(subjectIri: String, predicateIri: String): scala.collection.immutable.Seq[RDFNode] =
      dataset.begin(ReadWrite.READ)
      try
        val model: Model    = dataset.getDefaultModel
        val s:     Resource = ResourceFactory.createResource(subjectIri)
        val p:     Property = ResourceFactory.createProperty(predicateIri)
        // Convert Jena iterator to a Scala Vector (an immutable Seq)
        model.listObjectsOfProperty(s, p).asScala.toVector
      finally dataset.end()

    /** Given Predicate + Object (literal), return all matching Subjects (Scala collection). */
    def SubjectDataAccess(predicateIri: String, objectValue: String): scala.collection.immutable.Seq[Resource] =
      dataset.begin(ReadWrite.READ)
      try
        val model: Model    = dataset.getDefaultModel
        val p:     Property = ResourceFactory.createProperty(predicateIri)
        val o:     RDFNode  = ResourceFactory.createStringLiteral(objectValue)
        model.listSubjectsWithProperty(p, o).asScala.toVector
      finally dataset.end()

    def close(): Unit = dataset.close()

  val subject   = "http://learningsparql.com/ns/addressBook#jaz"
  val predicate = "http://learningsparql.com/ns/addressBook#email"

  println(s"Objects for <$subject> <$predicate>:")
  TDB2DataAccess.ObjectDataAccess(subject, predicate).foreach { obj =>
    if obj.isLiteral then println(s"""  "${obj.asLiteral.getString}"""")
    else println(s"Objects for subject: <$subject> & predicate: <$predicate> --> <${obj.asResource.getURI}>")
  }

  val objValue = "jaz@in.com"
  TDB2DataAccess.SubjectDataAccess(predicate, objValue).foreach { subj =>
    println(s"Subjects for predicate: <$predicate> object: $objValue --> <${subj.getURI}>")
  }

  TDB2DataAccess.close()

}
