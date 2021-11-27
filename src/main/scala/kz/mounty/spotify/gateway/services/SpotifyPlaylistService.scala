package kz.mounty.spotify.gateway.services

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.Config
import kz.mounty.fm.domain.spotify.gateway.requests.GetCurrentUserPlaylistsResponseBody
import kz.mounty.spotify.gateway.services.SpotifyPlaylistService.GetCurrentUserPlaylist
import kz.mounty.spotify.gateway.utils.{LoggerActor, RestClient}

import scala.concurrent.ExecutionContext

object SpotifyPlaylistService {
  def props(implicit timeout: Timeout,
            config: Config,
            system: ActorSystem,
            executionContext: ExecutionContext): Props = Props(new SpotifyPlaylistService)

  case class GetCurrentUserPlaylist(accessToken: String)
}

class SpotifyPlaylistService(implicit timeout: Timeout,
                             config: Config,
                             system: ActorSystem,
                             executionContext: ExecutionContext) extends LoggerActor with RestClient {
  override def receive: Receive = {
    case command: GetCurrentUserPlaylist =>
      val url = config.getString("spotify-api-endpoints.current_user_playlist")
      val senderRef = sender()
      makeGetRequest[GetCurrentUserPlaylistsResponseBody](
        uri = url,
        headers = getAuthorizationHeaders(command.accessToken))
        .map { response =>
          senderRef ! response
        } recover {
        case e: Throwable =>
          senderRef ! e
      }
  }
}
