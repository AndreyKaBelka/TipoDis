package com.andreyka.routes

import model._
import service.{RequestHandler, RoomService, SessionService}
import zio.http.ChannelEvent._
import zio.http.Status.NotImplemented
import zio.http.{Handler, Method, Response, Route, WebSocketApp, WebSocketChannel, WebSocketFrame, handler}
import zio.json.{DecoderOps, EncoderOps}
import zio.metrics.Metric
import zio.{Cause, Task, UIO, ZIO, ZLayer}

import java.util.UUID

case class WebsocketSound(requestHandler: RequestHandler, sessionService: SessionService, roomService: RoomService) {

  val newSessionCount = Metric.gauge("new_session_count")

  private val socketApp: WebSocketApp[Any] = Handler.webSocket { implicit channel =>
    channel.receiveAll {
      case UserEventTriggered(UserEvent.HandshakeComplete) =>
        val userId = UUID.randomUUID()
        sessionService.addSession(User(userId), channel).tapBoth(
          err => ZIO.logErrorCause("Some error: ", Cause.die(err)) *> channel.shutdown,
          _ => ZIO.log(s"Added new session: $userId") *> send(UserId(userId))
        ) *> roomService.addParticipant(
          Room(UUID.fromString("00000000-0000-0000-0000-000000000000"), Set.empty),
          Session(channel, User(userId))
        ) <* newSessionCount.increment

      case Read(WebSocketFrame.Text(data)) =>
        data.fromJson[In].fold(
          err => send(Error(err)),
          suc => requestHandler.handle(suc).flatMap(send)
        )

      case Unregistered =>
        ZIO.log(s"Removing session") *> sessionService.removeSession(channel) *> newSessionCount.decrement

      case Read(WebSocketFrame.Close(status, reason)) => ZIO.succeed(NotImplemented)

      case ExceptionCaught(cause) => ZIO.logError(s"Channel error!: $cause")

      case _ => ZIO.unit
    }.catchAll { err =>
      ZIO.logError(s"Some error occurred: $err")
    }
  }

  private def send(data: Out)(implicit channel: WebSocketChannel): Task[Unit] = {
    channel.send(Read(WebSocketFrame.Text(data.toJson)))
  }


  val route: Route[Any, Response] = Method.GET / "ws" -> handler(socketApp.toResponse)
}

object WebsocketSound {
  val live = ZLayer.derive[WebsocketSound]
}