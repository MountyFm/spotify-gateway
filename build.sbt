
name := "spotify-gateway"

version := "0.1"

scalaVersion := "2.12.12"

val sVersion = "2.12.12"
val akkaVersion = "2.6.17"
val jsonVersion = "4.0.2"
val xtractVersion = "2.0.0"

credentials += Credentials("Artifactory Realm", "mounty.jfrog.io", "sansyzbayevdaniyar3@gmail.com", "AKCp8k8iXkJUazq2J2CAa5uT4XvrDwf9Y9uzWsLuGcoq5C1pYix9DaP2CGsAUjgvH4mReFuoJ")

resolvers +=
  "Artifactory" at "https://mounty.jfrog.io/artifactory/mounty-domain-sbt-release-local"

resolvers += Resolver.bintrayRepo("akka", "snapshots")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "de.heikoseeberger" %% "akka-http-json4s" % "1.38.2",
  "com.typesafe.akka" %% "akka-http" % "10.2.6",
  "ch.qos.logback" % "logback-classic" % "1.2.7",
  "org.json4s" %% "json4s-native" % jsonVersion,
  "org.json4s" %% "json4s-jackson" % jsonVersion,
  "com.lucidchart" %% "xtract" % xtractVersion,
  "com.lucidchart" %% "xtract-testing" % xtractVersion % "test",
  "com.typesafe.play" %% "play-json" % "2.9.2",
  "org.scala-lang.modules" %% "scala-xml" % "1.1.0",

  "kz.mounty"         %% "mounty-domain"    % "0.1.1",
  "org.scalaj"        %% "scalaj-http"      % "2.4.2",
  "com.rabbitmq" % "amqp-client" % "5.14.0"
)