package kz.mounty.spotify.gateway.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.Config
import kz.mounty.fm.amqp.messages.AMQPMessage
import kz.mounty.fm.amqp.messages.MountyMessages.{RoomCore, SpotifyGateway, UserProfileCore}
import kz.mounty.fm.domain.DomainEntity
import kz.mounty.fm.domain.commands._
import kz.mounty.fm.domain.requests._
import kz.mounty.fm.exceptions.{ErrorCodes, MountyException, ServerErrorRequestException}
import kz.mounty.fm.serializers.Serializers
import kz.mounty.spotify.gateway.domain.SpotifyUserProfile
import kz.mounty.spotify.gateway.domain.response._
import kz.mounty.spotify.gateway.services.{SpotifyPlayerService, SpotifyPlaylistService, SpotifyUserProfileService}
import kz.mounty.spotify.gateway.services.SpotifyPlaylistService.{GetCurrentUserPlaylists, GetPlaylistTracks}
import kz.mounty.spotify.gateway.utils.{LoggerActor, MountyEndpoint, SpotifyResponseConverter}
import org.json4s.jackson.JsonMethods.parse
import scredis.Redis
import org.json4s.jackson.Serialization.write

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object AmqpListenerActor {
  def props(redis: Redis)(implicit system: ActorSystem, ex: ExecutionContext, publisher: ActorRef, config: Config): Props =
    Props(new AmqpListenerActor(redis))
}

class AmqpListenerActor(redis: Redis)(implicit system: ActorSystem, ex: ExecutionContext, publisher: ActorRef, config: Config)
  extends Actor
    with LoggerActor
    with MountyEndpoint
    with SpotifyResponseConverter
    with Serializers {
  implicit val timeout: Timeout = 5.seconds

  override def receive: Receive = {
    case message: String =>
      log.info(s"received message $message")
      val amqpMessage = parse(message).extract[AMQPMessage]

      amqpMessage.routingKey match {
        case SpotifyGateway.GetCurrentUserRoomsGatewayRequest.routingKey =>
          val command = parse(amqpMessage.entity).extract[GetCurrentUserRoomsGatewayRequestBody]
          getTokenFromRedis(command.tokenKey).onComplete {
            case Success(token) =>
              (context.actorOf(SpotifyPlaylistService.props) ? GetCurrentUserPlaylists(
                accessToken = token
              )).map {
                case response: GetCurrentUserPlaylistsSpotifyResponse =>
                  handleSuccessfulResponse(convert(response), amqpMessage)
                case e: Throwable =>
                  handleException(e, Some(e.getMessage), amqpMessage)
                case any =>
                  handleUnknownResponse(any, amqpMessage)
              } recover {
                case e: Throwable =>
                  handleException(e, Some(e.getMessage), amqpMessage)
              }
            case Failure(exception) =>
              handleException(exception, Some("access key is missing"), amqpMessage)
          }
        case SpotifyGateway.GetPlaylistTracksGatewayRequest.routingKey =>
          val command = parse(amqpMessage.entity).extract[GetPlaylistTracksGatewayRequestBody]
          getTokenFromRedis(command.tokenKey).onComplete {
            case Success(token) =>
              (context.actorOf(SpotifyPlaylistService.props) ? GetPlaylistTracks(
                playlistId = command.playlistId,
                accessToken = token
              )).map {
                case response: GetPlaylistTracksSpotifyResponse =>
                  handleSuccessfulResponse(convert(response), amqpMessage)
                case e: Throwable =>
                  handleException(e, Some(e.getMessage), amqpMessage)
                case any =>
                  handleUnknownResponse(any, amqpMessage)
              } recover {
                case e: Throwable =>
                  handleException(e, Some(e.getMessage), amqpMessage)
              }
            case Failure(exception) =>
              handleException(exception, Some("access key is missing"), amqpMessage)
          }
        case SpotifyGateway.PlayerPlayGatewayCommand.routingKey =>
          val command = parse(amqpMessage.entity).extract[PlayerPlayGatewayCommandBody]
          getTokenFromRedis(command.tokenKey).onComplete {
            case Success(token) =>
              val entity = if (command.contextUri.isDefined && command.offset.isDefined) {
                Some(SpotifyPlayerService.PlayerPlayCommandBody(
                  context_uri = command.contextUri.get,
                  offset = SpotifyPlayerService.Offset(
                    position = command.offset.get
                  )))
              } else None
              (context.actorOf(SpotifyPlayerService.props) ? SpotifyPlayerService.PlayerPlay(
                deviceId = command.deviceId,
                entity = entity,
                accessToken = token
              )).map {
                case response: PlayerPlayGatewayResponseBody =>
                  handleSuccessfulResponse(response, amqpMessage)
                case e: Throwable =>
                  handleException(e, Some(e.getMessage), amqpMessage)
                case any =>
                  handleUnknownResponse(any, amqpMessage)
              } recover {
                case e: Throwable =>
                  handleException(e, Some(e.getMessage), amqpMessage)
              }
            case Failure(exception) =>
              handleException(exception, Some("access key is missing"), amqpMessage)
          }
        case SpotifyGateway.PlayerPauseGatewayCommand.routingKey =>
          val command = parse(amqpMessage.entity).extract[PlayerPauseGatewayCommandBody]
          getTokenFromRedis(command.tokenKey).onComplete {
            case Success(token) =>
              (context.actorOf(SpotifyPlayerService.props) ? SpotifyPlayerService.PlayerPause(
                deviceId = command.deviceId,
                accessToken = token
              )).map {
                case response: PlayerPauseGatewayResponseBody =>
                  handleSuccessfulResponse(response, amqpMessage)
                case e: Throwable =>
                  handleException(e, Some(e.getMessage), amqpMessage)
                case any =>
                  handleUnknownResponse(any, amqpMessage)
              } recover {
                case e: Throwable =>
                  handleException(e, Some(e.getMessage), amqpMessage)
              }
            case Failure(exception) =>
              handleException(exception, Some("access key is missing"), amqpMessage)
          }
        case SpotifyGateway.PlayerNextGatewayCommand.routingKey =>
          val command = parse(amqpMessage.entity).extract[PlayerNextGatewayCommandBody]
          getTokenFromRedis(command.tokenKey).onComplete {
            case Success(token) =>
              (context.actorOf(SpotifyPlayerService.props) ? SpotifyPlayerService.PlayerNext(
                deviceId = command.deviceId,
                accessToken = token
              )).map {
                case response: PlayerNextGatewayResponseBody =>
                  handleSuccessfulResponse(response, amqpMessage)
                case e: Throwable =>
                  handleException(e, Some(e.getMessage), amqpMessage)
                case any =>
                  handleUnknownResponse(any, amqpMessage)
              } recover {
                case e: Throwable =>
                  handleException(e, Some(e.getMessage), amqpMessage)
              }
            case Failure(exception) =>
              handleException(exception, Some("access key is missing"), amqpMessage)
          }
        case SpotifyGateway.PlayerPrevGatewayCommand.routingKey =>
          val command = parse(amqpMessage.entity).extract[PlayerPrevGatewayCommandBody]
          getTokenFromRedis(command.tokenKey).onComplete {
            case Success(token) =>
              (context.actorOf(SpotifyPlayerService.props) ? SpotifyPlayerService.PlayerPrev(
                deviceId = command.deviceId,
                accessToken = token
              )).map {
                case response: PlayerPrevGatewayResponseBody =>
                  handleSuccessfulResponse(response, amqpMessage)
                case e: Throwable =>
                  handleException(e, Some(e.getMessage), amqpMessage)
                case any =>
                  handleUnknownResponse(any, amqpMessage)
              } recover {
                case e: Throwable =>
                  handleException(e, Some(e.getMessage), amqpMessage)
              }
            case Failure(exception) =>
              handleException(exception, Some("access key is missing"), amqpMessage)
          }
        case SpotifyGateway.GetCurrentlyPlayingTrackGatewayRequest.routingKey =>
          val command = parse(amqpMessage.entity).extract[GetCurrentlyPlayingTrackGatewayRequestBody]
          getTokenFromRedis(command.tokenKey).onComplete {
            case Success(token) =>
              (context.actorOf(SpotifyPlayerService.props) ? SpotifyPlayerService.PlayerGetCurrentlyPlaying(
                accessToken = token
              )).map {
                case response: GetCurrentlyPlayingTrackSpotifyResponse =>
                  handleSuccessfulResponse(convert(response), amqpMessage)
                case e: Throwable =>
                  handleException(e, Some(e.getMessage), amqpMessage)
                case any =>
                  handleUnknownResponse(any, amqpMessage)
              } recover {
                case e: Throwable =>
                  handleException(e, Some(e.getMessage), amqpMessage)
              }
            case Failure(exception) =>
              handleException(exception, Some("access key is missing"), amqpMessage)
          }
        case SpotifyGateway.GetUserProfileGatewayRequest.routingKey =>
          val command = parse(amqpMessage.entity).extract[GetUserProfileGatewayRequestBody]
          println("GOT REQUEST: " + command)
          getTokenFromRedis(command.tokenKey).onComplete {
            case Success(token) =>
              (context.actorOf(SpotifyUserProfileService.props) ? SpotifyUserProfileService.GetCurrentUserProfile(
                accessToken = token
              )).map {
                case response: SpotifyUserProfile =>
                  handleSuccessfulResponse(convert(response), amqpMessage)
                case e: Throwable =>
                  handleException(e, Some(e.getMessage), amqpMessage)
                case any =>
                  handleUnknownResponse(any, amqpMessage)
              } recover {
                case e: Throwable =>
                  handleException(e, Some(e.getMessage), amqpMessage)
              }
            case Failure(exception) =>
              handleException(exception, Some("access key is missing"), amqpMessage)
          }
        case _ =>
          log.info("something else")
      }
  }

  def getTokenFromRedis(tokenKey: String): Future[String] = {
    redis.get(tokenKey).map { res =>
      res.get
    } recover {
      case e: Throwable =>
        throw e
    }
  }

  def handleSuccessfulResponse(response: DomainEntity,
                               amqpMessage: AMQPMessage): Unit = {
    publisher ! amqpMessage.copy(entity = write(response), routingKey = getResponseRoutingKey(amqpMessage.routingKey), exchange = "X:mounty-spotify-gateway-out")
  }

  def handleException(exception: Throwable,
                      exceptionMessage: Option[String],
                      amqpMessage: AMQPMessage): Unit = {
    writeErrorLog(s"Received exception: $exceptionMessage", exception)

    val exceptionInfo = exception match {
      case e: MountyException =>
        e.getExceptionInfo
      case _ =>
        ServerErrorRequestException(
          ErrorCodes.INTERNAL_SERVER_ERROR(
            errorSeries
          ),
          exceptionMessage
        ).getExceptionInfo
    }

    publisher ! amqpMessage.copy(entity = write(exceptionInfo), routingKey = getResponseRoutingKey(amqpMessage.routingKey), exchange = "X:mounty-spotify-gateway-out")
  }

  def handleUnknownResponse(response: Any, amqpMessage: AMQPMessage): Unit = {
    val exception = ServerErrorRequestException(
      ErrorCodes.INTERNAL_SERVER_ERROR(
        errorSeries
      ),
      Some("unknown response")
    )
    handleException(exception, Some(s"received unhandled response: $response"), amqpMessage)
  }

  def getResponseRoutingKey(requestRoutingKey: String): String = {
    requestRoutingKey match {
      case SpotifyGateway.GetCurrentUserRoomsGatewayRequest.routingKey => RoomCore.GetCurrentUserRoomsGatewayResponse.routingKey
      case SpotifyGateway.GetPlaylistTracksGatewayRequest.routingKey => RoomCore.GetPlaylistTracksGatewayResponse.routingKey
      case SpotifyGateway.GetUserProfileGatewayRequest.routingKey => UserProfileCore.GetUserProfileGatewayResponse.routingKey
      case SpotifyGateway.PlayerPlayGatewayCommand.routingKey => RoomCore.PlayerPlayGatewayResponse.routingKey
      case SpotifyGateway.PlayerPauseGatewayCommand.routingKey => RoomCore.PlayerPauseGatewayResponse.routingKey
      case SpotifyGateway.PlayerNextGatewayCommand.routingKey => RoomCore.PlayerNextGatewayResponse.routingKey
      case SpotifyGateway.PlayerPrevGatewayCommand.routingKey => RoomCore.PlayerPrevGatewayResponse.routingKey
      case SpotifyGateway.GetCurrentlyPlayingTrackGatewayRequest.routingKey => RoomCore.GetCurrentlyPlayingTrackGatewayResponse.routingKey
      case _ => "unknown routing key"
    }
  }
}