package kz.mounty.spotify.gateway.domain

import kz.mounty.fm.domain.DomainEntity

case class SpotifyPlaylist(id: String,
                           name: String,
                           images: Seq[Image],
                           tracks: SpotifyTracks,
                           public: Boolean,
                           uri: String) extends DomainEntity
