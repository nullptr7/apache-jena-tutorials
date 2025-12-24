package com.github.nullptr7.jenademo

import cats.effect.{ IO, IOApp }
import com.github.nullptr7.streamer.TripleInsertionService
import fs2.io.file.Path
import org.apache.jena.query.Dataset
import org.apache.jena.tdb2.TDB2Factory

object StreamInsert extends IOApp.Simple {
  private val dataset:  Dataset                  = TDB2Factory.connectDataset("D:\\softwares\\apache-jena-fuseki-5.6.0\\db")
  private val graphIri: String                   = "http://com.github.nullptr7/graph/countries"
  private val logic:    ApacheJenaInsertionLogic = new ApacheJenaInsertionLogic(dataset, graphIri)

  private val tripleInsertionService: TripleInsertionService =
    new TripleInsertionService(Path("C:\\Users\\User\\Desktop\\pp\\triples_10m.csv"), logic)

  override def run: IO[Unit] = IO.delay(logic.compat())

}
