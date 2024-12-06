
name := "PomodoroBot"

version := "0.1"

scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.6.20",
  "com.bot4s" %% "telegram-core" % "5.7.0",
  "com.typesafe.akka" %% "akka-slf4j" % "2.6.20",
  "ch.qos.logback" % "logback-classic" % "1.2.11",
  "io.circe" %% "circe-core" % "0.14.5",
  "io.circe" %% "circe-parser" % "0.14.5",
  "com.typesafe.akka" %% "akka-stream" % "2.6.20"
)
