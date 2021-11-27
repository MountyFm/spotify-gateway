package kz.mounty.spotify.gateway.services

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.Config
import kz.mounty.fm.domain.spotify.gateway.requests.GetPlayListTracksResponseBody
import kz.mounty.spotify.gateway.services.SpotifyTrackService.GetPlaylistTracks
import kz.mounty.spotify.gateway.utils.{LoggerActor, RestClient}

import scala.concurrent.ExecutionContext

object SpotifyTrackService {
  def props(implicit timeout: Timeout,
            config: Config,
            system: ActorSystem,
            executionContext: ExecutionContext): Props = Props(new SpotifyTrackService)

  case class GetPlaylistTracks(playlistId: String, accessToken: String)
}

class SpotifyTrackService(implicit timeout: Timeout,
                          config: Config,
                          system: ActorSystem,
                          executionContext: ExecutionContext) extends LoggerActor with RestClient {
  override def receive: Receive = {
    case command: GetPlaylistTracks =>
      val url = config.getString("spotify-api-endpoints.playlist_tracks").replace("@@playlist_id@@", command.playlistId)
      val senderRef = sender()
      makeGetRequest[GetPlayListTracksResponseBody](
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
