package kz.mounty.spotify.gateway.domain.response

import kz.mounty.spotify.gateway.domain.Image

case class GetCurrentUserProfileSpotifyResponse(country: String,
                                           displayName: String,
                                           email: String,
                                           images: Seq[Image],
                                           `type`: String,
                                           uri: String)
