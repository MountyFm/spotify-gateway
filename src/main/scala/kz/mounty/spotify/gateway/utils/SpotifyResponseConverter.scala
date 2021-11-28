package kz.mounty.spotify.gateway.utils

import kz.mounty.fm.domain.playlist.Playlist
import kz.mounty.fm.domain.requests.{GetCurrentUserPlaylistsResponseBody, GetCurrentlyPlayingTrackResponseBody}
import kz.mounty.fm.domain.track.Track
import kz.mounty.spotify.gateway.domain.response.{GetCurrentUserPlaylistsSpotifyResponse, GetCurrentlyPlayingTrackSpotifyResponse}

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
