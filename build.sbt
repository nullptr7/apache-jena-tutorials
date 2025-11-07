ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.7"

lazy val jenaVersion    = "5.6.0"
lazy val logbackVersion = "1.5.20"

lazy val root = (project in file("."))
  .settings(
    name := "apache-jena-tutorials"
  )
  .aggregate(jenaProject, blazegraphProject, streamer)

lazy val jenaProject =
  (project in file("jena-project"))
    .settings(
      name := "jena-projects",
      libraryDependencies ++= Seq(
        "org.apache.jena" % "jena-core"        % jenaVersion,
        "org.apache.jena" % "jena-fuseki-main" % jenaVersion,
        "ch.qos.logback"  % "logback-classic"  % logbackVersion,
      ) ++ fs2Deps,
    )
    .dependsOn(streamer)

lazy val streamer =
  (project in file("streamer"))
    .settings(
      name := "streamer",
      libraryDependencies ++= fs2Deps,
    )

lazy val blazegraphProject =
  (project in file("blazegraph-project"))
    .settings(
      name := "blazegraph-project",
      libraryDependencies ++= Seq("com.blazegraph" % "bigdata-core" % "2.1.4"),
    )
    .dependsOn(streamer)

lazy val fs2Deps =
  Seq(
    "co.fs2" %% "fs2-core" % "3.12.2",
    "co.fs2" %% "fs2-io"   % "3.12.2",
  )
