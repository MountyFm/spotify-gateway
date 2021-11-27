package kz.mounty.spotify.gateway.domain

case class SpotifyTrack(id: String,
                        album: Album,
                        artists: Seq[Artist],
                        name: String,
                        `type`: String,
                        durationMs: Int)
