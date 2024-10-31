package service

import model.Codecs._
import model._
import zio.metrics._
import zio.{Task, ZIO, ZLayer}

case class RequestHandler(
                           roomService: RoomService,
                           sessionService: SessionService,
                           soundService: SoundService
                         ) {

  def handle(request: In): Task[Out] = request match {
    case AddToRoom(room, user) =>
      sessionService.getSession(user).flatMap(
        roomService.addParticipant(room, _)
      ).as(Empty())
    case DeleteRoom(room) => roomService.deleteRoom(room).as(Empty()) <* roomsGauge.decrement
    case CreateRoom() => roomService.createRoom.map(room => RoomId(room.roomId)) <* roomsGauge.increment
    case RoomsList() => roomService.allRooms.map(RoomsListResponse)
    case SessionsList() => sessionService.allSessions.map(SessionsListResponse)
    case Voice(soundFrame) => soundService.broadcast(soundFrame).as(Empty()) @@ voiceMetric
    case _ => ZIO.unit.as(Error("type not found"))
  }

  private def voiceMetric = Metric.counter("voice_messages").fromConst(1)

  private def roomsGauge = Metric.gauge("rooms_count")
}

object RequestHandler {
  val live = ZLayer.derive[RequestHandler]
}
