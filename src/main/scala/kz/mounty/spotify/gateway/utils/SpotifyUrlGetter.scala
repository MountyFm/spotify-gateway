package kz.mounty.spotify.gateway.utils

import com.typesafe.config.Config
import kz.mounty.spotify.gateway.services.SpotifyPlayerService._
import kz.mounty.spotify.gateway.services.SpotifyPlaylistService.GetCurrentUserPlaylists
import kz.mounty.spotify.gateway.services.SpotifyTrackService.GetPlaylistTracks
import kz.mounty.spotify.gateway.services.SpotifyUserProfileService.GetCurrentUserProfile
import kz.mounty.spotify.gateway.services._

trait SpotifyUrlGetter {
  def getUrl(command: ServiceCommand)(implicit config: Config): String = {
    command match {
      case cmd: PlayerPlay =>
        if(cmd.deviceId.isDefined)
          s"${config.getString("spotify-api-base-url")}/me/player/play?device_id=${cmd.deviceId.get}"
        else
          s"${config.getString("spotify-api-base-url")}/me/player/play"
      case cmd: PlayerPause =>
        if(cmd.deviceId.isDefined)
          s"${config.getString("spotify-api-base-url")}/me/player/pause?device_id=$cmd.deviceId.get"
        else
          s"${config.getString("spotify-api-base-url")}/me/player/pause"
      case cmd: PlayerNext =>
        if(cmd.deviceId.isDefined)
          s"${config.getString("spotify-api-base-url")}/me/player/next?device_id=$cmd.deviceId.get"
        else
          s"${config.getString("spotify-api-base-url")}/me/player/next"
      case cmd: PlayerPrev =>
        if(cmd.deviceId.isDefined)
          s"${config.getString("spotify-api-base-url")}/me/player/previous?device_id=$cmd.deviceId.get"
        else
          s"${config.getString("spotify-api-base-url")}/me/player/previous"
      case _: GetCurrentUserPlaylists =>
        s"${config.getString("spotify-api-base-url")}/me/playlists"
      case cmd: GetPlaylistTracks =>
        s"${config.getString("spotify-api-base-url")}/playlists/${cmd.playlistId}/tracks"
      case _: GetCurrentUserProfile =>
        s"${config.getString("spotify-api-base-url")}/me"
    }
  }
}
