package model

import model.Codecs._
import zio.json.{SnakeCase, jsonDerive, jsonDiscriminator, jsonHintNames}

@jsonDiscriminator("$type")
@jsonHintNames(SnakeCase)
@jsonDerive
sealed trait In

case class CreateRoom() extends In

case class DeleteRoom(room: Room) extends In

case class AddToRoom(room: Room, user: User) extends In

case class Voice(soundFrame: SoundFrame) extends In

case class RoomsList() extends In

case class SessionsList() extends In

