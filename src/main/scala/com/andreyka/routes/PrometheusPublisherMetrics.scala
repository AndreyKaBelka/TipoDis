package com.andreyka.routes

import zio.{Clock, ZIO, ZLayer}
import zio.http.{Method, Response, Routes, handler}
import zio.metrics.Metric
import zio.metrics.connectors.prometheus.PrometheusPublisher

class PrometheusPublisherMetrics {

  def memoryUsage: ZIO[Any, Nothing, Double] = {
    import java.lang.Runtime._
    ZIO
      .succeed(getRuntime.totalMemory() - getRuntime.freeMemory())
      .map(_ / (1024.0 * 1024.0)) @@ Metric.gauge("memory_usage")
  }

  val httpApp =
    Routes(
      Method.GET / "metrics" ->
        handler(ZIO.serviceWithZIO[PrometheusPublisher](_.get.map(Response.text))),
      Method.GET / "foo" -> handler {
        for {
          _    <- memoryUsage
          time <- Clock.currentDateTime
        } yield Response.text(s"$time\t/foo API called")
      }
    )

}

object PrometheusPublisherMetrics {
  val live = ZLayer.derive[PrometheusPublisherMetrics]
}
