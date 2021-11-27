package kz.mounty.spotify.gateway.domain

case class SpotifyTracks(items: Seq[SpotifyTrack] = Seq.empty,
                         total: Int,
                         limit: Option[Int],
                         offset: Option[Int])
