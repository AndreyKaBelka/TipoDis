package com.andreyka.routes

import service.SessionService
import zio.http.{Method, Response, Routes, handler}
import zio.metrics.Metric
import zio.metrics.connectors.prometheus.PrometheusPublisher
import zio.{Schedule, ZIO, ZLayer, durationInt}

case class PrometheusPublisherMetrics() {
  val httpApp: Routes[PrometheusPublisher, Nothing] =
    Routes(
      Method.GET / "metrics" ->
        handler(ZIO.serviceWithZIO[PrometheusPublisher](_.get.map(Response.text)))
    )

}

object PrometheusPublisherMetrics {
  val live = ZLayer.derive[PrometheusPublisherMetrics]
}
