package service

import model.SoundFrame
import zio.http.ChannelEvent.Read
import zio.http.WebSocketFrame
import zio.json.EncoderOps
import zio.{Task, ZIO, ZLayer}

case class SoundService(roomService: RoomService) {

  def broadcast(soundFrame: SoundFrame): Task[Unit] = for {
    _ <- ZIO.log(s"Broadcasting message: room ${soundFrame.room}, user: ${soundFrame.user}")
    room <- roomService.findRoom(soundFrame.room)
    sessions = room.sessions.filterNot(_.user.userId == soundFrame.user.userId)
    errors <- ZIO.partitionPar(sessions)(session => session.socket.send(
      Read(WebSocketFrame.Text(soundFrame.toJson))
    ))
    _ <- ZIO.log(s"Some errors occurred: ${errors._1.size}, firstError: ${errors._1.headOption}")
      .when(errors._1.nonEmpty)
  } yield ()
}

object SoundService {
  val live = ZLayer.derive[SoundService]
}
