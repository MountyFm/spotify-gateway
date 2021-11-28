package kz.mounty.spotify.gateway.utils

import kz.mounty.spotify.gateway.domain._
import kz.mounty.spotify.gateway.domain.response._
import kz.mounty.spotify.gateway.services.SpotifyPlayerService.{Offset, PlayerPlayCommandBody}
import org.json4s.ShortTypeHints
import org.json4s.jackson.Serialization

trait LocalSerializer {
  implicit val formats = Serialization.formats(
    ShortTypeHints(
      List(
        classOf[GetCurrentUserPlaylistsSpotifyResponse],
        classOf[SpotifyUserProfile],
        classOf[GetPlaylistTracksSpotifyResponse],
        classOf[Album],
        classOf[Artist],
        classOf[Image],
        classOf[SpotifyPlaylist],
        classOf[SpotifyPlaylistItem],
        classOf[SpotifyTrack],
        classOf[SpotifyTracks],
        classOf[Offset],
        classOf[PlayerPlayCommandBody],
        classOf[GetCurrentlyPlayingTrackSpotifyResponse],
      )
    )
  )
}
