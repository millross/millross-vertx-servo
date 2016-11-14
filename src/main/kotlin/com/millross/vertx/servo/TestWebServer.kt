package com.millross.vertx.servo

import com.netflix.servo.annotations.DataSourceType.COUNTER
import com.netflix.servo.annotations.Monitor
import com.netflix.servo.monitor.Monitors
import com.netflix.servo.publish.*
import com.netflix.servo.publish.graphite.GraphiteMetricObserver
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import java.util.concurrent.TimeUnit
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

        // Set up a graphite observer
        val prefix = "servo"
        val addr = "localhost:2003"
        val observer = GraphiteMetricObserver(prefix, addr)
        PollScheduler.getInstance().start()
        schedule(MonitorRegistryMetricPoller(), observer)

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

    fun schedule(poller: MetricPoller, observer: MetricObserver) {
        val task = PollRunnable(poller, BasicMetricFilter.MATCH_ALL, observer)
        PollScheduler.getInstance().addPoller(task, 10, TimeUnit.SECONDS)
    }
}