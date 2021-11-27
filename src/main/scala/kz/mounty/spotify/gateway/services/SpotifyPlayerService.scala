package kz.mounty.spotify.gateway.services

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.Config
import kz.mounty.fm.domain.spotify.gateway.requests.{ChangePlayerStateResponseBody, GetPlayListTracksResponseBody}
import kz.mounty.spotify.gateway.services.SpotifyPlayerService.{Next, Pause, Play, Prev}
import kz.mounty.spotify.gateway.utils.{LoggerActor, RestClient}

import scala.concurrent.ExecutionContext

object SpotifyPlayerService {
  def props(implicit timeout: Timeout,
            config: Config,
            system: ActorSystem,
            executionContext: ExecutionContext): Props = Props(new SpotifyPlayerService)

  case class Play(deviceId: Option[String], accessToken: String)

  case class Pause(deviceId: Option[String], accessToken: String)

  case class Next(deviceId: Option[String], accessToken: String)

  case class Prev(deviceId: Option[String], accessToken: String)
}

class SpotifyPlayerService(implicit timeout: Timeout,
                           config: Config,
                           system: ActorSystem,
                           executionContext: ExecutionContext) extends LoggerActor with RestClient {
  override def receive: Receive = {
    case command: Play =>
      val url = config.getString("spotify-api-endpoints.player").replace("@@player_command@@", "play")
      val senderRef = sender()
      makePutRequest[ChangePlayerStateResponseBody](
        uri = url,
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
      val url = config.getString("spotify-api-endpoints.player").replace("@@player_command@@", "pause")
      val senderRef = sender()
      makePutRequest[ChangePlayerStateResponseBody](
        uri = url,
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
      val url = config.getString("spotify-api-endpoints.player").replace("@@player_command@@", "next")
      val senderRef = sender()
      makePutRequest[ChangePlayerStateResponseBody](
        uri = url,
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
      val url = config.getString("spotify-api-endpoints.player").replace("@@player_command@@", "prev")
      val senderRef = sender()
      makePutRequest[ChangePlayerStateResponseBody](
        uri = url,
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
}
