package com.andreyka.routes

import zio.http.Header.AccessControlAllowOrigin
import zio.http.Middleware.{CorsConfig, cors}
import zio.http.{Method, Response, Routes, handler}
import zio.metrics.connectors.prometheus.PrometheusPublisher
import zio.{ZIO, ZLayer}

case class PrometheusPublisherMetrics() {
  val config: CorsConfig = CorsConfig(
    allowedOrigin = _ => Some(AccessControlAllowOrigin.All)
  )

  val httpApp: Routes[PrometheusPublisher, Nothing] =
    Routes(
      Method.GET / "metrics" ->
        handler(ZIO.serviceWithZIO[PrometheusPublisher](_.get.map(Response.text)))
    ) @@ cors(config)

}

object PrometheusPublisherMetrics {
  val live = ZLayer.derive[PrometheusPublisherMetrics]
}
