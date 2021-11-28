package kz.mounty.spotify.gateway.domain

case class SpotifyUserProfile(id: String,
                              country: String,
                              displayName: String,
                              email: String,
                              images: Seq[Image],
                              `type`: String,
                              uri: String)
