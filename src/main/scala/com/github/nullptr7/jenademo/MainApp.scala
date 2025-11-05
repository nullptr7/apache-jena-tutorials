package com.github.nullptr7.jenademo

import org.apache.jena.fuseki.main.FusekiServer
import org.apache.jena.query.{ Dataset, DatasetFactory }

import org.apache.jena.query.{ Dataset, QueryExecutionFactory, QueryFactory, ReadWrite, ResultSetFormatter }
import org.apache.jena.rdf.model.{ Model, ResourceFactory, StmtIterator }
import org.apache.jena.tdb2.TDB2Factory

object MainApp extends App {
  case class Config(
      storeDir: String         = "db",
      dump:     Boolean        = false,
      person:   Option[String] = None,
    )

  def parse(xs: List[String], acc: Config = Config()): Config = xs match
    case Nil                       => acc
    case "--store" :: dir :: tail  => parse(tail, acc.copy(storeDir = dir))
    case "--dump" :: tail          => parse(tail, acc.copy(dump = true))
    case "--person" :: iri :: tail => parse(tail, acc.copy(person = Some(iri)))
    case _ :: tail                 => parse(tail, acc)

  val cfg = parse(List("--store", "D:\\softwares\\apache-jena-fuseki-5.6.0\\db"))

  // Connect to the same TDB2 dataset that Fuseki uses (--loc=db)
  val dataset: Dataset = TDB2Factory.connectDataset(cfg.storeDir)

  dataset.begin(ReadWrite.READ)
  try
    if cfg.dump then
      println(s"--- Dumping all triples from ${cfg.storeDir} ---")
      val model: Model        = dataset.getDefaultModel
      val it:    StmtIterator = model.listStatements()
      while it.hasNext do println(it.nextStatement().toString)
    else
      // Example SPARQL: list each person, their homeTel and emails
      val queryStr =
        """
          |PREFIX ab: <http://learningsparql.com/ns/addressBook#>
          |
          |SELECT ?gazEmail
          |WHERE { ab:gaz ab:email ?gazEmail . }
          |""".stripMargin

      val q  = QueryFactory.create(queryStr)
      val qe = QueryExecutionFactory.create(q, dataset)
      val rs = qe.execSelect()
      println("--- All persons with emails and phone numbers ---")
      ResultSetFormatter.out(System.out, rs, q)
      qe.close()

      // Optional: look up a specific person (e.g., ab:gaz)
      cfg.person.foreach { iri =>
        val model = dataset.getDefaultModel
        val subj  = ResourceFactory.createResource(iri)
        val it    = model.listStatements(subj, null, null.asInstanceOf[org.apache.jena.rdf.model.RDFNode])
        println(s"\n--- Triples about <$iri> ---")
        while it.hasNext do println(it.nextStatement().toString)
      }
  finally
    dataset.end()
    dataset.close()

}
