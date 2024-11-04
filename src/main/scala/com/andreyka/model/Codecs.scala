package com.andreyka.model

import zio.json._

import java.util.UUID

object Codecs {
  implicit val roomDecoder: JsonDecoder[Room] = {
    JsonDecoder[UUID].map(Room(_, null))
  }

  implicit val roomEncoder: JsonEncoder[Room] = {
    JsonEncoder[UUID].contramap(
      (room: Room) =>
        room.roomId
    )
  }
}
