package com.github.nullptr7.jenademo

import cats.effect.IO
import com.github.nullptr7.streamer.{ InsertLogic, Triple }
import fs2.Chunk
import org.apache.jena.query.{ Dataset, ReadWrite }
import org.apache.jena.rdf.model.{ ResourceFactory, Statement }

final class ApacheJenaInsertionLogic(val dataset: Dataset, graphIri: String) extends InsertLogic {
  private var count: Long = 0L

  private def execute(triples: Chunk[Triple]): Unit = {
    println(s"Inserting ${triples.size} triples")
    count += doInsert(triples)
    println(s"Total $count triples")
  }

  private def doInsert(triples: Chunk[Triple]): Int = {
    val arrayOfStatements =
      triples.map { triple =>
        val subject   = ResourceFactory.createResource(triple.s)
        val obj       = ResourceFactory.createResource(triple.o)
        val predicate = ResourceFactory.createProperty(triple.p)
        // println(s"Triple: $subject $predicate $obj")
        ResourceFactory.createStatement(subject, predicate, obj)
      }.toArray

    val model = dataset.getNamedModel(graphIri)
    model.add(arrayOfStatements)
    arrayOfStatements.length
  }

  override def insert(triples: Chunk[Triple]): IO[Unit] =
    IO.blocking {
      begin()
      execute(triples)
      commit()
      close()
    }

  private def begin(): Unit = dataset.begin(ReadWrite.WRITE)

  private def isBusy: Boolean = dataset.isInTransaction

  private def commit(): Unit = dataset.commit()

  private def close(): Unit = {
    dataset.end()
    dataset.close()
  }

}
