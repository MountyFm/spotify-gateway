package kz.mounty.spotify.gateway

import Boot.{config, system}
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import kz.mounty.fm.amqp.RabbitMQConnection

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{FiniteDuration, SECONDS}
import scala.util.{Failure, Success}

object Boot extends App {
  implicit val config: Config = ConfigFactory.load()
  implicit val system: ActorSystem = ActorSystem()
  implicit val mat = Materializer(system)
  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(FiniteDuration(config.getLong("request-timeout"), SECONDS))

  val rmqHost = config.getString("rabbitmq.host")
  val rmqPort = config.getInt("rabbitmq.port")
  val username = config.getString("rabbitmq.username")
  val password = config.getString("rabbitmq.password")
  val virtualHost = config.getString("rabbitmq.virtualHost")


  val connection = RabbitMQConnection.rabbitMQConnection(
    username,
    password,
    rmqHost,
    rmqPort,
    virtualHost
  )

  val channel = connection.createChannel()

  RabbitMQConnection.declareExchange(
    channel,
    "X:mounty-spotify-gateway-in",
    "topic"
  ) match {
    case Success(value) => system.log.info("succesfully declared exchange")
    case Failure(exception) => system.log.warning(s"couldn't declare exchange ${exception.getMessage}")
  }

  RabbitMQConnection.declareAndBindQueue(
    channel,
    "Q:mounty-spotify-gateway-queue",
    "X:mounty-spotify-gateway-in",
    "mounty-messages.spotify-gateway.#"
  )
}
