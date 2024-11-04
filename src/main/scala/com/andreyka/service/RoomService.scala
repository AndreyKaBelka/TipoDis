package com.andreyka.service

import com.andreyka.model.{Room, SoundFrame}
import com.andreyka.service.RoomService.CantAddParticipant
import zio.http.ChannelEvent.Read
import zio.http.{WebSocketChannel, WebSocketFrame}
import zio.json.EncoderOps
import zio.stream.ZStream
import zio.{Hub, Promise, Ref, Task, ZIO, ZLayer}

import java.util.UUID

case class RoomService(
                        rooms: Ref[Set[Room]]
                      ) {

  def createRoom: Task[Room] = for {
    uuid <- ZIO.succeed(UUID.randomUUID())
    hub <- Hub.bounded[SoundFrame](1024)
    room = Room(uuid, hub)
    _ <- rooms.update(_ + room)
  } yield room

  def createDefaultRoom: Task[Unit] = for {
    uuid <- ZIO.succeed(UUID.fromString("00000000-0000-0000-0000-000000000000"))
    hub <- Hub.bounded[SoundFrame](1024)
    room = Room(uuid, hub)
    _ <- rooms.update(_ + room)
  } yield ()

  def addListener(roomId: UUID, isClosed: Promise[Throwable, Unit])(implicit channel: WebSocketChannel, userId: UUID): Task[Unit] = for {
    _ <- ZIO.log("Adding new subscriber")
    room <- findRoom(roomId)
    messageHub = room.hub
    stream = ZStream.fromHub(messageHub)
    _ <- stream.interruptWhen(isClosed.await).foreach(
      sf => channel.send(Read(WebSocketFrame.Text(sf.toJson))).when(userId != sf.userId)
    ).forkDaemon
    _ <- ZIO.log(s"Added listener for room ${room.roomId}")
  } yield ()

  def findRoom(roomId: UUID): Task[Room] = {
    rooms.get.map(_.find(_.roomId == roomId)).someOrFail(CantAddParticipant(roomId))
  }

  def allRooms: Task[Set[Room]] = for {
    rooms <- rooms.get
  } yield rooms

  def deleteRoom(room: Room): Task[Unit] = for {
    _ <- ZIO.log(s"Deleting room: ${room.roomId}")
    _ <- rooms.update(_.filterNot(_.roomId == room.roomId))
  } yield ()
}

object RoomService {
  val live = ZLayer {
    for {
      ref <- Ref.make(Set.empty[Room])
    } yield RoomService(ref)
  }

  private case class CantAddParticipant(roomId: UUID) extends RuntimeException
}
