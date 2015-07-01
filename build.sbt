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

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.5",
  "org.apache.commons" % "commons-email" % "1.2",
  "com.twilio.sdk" % "twilio-java-sdk" % "3.4.5"
)
