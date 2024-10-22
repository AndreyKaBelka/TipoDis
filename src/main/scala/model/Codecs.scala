package model

import zio.json._

import java.util.UUID

object Codecs {
  implicit val roomDecoder: JsonDecoder[Room] = {
    JsonDecoder[UUID].map(Room(_, Set.empty))
  }

  implicit val roomEncoder: JsonEncoder[Room] = {
    JsonEncoder[(UUID, Set[UUID])].contramap(
      (room: Room) =>
        room.roomId -> room.sessions.map(_.user.userId)
    )
  }

  implicit val userEncoder: JsonEncoder[User] = {
    JsonEncoder[UUID].contramap((user: User) => user.userId)
  }

  implicit val userDecoder: JsonDecoder[User] = {
    JsonDecoder[UUID].map(User)
  }
}
