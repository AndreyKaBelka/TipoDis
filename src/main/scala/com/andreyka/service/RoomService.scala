package service

import model.{Room, Session}
import service.RoomService._
import zio.{Ref, Task, ZIO, ZLayer}

import java.util.UUID

case class RoomService(
                        rooms: Ref[Set[Room]]
                      ) {

  def createRoom: Task[Room] = for {
    uuid <- ZIO.succeed(UUID.randomUUID())
    sessions = Set.empty[Session]
    room = Room(uuid, sessions)
    _ <- rooms.update(_ + room)
  } yield room

  def createDefaultRoom: Task[Unit] = for {
    uuid <- ZIO.succeed(UUID.fromString("00000000-0000-0000-0000-000000000000"))
    sessions = Set.empty[Session]
    room = Room(uuid, sessions)
    _ <- rooms.update(_ + room)
  } yield ()

  def addParticipant(roomId: Room, session: Session): Task[Unit] = for {
    room <- findRoom(roomId)
    newSessions = room.sessions.filterNot(_.user.userId == session.user.userId) + session
    _ <- ZIO.log(s"Updating room ${room.roomId} with user ${session.user.userId}, newSessions: $newSessions")
    _ <- replaceRoom(Room(room.roomId, newSessions))
  } yield ()

  def findRoom(room: Room): Task[Room] = {
    rooms.get.map(_.find(_.roomId == room.roomId)).someOrFail(CantAddParticipant(room.roomId))
  }

  private def replaceRoom(room: Room): Task[Unit] = {
    rooms.update(oldRooms => oldRooms.filterNot(_.roomId == room.roomId) + room)
  }

  def removeParticipant(roomId: Room, session: Session): Task[Unit] = for {
    room <- findRoom(roomId)
    updatedSessions = room.sessions.filterNot(_.user.userId == session.user.userId)
    newRoom = Room(room.roomId, updatedSessions)
    _ = replaceRoom(newRoom)
  } yield ()

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
