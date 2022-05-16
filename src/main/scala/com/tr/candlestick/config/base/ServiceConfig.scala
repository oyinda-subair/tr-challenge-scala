package com.tr.candlestick.config.base

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import akka.stream.Materializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{cors, corsRejectionHandler}
import ch.megard.akka.http.cors.scaladsl.model.{HttpHeaderRange, HttpOriginMatcher}
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import com.tr.candlestick.ServerMain.system
import com.tr.candlestick.config.errorHandler.ErrorHandler._
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport

import scala.concurrent.ExecutionContext
import scala.io.StdIn

trait ServiceConfig extends App with PlayJsonSupport {

  implicit val system: ActorSystem = ActorSystem("messaging-system")
  implicit val materializer: Materializer = Materializer(system)
  implicit val executionContext: ExecutionContext = system.dispatcher

  val setting: CorsSettings = CorsSettings.default
    .withAllowedOrigins(HttpOriginMatcher.*)
    .withAllowCredentials(true)
    .withAllowedHeaders(HttpHeaderRange.*)
    .withAllowedMethods(scala.collection.immutable.Seq(POST, PUT, GET, OPTIONS, DELETE))
    .withExposedHeaders(
      scala.collection.immutable.Seq(
        "Content-Type",
        "X-Content-Type",
        "Origin",
        "X-Requested-With",
        "Set_Cookie"
      )
    )

  val handleErrors: Directive0 = handleRejections(rejectionHandler) & handleExceptions(myExceptionHandler)

  def corsSupport(settings: CorsSettings = setting): Directive0 = handleRejections(corsRejectionHandler).tflatMap { _ => cors(settings) }

  val routes: Route

  lazy val allRoutes:Route = {
    corsSupport() {
      handleErrors {
        routes
      }
    }
  }

  def startService(): Unit = {
    val bindingFuture = Http().newServerAt("localhost", 9000).bind(allRoutes)
    println("Server running...")

    // kill the server with input
    println(s"Server now online. Please navigate to http://localhost:9000/hello\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
    println("Server is shut down")
  }
}
