package kz.mounty.spotify.gateway.utils

import kz.mounty.fm.domain.requests._
import kz.mounty.fm.domain.room.{Room, RoomStatus}
import kz.mounty.fm.domain.track.Track
import kz.mounty.fm.domain.user.UserProfile
import kz.mounty.spotify.gateway.domain.SpotifyUserProfile
import kz.mounty.spotify.gateway.domain.response._
import org.joda.time.DateTime

trait SpotifyResponseConverter {
  def convert(response: GetCurrentUserPlaylistsSpotifyResponse, userId: String): GetCurrentUserRoomsGatewayResponseBody = {
    val rooms = response.items.map {
      spotifyPlaylistItem =>
        Room(
          id = spotifyPlaylistItem.id,
          title = spotifyPlaylistItem.name,
          genreIds = None,
          status = RoomStatus.ACTIVE,
          isPrivate = !spotifyPlaylistItem.public,
          imageUrl = spotifyPlaylistItem.images.head.url,
          inviteCode = None,
          spotifyUri = spotifyPlaylistItem.uri,
          createdAt = DateTime.now
        )
    }
    GetCurrentUserRoomsGatewayResponseBody(
      userId = userId,
      rooms = rooms
    )
  }

  def convert(roomId: String, response: GetPlaylistTracksSpotifyResponse): GetPlaylistTracksGatewayResponseBody = {
    val tracks = response.items.map {
      spotifyTrackItem =>
        Track(
          id = spotifyTrackItem.track.id,
          imageUrl = spotifyTrackItem.track.album.images.head.url,
          artists = spotifyTrackItem.track.artists.map(_.name),
          name = spotifyTrackItem.track.name,
          duration = spotifyTrackItem.track.durationMs,
          spotifyUri = spotifyTrackItem.track.uri
        )
    }
    GetPlaylistTracksGatewayResponseBody(
      roomId = roomId,
      tracks = tracks)
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

  def convert(response: GetCurrentlyPlayingTrackSpotifyResponse): GetCurrentlyPlayingTrackGatewayResponseBody = {
    GetCurrentlyPlayingTrackGatewayResponseBody(
      track = Track(
        id = response.item.id,
        imageUrl = response.item.album.images.head.url,
        artists = response.item.artists.map(_.name),
        name = response.item.name,
        duration = response.item.durationMs,
        spotifyUri = response.item.uri
      ),
      progressMs = response.progressMs
    )
  }
}
