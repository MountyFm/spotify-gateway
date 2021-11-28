package kz.mounty.spotify.gateway.services

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.Config
import kz.mounty.spotify.gateway.domain.response.GetCurrentUserProfileSpotifyResponse
import kz.mounty.spotify.gateway.services.SpotifyUserProfileService.GetCurrentUserProfile
import kz.mounty.spotify.gateway.utils.{LoggerActor, RestClient, SpotifyUrlGetter}

import scala.concurrent.ExecutionContext

object SpotifyUserProfileService {
  def props(implicit timeout: Timeout,
            config: Config,
            system: ActorSystem,
            executionContext: ExecutionContext): Props = Props(new SpotifyUserProfileService)

  sealed trait UserProfileServiceCommand extends ServiceCommand

  case class GetCurrentUserProfile(accessToken: String) extends UserProfileServiceCommand

}

class SpotifyUserProfileService(implicit timeout: Timeout,
                                config: Config,
                                system: ActorSystem,
                                executionContext: ExecutionContext) extends LoggerActor with SpotifyUrlGetter with RestClient {
  override def receive: Receive = {
    case command: GetCurrentUserProfile =>
      val senderRef = sender()
      makeGetRequest[GetCurrentUserProfileSpotifyResponse](
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
