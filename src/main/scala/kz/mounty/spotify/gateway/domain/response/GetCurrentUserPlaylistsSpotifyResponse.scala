package kz.mounty.spotify.gateway.domain.response

import kz.mounty.spotify.gateway.domain.SpotifyPlaylist

case class GetCurrentUserPlaylistsSpotifyResponse(items: Seq[SpotifyPlaylist])
