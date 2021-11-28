package kz.mounty.spotify.gateway.utils

import kz.mounty.fm.domain.playlist.Playlist
import kz.mounty.fm.domain.requests._
import kz.mounty.fm.domain.track.Track
import kz.mounty.fm.domain.user.UserProfile
import kz.mounty.spotify.gateway.domain.SpotifyUserProfile
import kz.mounty.spotify.gateway.domain.response._
import org.joda.time.DateTime

trait SpotifyResponseConverter {
  def convert(response: GetCurrentUserPlaylistsSpotifyResponse): GetCurrentUserPlaylistsResponseBody = {
    val playlists = response.items.map {
      spotifyPlaylistItem =>
        Playlist(
          id = spotifyPlaylistItem.id,
          name = spotifyPlaylistItem.name,
          imageUrl = spotifyPlaylistItem.images.head.url,
          tracksTotalAmount = spotifyPlaylistItem.tracks.total,
          isPublic = spotifyPlaylistItem.public,
          spotifyUri = spotifyPlaylistItem.uri
        )
    }
    GetCurrentUserPlaylistsResponseBody(
      playlists = playlists
    )
  }

  def convert(response: GetPlaylistTracksSpotifyResponse): GetPlaylistTracksResponseBody = {
    val tracks = response.items.map {
      spotifyTrackItem =>
        Track(
          id = spotifyTrackItem.track.id,
          imageUrl = spotifyTrackItem.track.album.images.head.url,
          artists = spotifyTrackItem.track.artists.map(_.name),
          name = spotifyTrackItem.track.name,
          duration = spotifyTrackItem.track.durationMs
        )
    }
    GetPlaylistTracksResponseBody(
      tracks = tracks
    )
  }

  def convert(response: SpotifyUserProfile): GetUserProfileGatewayResponseBody = {
    GetUserProfileGatewayResponseBody(
      userProfile = UserProfile(
        id = response.id,
        name = response.displayName,
        email = response.email,
        avatarUrl = response.images.headOption.map(_.url),
        spotifyUri = response.uri,
        createdAt = DateTime.now
      )
    )
  }

  def convert(response: GetCurrentlyPlayingTrackSpotifyResponse): GetCurrentlyPlayingTrackResponseBody = {
    GetCurrentlyPlayingTrackResponseBody(
      track = Track(
        id = response.item.id,
        imageUrl = response.item.album.images.head.url,
        artists = response.item.artists.map(_.name),
        name = response.item.name,
        duration = response.item.durationMs
      )
    )
  }
}
