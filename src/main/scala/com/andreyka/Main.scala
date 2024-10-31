package com.andreyka

import com.andreyka.routes.{PrometheusPublisherMetrics, WebsocketSound}
import service.{RequestHandler, RoomService, SessionService, SoundService}
import zio.config.typesafe.TypesafeConfigProvider
import zio.http._
import zio.logging.consoleLogger
import zio.metrics.connectors.{MetricsConfig, prometheus}
import zio.metrics.jvm.DefaultJvmMetrics
import zio.{Config, ConfigProvider, Runtime, Task, ZIO, ZIOAppDefault, ZLayer, durationInt}

object Main extends ZIOAppDefault {

  override val bootstrap: ZLayer[Any, Config.Error, Unit] =
    Runtime.removeDefaultLoggers >>> Runtime.setConfigProvider(configProvider) >>> consoleLogger()
  private val configString: String =
    s"""
       |logger {
       |
       |  format = "%highlight{%timestamp{yyyy-MM-dd'T'HH:mm:ssZ} %fixed{7}{%level} [%fiberId] %name:%line %message %cause}"
       |
       |  filter {
       |    mappings {
       |      "zio.logging.example.LivePingService" = "DEBUG"
       |    }
       |  }
       |}
       |""".stripMargin
  private val configProvider: ConfigProvider = TypesafeConfigProvider.fromHoconString(configString)

  override def run: Task[Unit] = (for {
    _ <- ZIO.log("Starting server...")
    wsRoutes <- ZIO.serviceWith[WebsocketSound](_.route)
    prometheusMetrics <- ZIO.serviceWith[PrometheusPublisherMetrics](_.httpApp)
    _ <- ZIO.serviceWithZIO[RoomService](_.createDefaultRoom)
    _ <- Server.serve((
      wsRoutes.toRoutes ++ prometheusMetrics
      ).handleError(e => Response.internalServerError(e.toString)))
  } yield ()).provide(
    Server.default,
    RoomService.live,
    WebsocketSound.live,
    SoundService.live,
    SessionService.live,
    RequestHandler.live,
    DefaultJvmMetrics.live.unit,
    PrometheusPublisherMetrics.live,
    prometheus.prometheusLayer,
    prometheus.publisherLayer,
    ZLayer.succeed(MetricsConfig(500.millis))
  )
}
