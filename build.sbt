name := "ccloader"

version := "1.0"

scalaVersion := "2.11.5"

val phantomVersion = "1.5.0"

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Websudos releases" at "http://maven.websudos.co.uk/ext-release-local",
  "Sonatype Repository" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3",
  "com.websudos" %% "phantom-dsl" % phantomVersion,
  "com.websudos" %% "phantom-zookeeper" % phantomVersion,
  "org.specs2" % "specs2_2.11" % "3.0-M3",
  "org.specs2" % "specs2-core_2.11" % "3.0-M3"
)

scalacOptions in Test ++= Seq("-Yrangepos")
