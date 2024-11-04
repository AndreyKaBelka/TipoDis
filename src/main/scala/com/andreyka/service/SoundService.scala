package com.andreyka.service

import com.andreyka.model.SoundFrame
import zio.{Task, ZIO, ZLayer}


case class SoundService(roomService: RoomService) {

  def addNewVoice(soundFrame: SoundFrame): Task[Unit] = for {
    _ <- ZIO.log(s"New voice coming: room=${soundFrame.room} user=${soundFrame.userId}")
    messageHub <- roomService.findRoom(soundFrame.room.roomId).map(_.hub)
    _ <- messageHub
      .publish(soundFrame)
  } yield ()
}

object SoundService {
  val live = ZLayer {
    for {
      roomService <- ZIO.service[RoomService]
    } yield new SoundService(roomService)
  }
}
