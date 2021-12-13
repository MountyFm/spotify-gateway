package kz.mounty.spotify.gateway.domain.response

import kz.mounty.fm.domain.DomainEntity
import kz.mounty.spotify.gateway.domain.SpotifyTrack

case class GetCurrentlyPlayingTrackSpotifyResponse(item: SpotifyTrack, progressMs: Int) extends DomainEntity
