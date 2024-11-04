package com.andreyka.service

import com.andreyka.model._
import zio.http.WebSocketChannel
import zio.metrics._
import zio.{Promise, Task, ZIO, ZLayer}

import java.util.UUID

case class RequestHandler(
                           roomService: RoomService,
                           soundService: SoundService
                         ) {

  def handle(request: In, isClosed: Promise[Throwable, Unit])(implicit webSocket: WebSocketChannel, userId: UUID): Task[Out] = request match {
    case AddToRoom(room) =>
      roomService.addListener(room.roomId, isClosed).as(Empty())
    case DeleteRoom(room) => roomService.deleteRoom(room).as(Empty()) <* roomsGauge.decrement
    case CreateRoom() => roomService.createRoom.map(room => RoomId(room.roomId)) <* roomsGauge.increment
    case RoomsList() => roomService.allRooms.map(RoomsListResponse)
    case Voice(soundFrame) => soundService.addNewVoice(soundFrame).as(Empty()) @@ voiceMetric
    case _ => ZIO.unit.as(Error("type not found"))
  }

  private def voiceMetric = Metric.counter("voice_messages").fromConst(1)

  private def roomsGauge = Metric.gauge("rooms_count")
}

object RequestHandler {
  val live = ZLayer.derive[RequestHandler]
}