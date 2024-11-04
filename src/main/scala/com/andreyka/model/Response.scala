package com.andreyka.model

import zio.json.{SnakeCase, jsonDerive, jsonDiscriminator, jsonHintNames}

import java.util.UUID
import com.andreyka.model.Codecs._

@jsonDerive
@jsonDiscriminator("$type")
@jsonHintNames(SnakeCase)
sealed trait Out

case class RoomsListResponse(rooms: Set[Room]) extends Out

case class RoomId(room: UUID) extends Out

case class Empty() extends Out

case class Error(err: String) extends Out

case class UserId(userId: UUID) extends Out
