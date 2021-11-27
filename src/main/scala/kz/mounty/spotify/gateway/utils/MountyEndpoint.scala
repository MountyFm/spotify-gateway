package kz.mounty.spotify.gateway.utils

import kz.mounty.fm.exceptions.ErrorSeries

trait MountyEndpoint {
  implicit val errorSeries: ErrorSeries = ErrorSeries.SPOTIFY_GATEWAY
}
