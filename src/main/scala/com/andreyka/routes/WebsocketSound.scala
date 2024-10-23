package routes

import model._
import service.{RequestHandler, SessionService}
import zio.http.ChannelEvent.{ExceptionCaught, Read, Unregistered, UserEvent, UserEventTriggered}
import zio.http.WebSocketFrame.Close
import zio.http.{Handler, Method, Request, Response, Route, WebSocketApp, WebSocketChannel, WebSocketFrame, handler}
import zio.json.{DecoderOps, EncoderOps}
import zio.{Cause, Task, ZIO, ZLayer}

import java.util.UUID

case class WebsocketSound(requestHandler: RequestHandler, sessionService: SessionService) {

  val socketApp: WebSocketApp[Any] = Handler.webSocket { implicit channel =>
    channel.receiveAll {
      case UserEventTriggered(UserEvent.HandshakeComplete) =>
        val userId = UUID.randomUUID()
        sessionService.addSession(User(userId), channel).tapBoth(
          err => ZIO.logErrorCause("Some error: ", Cause.die(err)) *> channel.shutdown,
          _ => ZIO.log(s"Added new session: $userId") *> send(UserId(userId))
        )

      case Read(WebSocketFrame.Text(data)) =>
        data.fromJson[In].fold(
          err => send(Error(err)),
          suc => requestHandler.handle(suc).flatMap(send)
        )

      case Unregistered =>
        ZIO.log(s"Removing session") *> sessionService.removeSession(channel)

      case ExceptionCaught(cause) => ZIO.logError(s"Channel error!: ${cause.getMessage}")

      case _ => ZIO.unit
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