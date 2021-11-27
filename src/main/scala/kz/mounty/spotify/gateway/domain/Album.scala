package kz.mounty.spotify.gateway.domain

case class Album(albumType: String,
                 artists: Seq[Artist],
                 name: String,
                 images: Seq[Image])
