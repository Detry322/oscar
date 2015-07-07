name          := "oscar"

organization  := "com.anishathalye"

version       := "1.0"

scalaVersion  := "2.11.7"

scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfatal-warnings",
  "-encoding", "utf8"
)

libraryDependencies ++= {
  val http4sVersion = "0.8.0"
  Seq(
    "org.apache.httpcomponents" % "httpclient" % "4.5",
    "commons-io" % "commons-io" % "2.4",
    "org.apache.commons" % "commons-email" % "1.2",
    "com.twilio.sdk" % "twilio-java-sdk" % "3.4.5",
    "io.spray" %%  "spray-json" % "1.3.2",
    "org.http4s" %% "http4s-core" % http4sVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-blazeserver" % http4sVersion,
    "org.http4s" %% "http4s-blazeclient" % http4sVersion
  )
}

resolvers ++= Seq(
  "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases"
)
