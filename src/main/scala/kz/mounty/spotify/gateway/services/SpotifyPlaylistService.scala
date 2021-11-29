package kz.mounty.spotify.gateway.services

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.Config
import kz.mounty.spotify.gateway.domain.response.{GetCurrentUserPlaylistsSpotifyResponse, GetPlaylistTracksSpotifyResponse}
import kz.mounty.spotify.gateway.services.SpotifyPlaylistService.{GetCurrentUserPlaylists, GetPlaylistTracks}
import kz.mounty.spotify.gateway.utils.{LoggerActor, RestClient, SpotifyUrlGetter}

import scala.concurrent.ExecutionContext

object SpotifyPlaylistService {
  def props(implicit timeout: Timeout,
            config: Config,
            system: ActorSystem,
            publisher: ActorRef,
            executionContext: ExecutionContext): Props = Props(new SpotifyPlaylistService)

  sealed trait PlaylistServiceCommand extends ServiceCommand

  case class GetCurrentUserPlaylists(accessToken: String) extends PlaylistServiceCommand

  case class GetPlaylistTracks(playlistId: String, accessToken: String) extends PlaylistServiceCommand
}

class SpotifyPlaylistService(implicit timeout: Timeout,
                             config: Config,
                             system: ActorSystem,
                             executionContext: ExecutionContext) extends LoggerActor with SpotifyUrlGetter with RestClient {
  override def receive: Receive = {
    case command: GetCurrentUserPlaylists =>
      val senderRef = sender()
      makeGetRequest[GetCurrentUserPlaylistsSpotifyResponse](
        uri = getUrl(command),
        headers = getAuthorizationHeaders(command.accessToken))
        .map { response =>
          senderRef ! response
        } recover {
        case e: Throwable =>
          senderRef ! e
      }
      context.stop(self)

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
      context.stop(self)
  }
}
