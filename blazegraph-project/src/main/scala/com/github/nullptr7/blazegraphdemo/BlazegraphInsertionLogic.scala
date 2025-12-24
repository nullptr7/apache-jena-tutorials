package com.github.nullptr7.blazegraphdemo

import cats.effect.IO
import com.bigdata.rdf.sail.BigdataSailRepositoryConnection
import com.github.nullptr7.streamer.{ InsertLogic, Triple }
import fs2.Chunk
import org.openrdf.model.impl.{ StatementImpl, URIImpl, ValueFactoryImpl }

import java.util

final class BlazegraphInsertionLogic private (connection: BigdataSailRepositoryConnection) extends InsertLogic {
  var count: Long = 0L

  private def convertToStatements(triples: Chunk[Triple]): IO[util.List[StatementImpl]] =
    IO.delay(
      triples.map { triple =>
        new StatementImpl(
          new URIImpl(triple.s),
          new URIImpl(triple.p),
          new URIImpl(triple.o),
        )
      }.asJava
    )

  override def insert(triples: Chunk[Triple]): IO[Unit] =
    for {
      statements <- convertToStatements(triples)
      _          <- IO.println(s"Inserting ${triples.size} triples")
      _          <- IO.blocking {
                      connection.begin()
                      connection.add(statements)
                      connection.commit()
                    }
      _          <- IO.pure(count += triples.size)
      _          <- IO.println(s"Total $count triples inserted")
    } yield ()

}

object BlazegraphInsertionLogic {
  def apply(connection: BigdataSailRepositoryConnection): BlazegraphInsertionLogic =
    new BlazegraphInsertionLogic(connection)

}
