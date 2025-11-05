ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.7"

lazy val root = (project in file("."))
  .settings(
    name := "apache-jena-tutorials"
  )

lazy val jenaVersion    = "5.6.0"
lazy val logbackVersion = "1.5.20"

libraryDependencies ++= Seq(
  "org.apache.jena" % "jena-core"        % jenaVersion,
  "org.apache.jena" % "jena-fuseki-main" % jenaVersion,
  "ch.qos.logback"  % "logback-classic"  % logbackVersion,
)
