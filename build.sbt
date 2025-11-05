ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.7"

lazy val jenaVersion    = "5.6.0"
lazy val logbackVersion = "1.5.20"

lazy val root = (project in file("."))
  .settings(
    name := "apache-jena-tutorials"
  )
  .aggregate(jenaProject, blazegraphProject)

lazy val jenaProject =
  (project in file("jena-project"))
    .settings(
      name := "jena-projects",
      libraryDependencies ++= Seq(
        "org.apache.jena" % "jena-core"        % jenaVersion,
        "org.apache.jena" % "jena-fuseki-main" % jenaVersion,
        "ch.qos.logback"  % "logback-classic"  % logbackVersion,
      ),
    )

lazy val blazegraphProject =
  (project in file("blazegraph-project"))
    .settings(
      name := "blazegraph-project",
      libraryDependencies ++= Seq("com.blazegraph" % "bigdata-core" % "2.1.4"),
    )
