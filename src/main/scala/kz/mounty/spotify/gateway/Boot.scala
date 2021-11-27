package kz.mounty.spotify.gateway

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{FiniteDuration, SECONDS}

object Boot extends App {
  implicit val config: Config = ConfigFactory.load()
  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(FiniteDuration(config.getLong("request-timeout"), SECONDS))

}
