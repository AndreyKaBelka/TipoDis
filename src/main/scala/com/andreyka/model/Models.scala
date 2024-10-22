package model

import model.Codecs._
import zio.http.WebSocketChannel
import zio.json.jsonDerive

import java.util.UUID

case class Session(socket: WebSocketChannel, user: User)

@jsonDerive
case class User(userId: UUID)

@jsonDerive
case class SoundFrame(user: User, room: Room, sound: Array[Byte])

case class Room(roomId: UUID, sessions: Set[Session])