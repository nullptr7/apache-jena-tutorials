package com.github.nullptr7.streamer

import cats.effect.*
import fs2.*
import fs2.io.file.{ Files, Flags, Path }

final class TripleInsertionService(csvPath: Path, logic: InsertLogic) {
  private def parseLine(line: String): Option[Triple] =
    line.split(",", 3) match
      case Array(s, p, o) => Some(Triple(s, p, o))
      case _              => None

  private val header = "s,p,o"

  private val executeStream: IO[Unit] =
    Files[IO]
      .readAll(csvPath, chunkSize = 64 * 1064, Flags.Read)
      .through(text.utf8.decode)
      .through(text.lines)
      .filter(_.nonEmpty)
      .dropWhile(_ == header) // skip header
      .map(parseLine)
      .unNone // drop malformed rows
      .chunkN(10_0000, allowFewer = true) // tune to your batch size
      .evalMap(logic.insert)
      .compile
      .drain

  val doExecute: IO[Unit] =
    for {
      _ <- IO.println("Starting execution")
      _ <- executeStream
      _ <- IO.println("Execution completed")
    } yield ()

}
