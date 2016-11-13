package com.millross.vertx.servo

import com.netflix.servo.annotations.DataSourceType.COUNTER
import com.netflix.servo.annotations.Monitor
import com.netflix.servo.monitor.Monitors
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import java.util.concurrent.atomic.AtomicInteger

/**
 * Simple test webserver verticle to try and collect some trivial metrics via servo
 */
class TestWebServer : AbstractVerticle() {

    // Servo monitoring of the invocation count
    @Monitor(name="invocation.count", type=COUNTER)
    private val invocationCount = AtomicInteger(0)

    override fun start(startFuture: Future<Void>?) {
        super.start(startFuture)
        val router = Router.router(vertx)
        router.get("/testMetric").handler { handleMetricTest(it) }
        router.get("/").handler { index(it) }
        Monitors.registerObject("TestWebServer", this)
        vertx.createHttpServer()
                .requestHandler { router.accept(it) }
                .listen(8080)
    }

    fun handleMetricTest(rc: RoutingContext) {
        invocationCount.incrementAndGet()
        rc.response().end("Metric triggered")
    }

    fun index(rc: RoutingContext) {
        rc.response().sendFile("webroot/index.html")
    }
}