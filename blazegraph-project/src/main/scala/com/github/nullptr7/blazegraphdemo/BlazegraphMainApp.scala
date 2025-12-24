package com.github.nullptr7.blazegraphdemo

import cats.effect.{ IO, IOApp }
import com.bigdata.journal.Options
import com.bigdata.rdf.sail.{ BigdataSail, BigdataSailRepository }
import com.github.nullptr7.streamer.TripleInsertionService
import fs2.io.file.Path

import java.util
import scala.concurrent.duration.{ Duration, DurationInt }

object BlazegraphMainApp extends IOApp.Simple {
  val properties = new util.Properties

  properties.put(Options.BUFFER_MODE, "DiskRW")
  properties.put(Options.FILE, "D:\\softwares\\blazegraph\\database.jnl")

  override def run: IO[Unit] =
    for {
      repo  <- IO.blocking(new BigdataSailRepository(new BigdataSail(properties)))
      conn  <- IO.blocking { repo.initialize(); repo.getConnection }
      logic <- IO.blocking(BlazegraphInsertionLogic(conn))
      _     <- IO.println("Starting execution")
//      _     <- IO.sleep(10.seconds)
      serv  <- IO.pure(TripleInsertionService(Path("C:\\Users\\User\\Desktop\\pp\\triples_10m.csv"), logic))
      _     <- serv.doExecute
      _     <- IO.delay(conn.close())
    } yield ()

}
