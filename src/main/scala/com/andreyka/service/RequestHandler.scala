package service

import model.Codecs._
import model._
import zio.{Task, ZIO, ZLayer}

case class RequestHandler(
                           roomService: RoomService,
                           sessionService: SessionService,
                           soundService: SoundService
                         ) {

  def handle(request: In): Task[Out] = request match {
    case AddToRoom(room, user) =>
      ZIO.log("Ганджоны здесяяяяяя") *> sessionService.getSession(user).flatMap(
        roomService.addParticipant(room, _)
      ).as(Empty())
    case DeleteRoom(room) => roomService.deleteRoom(room).as(Empty())
    case CreateRoom() => roomService.createRoom.map(room => RoomId(room.roomId))
    case RoomsList() => roomService.allRooms.map(RoomsListResponse)
    case SessionsList() => sessionService.allSessions.map(SessionsListResponse)
    case Voice(soundFrame) => soundService.broadcast(soundFrame).as(Empty())
    case _ => ZIO.unit.as(Empty())
  }
}

object RequestHandler {
  val live = ZLayer.derive[RequestHandler]
}
