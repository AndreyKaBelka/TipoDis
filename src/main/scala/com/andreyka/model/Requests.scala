package com.andreyka.model

import zio.json.{SnakeCase, jsonDerive, jsonDiscriminator, jsonHintNames}
import com.andreyka.model.Codecs._

@jsonDiscriminator("$type")
@jsonHintNames(SnakeCase)
@jsonDerive
sealed trait In

case class CreateRoom() extends In

case class DeleteRoom(room: Room) extends In

case class AddToRoom(room: Room) extends In

case class Voice(soundFrame: SoundFrame) extends In

case class RoomsList() extends In

