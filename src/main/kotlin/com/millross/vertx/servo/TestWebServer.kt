package com.millross.vertx.servo

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

/**
 * Simple test webserver verticle to try and collect some trivial metrics via servo
 */
class TestWebServer : AbstractVerticle() {

    override fun start(startFuture: Future<Void>?) {
        super.start(startFuture)
        val router = Router.router(vertx)
        router.get("/testMetric").handler { handleMetricTest(it) }
        router.get("/").handler { index(it) }
        vertx.createHttpServer()
                .requestHandler { router.accept(it) }
                .listen(8080)
    }

    fun handleMetricTest(rc: RoutingContext) {
        rc.response().end("Metric triggered")
    }

    fun index(rc: RoutingContext) {
        rc.response().sendFile("webroot/index.html")
    }
}