package com.github.nullptr7.streamer

import cats.effect.IO
import fs2.Chunk

trait InsertLogic {
  def insert(triples: Chunk[Triple]): IO[Unit]

}
