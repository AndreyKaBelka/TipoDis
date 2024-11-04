package com.andreyka.model

import zio.Hub
import zio.json.jsonDerive
import com.andreyka.model.Codecs._

import java.util.UUID

@jsonDerive
case class SoundFrame(room: Room, sound: Array[Float], userId: UUID)

case class Room(roomId: UUID, hub: Hub[SoundFrame])