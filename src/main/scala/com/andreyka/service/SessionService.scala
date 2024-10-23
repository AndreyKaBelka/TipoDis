package service

import model.{Session, User}
import service.SessionService.CantFindSession
import zio.http.WebSocketChannel
import zio.{Ref, Task, ZIO, ZLayer}

import java.util.UUID

case class SessionService(private val sessions: Ref[Set[Session]]) {
  def addSession(user: User, session: WebSocketChannel): Task[Unit] = for {
    _ <- sessions.update(_ + Session(session, user))
  } yield ()

  def removeSession(socket: WebSocketChannel): Task[Unit] = for {
    _ <- sessions.updateAndGet(_.filterNot(_.socket == socket))
  } yield ()

  def getSession(user: User): Task[Session] = for {
    session <- sessions.get.map(_.find(_.user.userId == user.userId)).someOrFail(CantFindSession(user.userId))
    _ <- ZIO.log(s"Got session ${session.user.userId}")
  } yield session

  def allSessions: Task[Set[User]] = for {
    _ <- sessions.get.tap(cur => ZIO.log(s"Getting all sessions: ${cur.size}"))
    users <- sessions.get.map(_.map(_.user))
  } yield users
}

object SessionService {
  val live = ZLayer {
    for {
      set <- Ref.make(Set.empty[Session])
      service = SessionService(set)
    } yield service
  }

  private case class CantFindSession(userId: UUID) extends RuntimeException
}
