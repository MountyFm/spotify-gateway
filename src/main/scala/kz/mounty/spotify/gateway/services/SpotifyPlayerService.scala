package kz.mounty.spotify.gateway.services

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.Config
import kz.mounty.fm.domain.spotify.gateway.requests.ChangePlayerStateResponseBody
import kz.mounty.spotify.gateway.services.SpotifyPlayerService.{Next, Pause, Play, PlayerCommand, Prev}
import kz.mounty.spotify.gateway.utils.{LoggerActor, RestClient}

import scala.concurrent.ExecutionContext

object SpotifyPlayerService {
  def props(implicit timeout: Timeout,
            config: Config,
            system: ActorSystem,
            executionContext: ExecutionContext): Props = Props(new SpotifyPlayerService)

  trait PlayerCommand {
    def deviceId: Option[String]
    def accessToken: String
  }

  case class Play(deviceId: Option[String], accessToken: String) extends PlayerCommand

  case class Pause(deviceId: Option[String], accessToken: String) extends PlayerCommand

  case class Next(deviceId: Option[String], accessToken: String) extends PlayerCommand

  case class Prev(deviceId: Option[String], accessToken: String) extends PlayerCommand
}

class SpotifyPlayerService(implicit timeout: Timeout,
                           config: Config,
                           system: ActorSystem,
                           executionContext: ExecutionContext) extends LoggerActor with RestClient {
  override def receive: Receive = {
    case command: Play =>
      val senderRef = sender()
      makePutRequest[ChangePlayerStateResponseBody](
        uri = getUrl(command),
        headers = getAuthorizationHeaders(command.accessToken),
        body = null
      )
        .map { response =>
          senderRef ! response
        } recover {
        case e: Throwable =>
          senderRef ! e
      }
    case command: Pause =>
      val senderRef = sender()
      makePutRequest[ChangePlayerStateResponseBody](
        uri = getUrl(command),
        headers = getAuthorizationHeaders(command.accessToken),
        body = null
      )
        .map { response =>
          senderRef ! response
        } recover {
        case e: Throwable =>
          senderRef ! e
      }
    case command: Next =>
      val senderRef = sender()
      makePutRequest[ChangePlayerStateResponseBody](
        uri = getUrl(command),
        headers = getAuthorizationHeaders(command.accessToken),
        body = null
      )
        .map { response =>
          senderRef ! response
        } recover {
        case e: Throwable =>
          senderRef ! e
      }
    case command: Prev =>
      val senderRef = sender()
      makePutRequest[ChangePlayerStateResponseBody](
        uri = getUrl(command),
        headers = getAuthorizationHeaders(command.accessToken),
        body = null
      )
        .map { response =>
          senderRef ! response
        } recover {
        case e: Throwable =>
          senderRef ! e
      }
  }

  def getUrl(command: PlayerCommand): String = {
    val deviceIdStr = if (command.deviceId.isDefined) command.deviceId.get else ""
    val commandStr = command match {
      case _: Play => "play"
      case _: Pause => "pause"
      case _: Next => "next"
      case _: Prev => "previous"
    }
    config.getString("spotify-api-endpoints.player")
      .replace("@@player_command@@", commandStr)
      .replace("@@device_id@@", deviceIdStr)
  }
}
