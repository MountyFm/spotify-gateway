package kz.mounty.spotify.gateway.services

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.Config
import kz.mounty.fm.domain.commands.ChangePlayerStateResponseBody
import kz.mounty.spotify.gateway.services.SpotifyPlayerService._
import kz.mounty.spotify.gateway.utils.{LocalSerializer, LoggerActor, RestClient, SpotifyUrlGetter}
import org.json4s.jackson.Serialization.write

import scala.concurrent.ExecutionContext

object SpotifyPlayerService {
  def props(implicit timeout: Timeout,
            config: Config,
            system: ActorSystem,
            executionContext: ExecutionContext): Props = Props(new SpotifyPlayerService)

  sealed trait PlayerCommand extends ServiceCommand {
    def deviceId: Option[String]

    def accessToken: String
  }

  case class PlayerPlayCommandBody(context_uri: String, offset: Offset, position_ms: Int = 0)

  case class Offset(position: Int)

  case class PlayerPlay(deviceId: Option[String], entity: PlayerPlayCommandBody, accessToken: String) extends PlayerCommand

  case class PlayerPause(deviceId: Option[String], accessToken: String) extends PlayerCommand

  case class PlayerNext(deviceId: Option[String], accessToken: String) extends PlayerCommand

  case class PlayerPrev(deviceId: Option[String], accessToken: String) extends PlayerCommand

}

class SpotifyPlayerService(implicit timeout: Timeout,
                           config: Config,
                           system: ActorSystem,
                           executionContext: ExecutionContext) extends LoggerActor
  with LocalSerializer
  with SpotifyUrlGetter
  with RestClient {
  override def receive: Receive = {
    case command: PlayerPlay =>
      val senderRef = sender()
      makePutRequest[ChangePlayerStateResponseBody](
        uri = getUrl(command),
        headers = getAuthorizationHeaders(command.accessToken),
        body = Some(write(command.entity))
      )
        .map { response =>
          senderRef ! response
        } recover {
        case e: Throwable =>
          senderRef ! e
      }
    case command: PlayerPause =>
      val senderRef = sender()
      makePutRequest[ChangePlayerStateResponseBody](
        uri = getUrl(command),
        headers = getAuthorizationHeaders(command.accessToken),
        body = None
      )
        .map { response =>
          senderRef ! response
        } recover {
        case e: Throwable =>
          senderRef ! e
      }
    case command: PlayerNext =>
      val senderRef = sender()
      makePostRequest[ChangePlayerStateResponseBody](
        uri = getUrl(command),
        headers = getAuthorizationHeaders(command.accessToken),
        body = None
      )
        .map { response =>
          senderRef ! response
        } recover {
        case e: Throwable =>
          senderRef ! e
      }
    case command: PlayerPrev =>
      val senderRef = sender()
      makePostRequest[ChangePlayerStateResponseBody](
        uri = getUrl(command),
        headers = getAuthorizationHeaders(command.accessToken),
        body = None
      )
        .map { response =>
          senderRef ! response
        } recover {
        case e: Throwable =>
          senderRef ! e
      }
  }
}
