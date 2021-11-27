package kz.mounty.spotify.gateway.domain.response

import kz.mounty.spotify.gateway.domain.SpotifyPlaylistItem

case class GetPlaylistTracksSpotifyResponse(items: Seq[SpotifyPlaylistItem])
