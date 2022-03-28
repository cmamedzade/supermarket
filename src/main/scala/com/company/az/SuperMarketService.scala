package com.company.az

import akka.Done
import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import com.supermarket.az.SuperMarketServiceProtoHandler
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import sys.process._
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success}

object SuperMarketService {

  def main(args: Array[String]): Unit = {

    "docker restart upbeat_dijkstra" !  // the name of the container may change

    Thread.sleep(10000)

    val config = ConfigFactory
      .parseString("akka.http.server.preview.enable-http2 = on")
      .withFallback(ConfigFactory.defaultApplication())
    val system = ActorSystem(SuperMarketGuardianActor(),"SuperMarket", config)  // starts typed actor system

    new SuperMarketService(system).run()

    CoordinatedShutdown(system).addTask(      // graceful shutdown mechanism
      CoordinatedShutdown.PhaseServiceStop,
      "service-stop"
    ){ () =>
      system.terminate()
      Future.successful("docker stop upbeat_dijkstra" ! ).map(_ => Done)
    }
  }
}

class SuperMarketService(system: ActorSystem[_]) {

  implicit val sys = system
  implicit val ex = system.executionContext

  def run(): Future[Http.ServerBinding] = {

    val service: HttpRequest => Future[HttpResponse] = {
      SuperMarketServiceProtoHandler(new SuperMarketServiceImpl)
    }

    val bound = Http(system)
      .newServerAt("127.0.0.1", 8080)   // starts new service with given ip and port
      .bind(service) // HttpRequest => Future[Response]
      .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10 seconds))

    bound.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        println("gRPC server bound to {}:{}", address.getHostString, address.getPort)
      case Failure(ex) =>
        println("Failed to bind gRPC endpoint, terminating system", ex)
        system.terminate()
    }
    bound
  }
}
