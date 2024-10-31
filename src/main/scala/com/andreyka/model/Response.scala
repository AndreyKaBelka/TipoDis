package model

import model.Codecs._
import zio.json.{SnakeCase, jsonDerive, jsonDiscriminator, jsonHintNames}

import java.util.UUID

@jsonDerive
@jsonDiscriminator("$type")
@jsonHintNames(SnakeCase)
sealed trait Out

case class SessionsListResponse(sessions: Set[User]) extends Out

case class RoomsListResponse(rooms: Set[Room]) extends Out

case class RoomId(room: UUID) extends Out

case class UserId(user: UUID) extends Out

case class Empty() extends Out

case class Error(err: String) extends Out
