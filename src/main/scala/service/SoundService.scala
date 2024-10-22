package service

import model.{Room, SoundFrame, User}
import zio.http.ChannelEvent.Read
import zio.http.WebSocketFrame
import zio.json.EncoderOps
import zio.{Task, ZIO, ZLayer}

case class SoundService(roomService: RoomService) {

  def broadcast(soundFrame: SoundFrame): Task[Unit] = for {
    room <- roomService.findRoom(soundFrame.room)
    sessions = room.sessions.filterNot(_.user.userId == soundFrame.user.userId)
    _ <- ZIO.foreach(sessions)(session => session.socket.send(
      Read(WebSocketFrame.Text(soundFrame.toJson))
    ))
  } yield ()
}

object SoundService {
  val live = ZLayer.derive[SoundService]
}
