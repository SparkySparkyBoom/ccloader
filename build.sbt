name := "ccloader"

version := "1.0"

scalaVersion := "2.11.5"

val phantomVersion = "1.5.0"

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Websudos releases" at "http://maven.websudos.co.uk/ext-release-local"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3",
  "com.websudos" %% "phantom-dsl" % phantomVersion,
  "com.websudos" %% "phantom-zookeeper" % phantomVersion
)