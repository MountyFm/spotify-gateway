package kz.mounty.spotify.gateway.services

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.Config
import kz.mounty.spotify.gateway.domain.response.GetPlaylistTracksSpotifyResponse
import kz.mounty.spotify.gateway.services.SpotifyTrackService.GetPlaylistTracks
import kz.mounty.spotify.gateway.utils.{LoggerActor, RestClient, SpotifyUrlGetter}

import scala.concurrent.ExecutionContext

object SpotifyTrackService {
  def props(implicit timeout: Timeout,
            config: Config,
            system: ActorSystem,
            executionContext: ExecutionContext): Props = Props(new SpotifyTrackService)

  sealed trait TrackServiceCommand extends ServiceCommand

  case class GetPlaylistTracks(playlistId: String, accessToken: String) extends TrackServiceCommand
}

class SpotifyTrackService(implicit timeout: Timeout,
                          config: Config,
                          system: ActorSystem,
                          executionContext: ExecutionContext) extends LoggerActor with SpotifyUrlGetter with RestClient {
  override def receive: Receive = {
    case command: GetPlaylistTracks =>
      val senderRef = sender()
      makeGetRequest[GetPlaylistTracksSpotifyResponse](
        uri = getUrl(command),
        headers = getAuthorizationHeaders(command.accessToken))
        .map { response =>
          senderRef ! response
        } recover {
        case e: Throwable =>
          senderRef ! e
      }
  }
}
