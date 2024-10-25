ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.15"

lazy val root = (project in file("."))
  .settings(
    name := "TipoDis"
  )

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

maintainer := "AndreyKa"

val zioVersion = "2.1.6"
libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-streams" % zioVersion,
  "dev.zio" %% "zio-http" % "3.0.1",
  "dev.zio" %% "zio-logging" % "2.3.2",
  "dev.zio" %% "zio-interop-cats" % "23.1.0.3",
  "dev.zio" % "zio-json-macros_2.13" % "0.7.1"
)

libraryDependencies += "dev.zio" %% "zio-config"          % "4.0.2"
libraryDependencies += "dev.zio" %% "zio-config-magnolia" % "4.0.2"
libraryDependencies += "dev.zio" %% "zio-config-typesafe" % "4.0.2"
libraryDependencies += "dev.zio" %% "zio-config-refined"  % "4.0.1"

scalacOptions += "-Ymacro-annotations"