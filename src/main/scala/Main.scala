import routes.WebsocketSound
import service.{RequestHandler, RoomService, SessionService, SoundService}
import zio.config.typesafe.TypesafeConfigProvider
import zio.http._
import zio.logging.consoleLogger
import zio.{Config, ConfigProvider, LogAnnotation, Runtime, Task, ZIO, ZIOAppDefault, ZLayer}

object Main extends ZIOAppDefault {

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

  override val bootstrap: ZLayer[Any, Config.Error, Unit] =
    Runtime.removeDefaultLoggers >>> Runtime.setConfigProvider(configProvider) >>> consoleLogger()

  override def run: Task[Unit] = (for {
    _ <- ZIO.log("Starting server...")
    wsRoutes <- ZIO.serviceWith[WebsocketSound](_.route)
    _ <- Server.serve(Routes(wsRoutes).handleError(e => Response.internalServerError(e.toString)))
  } yield ()).provide(
    Server.default,
    RoomService.live,
    WebsocketSound.live,
    SoundService.live,
    SessionService.live,
    RequestHandler.live
  )
}
