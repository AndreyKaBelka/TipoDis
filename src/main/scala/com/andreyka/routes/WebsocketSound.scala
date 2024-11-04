package com.andreyka.routes

import com.andreyka.model._
import com.andreyka.service.{RequestHandler, RoomService, SoundService}
import zio.http.ChannelEvent._
import zio.http.{Handler, Method, Response, Route, WebSocketApp, WebSocketChannel, WebSocketFrame, handler}
import zio.json.{DecoderOps, EncoderOps}
import zio.metrics.Metric
import zio.{Promise, Task, ZIO, ZLayer}

import java.util.UUID

case class WebsocketSound(
                           requestHandler: RequestHandler,
                           roomService: RoomService,
                           soundService: SoundService
                         ) {

  val route: Route[Any, Response] = Method.GET / "ws" -> handler(socketApp.toResponse)
  private val newSessionCount = Metric.gauge("new_session_count")
  private val socketApp: WebSocketApp[Any] = Handler.webSocket { implicit channel =>
    implicit val userId: UUID = UUID.randomUUID()
    val isClosed = Promise.make[Throwable, Unit]
    isClosed.flatMap(isClosed => channel.receiveAll {
      case UserEventTriggered(UserEvent.HandshakeComplete) =>
        ZIO.collectAllDiscard(Seq(
          roomService.addListener(UUID.fromString("00000000-0000-0000-0000-000000000000"), isClosed),
          newSessionCount.increment,
          send(UserId(userId))
        ))

      case Read(WebSocketFrame.Text(data)) =>
        val json = data.fromJson[In]

        ZIO.fromEither(json).foldZIO(
          err => send(Error(err)),
          suc => requestHandler.handle(suc, isClosed).flatMap(send)
        )

      case Unregistered =>
        ZIO.log(s"Removing session") *> isClosed.succeed() *> newSessionCount.decrement

      case ExceptionCaught(cause) => ZIO.logError(s"Channel error!: $cause") *> isClosed.fail(cause)

      case _ => ZIO.unit
    }.catchAll { err =>
      ZIO.logError(s"Some error occurred: $err")
    })
  }

  private def send(data: Out)(implicit channel: WebSocketChannel): Task[Unit] = {
    channel.send(Read(WebSocketFrame.Text(data.toJson)))
  }
}

object WebsocketSound {
  val live = ZLayer.derive[WebsocketSound]
}